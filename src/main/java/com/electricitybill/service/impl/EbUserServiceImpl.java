package com.electricitybill.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.user.UserDTO;
import com.electricitybill.entity.dto.user.UserPageQuery;
import com.electricitybill.entity.po.*;
import com.electricitybill.entity.vo.dashboard.DashboardVO;
import com.electricitybill.entity.vo.user.UserDetailVO;
import com.electricitybill.entity.vo.user.UserPageVO;
import com.electricitybill.entity.vo.user.UserBillVO;
import com.electricitybill.enums.BillType;
import com.electricitybill.enums.UserType;
import com.electricitybill.enums.ValidType;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.*;
import com.electricitybill.service.IEbUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.BeanUtils;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
@Slf4j
public class EbUserServiceImpl extends ServiceImpl<EbUserMapper, EbUser> implements IEbUserService {
    @Resource
    private EbElectricityUsageMapper ebElectricityUsageMapper;
    @Resource
    private EbBillMapper ebBillMapper;
    @Resource
    private EbMeterMapper ebMeterMapper;

    @Override
    public DashboardVO getDashboardInfo() {
        /**
         * 拿到所有用户数量,用电量,支付金额,账单,用户类型和最近7天的用电量
         */
        DashboardVO dashboardVO = DashboardVO.builder().build();
        List<EbElectricityUsage> ebElectricityUsageList = ebElectricityUsageMapper.selectList(new LambdaQueryWrapper<>());
        //补充总用电量和最近7天的用电量
        if (CollUtils.isEmpty(ebElectricityUsageList)) {
            //如果没有数据,则默认为0或空,不用抛错误
            dashboardVO.setTotalElectricityUsage(0L);
            dashboardVO.setElectricityWeekUsageList(CollUtils.emptyList());
        } else {
            double totalElectricity = ebElectricityUsageList.stream().mapToDouble(usage -> new BigDecimal(String.valueOf(usage.getUsageAmount())).doubleValue()).sum();
            //将数据倒序,按时间降序,时间最新的在前面方便获取最新的7个数据
            List<EbElectricityUsage> ebElectricityUsages = ebElectricityUsageList.stream().sorted(Comparator.comparing(EbElectricityUsage::getEndTime).reversed()).collect(Collectors.toList());
            //拿到最近7天的数据,设置为0
            // 生成最近7天的日期列表
            List<LocalDate> recentDates = IntStream.range(0, 7).mapToObj(LocalDate.now()::minusDays).sorted().collect(Collectors.toList());
            // 生成 Map<LocalDate, BigDecimal>，这里假设 BigDecimal 的值为每个日期的日期差的 BigDecimal 表示
            Map<LocalDate, BigDecimal> dateToValueMap = recentDates.stream().collect(Collectors.toMap(date -> date, // 使用日期作为 key
                    date -> BigDecimal.ZERO, // 初始值为 0
                    (existing, replacement) -> existing, // 处理键冲突的策略
                    LinkedHashMap::new // 使用 LinkedHashMap 保持插入顺序
            ));
            ebElectricityUsages.stream().limit(7)
                    //再次把最新的日期放到列表最后面
                    .sorted(Comparator.comparing(EbElectricityUsage::getEndTime)).forEach(usage -> {
                        //拿到当前用电量的日期
                        LocalDate endDate = usage.getEndTime().toLocalDate();
                        //如果是最近7天,就加入到list中,否则就跳过
                        if (dateToValueMap.containsKey(endDate)) {
                            dateToValueMap.put(endDate, usage.getUsageAmount());
                        }
                    });
            //将dateToValueMap的value转为list
            List<Double> electricityWeekUsageList = dateToValueMap.values().stream()
                    //日期排序
                    .map(BigDecimal::doubleValue).collect(toList());
            log.debug("总用电量:{}", totalElectricity);
            log.debug("最近7天的用电量:{}", electricityWeekUsageList);
            dashboardVO.setTotalElectricityUsage((long) totalElectricity);
            dashboardVO.setElectricityWeekUsageList(electricityWeekUsageList);
        }
        //补充账单总数和总金额
        List<EbPayment> ebPaymentList = new ArrayList<>();
        if (CollUtils.isEmpty(ebPaymentList)) {
            dashboardVO.setTotalPaymentBill(0L);
            dashboardVO.setTotalAmount(0L);
        } else {
            int totalPaymentBill = ebPaymentList.size();
            int totalAmount = ebPaymentList.stream().mapToInt(payment -> new BigDecimal(String.valueOf(payment.getAmount())).intValue()).sum();
            log.debug("总账单数:{}", totalPaymentBill);
            log.debug("总金额:{}", totalAmount);
            dashboardVO.setTotalAmount((long) totalAmount);
            dashboardVO.setTotalPaymentBill((long) totalPaymentBill);
        }
        //补充用户类型和用户总数
        List<String> userTypeList = UserType.getUserTypeList();
        int totalUser = lambdaQuery().list().size();
        log.debug("用户类型列表:{}", userTypeList);
        Map<String, Integer> userTypeMap = new HashMap<>();
        userTypeList.forEach(userType -> {
            int userTypeCount = lambdaQuery().eq(EbUser::getUserType, userType).list().size();
            log.debug("用户类型{}的数量:{}", userType, userTypeCount);
            userTypeMap.put(userType, userTypeCount);
        });
        dashboardVO.setUserTypeMap(userTypeMap);
        dashboardVO.setTotalUser((long) totalUser);
        log.info("dashboardVO的对象数据:{}", dashboardVO);
        return dashboardVO;
    }

    @Override
    public PageDTO<UserPageVO> queryUserPage(UserPageQuery userPageQuery) {
        log.debug("userPageQuery:{}", userPageQuery);

        /**
         * new Page<>(PageNo, PageSize); 其中第一个参数是当前页，第二个参数是每页显示的条数
         */

        // 分页查询条件
        Page<EbUser> ebUserPage = new Page<>(userPageQuery.getPageNo(), userPageQuery.getPageSize());
        // 查询数据库条件
        Page<EbUser> page = lambdaQuery().eq(EbUser::getValid, ValidType.VALID.getValue()).eq(StrUtil.isNotBlank(userPageQuery.getUserType()), EbUser::getUserType, userPageQuery.getUserType()).eq(StrUtil.isNotBlank(userPageQuery.getPhone()), EbUser::getPhone, userPageQuery.getPhone()).like(StrUtil.isNotBlank(userPageQuery.getName()), EbUser::getUsername, userPageQuery.getName()).eq(StrUtil.isNotBlank(userPageQuery.getMeterId()), EbUser::getMeterId, userPageQuery.getMeterId()).ge(userPageQuery.getStartDate() != null, EbUser::getLastPaymentDate, userPageQuery.getStartDate()).le(userPageQuery.getEndDate() != null, EbUser::getLastPaymentDate, userPageQuery.getEndDate()).page(ebUserPage);
        //判断数据是否为空
        /**
         * page.getSize(): 每页的记录数 一页10条数据大小
         * page.getTotal(): 总记录数  一共100条数据大小
         * page.getRecords(): 当前页的数据列表 当前页的10条数据
         * page.getCurrent(): 当前页码 当前显示的页码
         * page.getPages(): 总页数 总共多少页
         */
        List<EbUser> ebUserList = page.getRecords();
        if (CollUtils.isEmpty(ebUserList)) {
            //如果为空，则返回空分页
            return PageDTO.empty(page);
        }
        List<UserPageVO> userPageVOList = BeanUtils.copyList(ebUserList, UserPageVO.class);
        log.debug("userPageVOList的数据:{}", userPageVOList);
        return PageDTO.of(page, userPageVOList);
    }

    @Override
    public UserDetailVO queryUserDetail(Long userId) {
        log.debug("userId:{}", userId);
        //根据id查询用户
        EbUser ebUser = getById(userId);

        if (ObjectUtils.isEmpty(ebUser)) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        if (ebUser.getValid().equals(ValidType.VALID.getValue())) {
            throw new BizIllegalException(Constant.USER_INVALID);
        }
        UserDetailVO userDetailVO = BeanUtils.copyBean(ebUser, UserDetailVO.class);
        if (ObjectUtils.isEmpty(userDetailVO)) {
            throw new BizIllegalException(Constant.CONVERT_ERROR);
        }
        log.debug("userDetailVO的数据:{}", userDetailVO);
        EbMeter ebMeter = ebMeterMapper.selectOne(new LambdaQueryWrapper<EbMeter>().eq(EbMeter::getUserId, userId));
        if (ObjectUtils.isEmpty(ebMeter)) {
            throw new BizIllegalException(Constant.METER_NOT_EXIST);
        }
        //获取未缴账单数量
        long unPaidBills = ebBillMapper.selectList(new LambdaQueryWrapper<EbBill>().eq(EbBill::getUserId, ebUser.getId())).stream().filter(ebBill -> !ebBill.getStatus().equals(BillType.PAID.getDesc())).count();
        userDetailVO.setLastMeterReadingDate(ebMeter.getLastMeterReadingDate());
        userDetailVO.setOutstandingBill((int) unPaidBills);
        return userDetailVO;
    }

    @Override
    public R insertUser(UserDTO userDTO) {
        //判断用户是否存在
        EbUser ebUser = baseMapper.selectOne(new LambdaQueryWrapper<EbUser>().eq(EbUser::getIdCardNo, userDTO.getIdCardNo));
        if (ObjectUtils.isNotEmpty(ebUser)) {
            throw new DbException(Constant.USER_EXIST);
        }
        EbUser user = BeanUtils.copyBean(userDTO, EbUser.class);
        baseMapper.insert(user);
        return R.ok();
    }

    @Override
    @Transactional
    public R deleteUser(List<Long> userIds) {
        List<EbUser> ebUsers = listByIds(userIds);
        ebUsers.forEach(user -> {
            user.setValid(ValidType.INVALID.getValue());
        });
        updateBatchById(ebUsers);
        return R.ok();
    }


    @Override
    public R updateUser(UserDTO userDTO) {
        if (ObjectUtils.isEmpty(baseMapper.selectOne(new LambdaQueryWrapper<EbUser>().eq(EbUser::getIdCardNo, userDTO.getIdCardNo)))) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        EbUser user = BeanUtils.copyBean(userDTO, EbUser.class);
        baseMapper.updateById(user);
        return R.ok();
    }


    @Override
    //TODO需要修改
    public List<UserBillVO> queryUserBill(Long userId) {
        EbUser ebUser = baseMapper.selectById(userId);
        if (ObjectUtils.isEmpty(ebUser)) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        List<EbBill> ebBills = ebBillMapper.selectList(new LambdaQueryWrapper<EbBill>().eq(EbBill::getUserId, userId));
        if (CollUtils.isEmpty(ebBills)) {
            throw new BizIllegalException(Constant.BILL_NOT_EXIST);
        }
        List<UserBillVO> userBillVOS = BeanUtils.copyList(ebBills, UserBillVO.class);
        userBillVOS.forEach(userBillVO -> {
            userBillVO.setUserType(ebUser.getUserType());
            userBillVO.setMeterId(ebUser.getMeterId());
            userBillVO.setUsername(ebUser.getUsername());
        });
        return userBillVOS;
    }

}
