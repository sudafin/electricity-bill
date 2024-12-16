package com.electricitybill.service.impl;

import cn.hutool.core.util.IdUtil;
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
import com.electricitybill.entity.vo.user.UserPaymentRecordVO;
import com.electricitybill.entity.vo.user.UserPaymentVO;
import com.electricitybill.enums.UserStatusType;
import com.electricitybill.enums.UserType;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.*;
import com.electricitybill.service.IEbUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.BeanUtils;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.UserContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
    private EbPaymentMapper paymentMapper;
    @Resource
    private EbAdminMapper adminMapper;
    @Resource
    private EbReconciliationMapper reconciliationMapper;
    @Resource
    private EbRateMapper ebRateMapper;
    private EbUserMapper ebUserMapper;
    @Autowired
    private EbPaymentMapper ebPaymentMapper;

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
            List<EbElectricityUsage> ebElectricityUsages = ebElectricityUsageList.stream()
                    .sorted(Comparator.comparing(EbElectricityUsage::getEndTime)
                            .reversed()).collect(Collectors.toList());
            //拿到最近7天的数据,没有就设置为0
            //因为mapToDouble返回的是DoubleStream,而collect方法需要的是Stream,所以使用了boxed()方将Double流转换为Double对象的流才能调用collect方法,
            List<Double> electricityWeekUsageList = ebElectricityUsages.stream()
                    //拿到前面最新日期的7个数据
                    .limit(7)
                    //再次把最新的日期放到列表最后面
                    .sorted(Comparator.comparing(EbElectricityUsage::getEndTime))
                    .mapToDouble(usage->{
                        //查看最近7个的数据
                        LocalDateTime endTime = usage.getEndTime();
                        LocalDate endDate = endTime.toLocalDate();
                        //如果是最近7天,就加入到list中,否则就跳过
                        if (endDate.isAfter(LocalDate.now().minusDays(7))) {
                            return usage.getUsageAmount().doubleValue();
                        }
                        return new BigDecimal(0).doubleValue();
                    })
                    .boxed()
                    .collect(toList());
            log.debug("总用电量:{}", totalElectricity);
            log.debug("最近7天的用电量:{}", electricityWeekUsageList);
            dashboardVO.setTotalElectricityUsage((long) totalElectricity);
            dashboardVO.setElectricityWeekUsageList(electricityWeekUsageList);
        }
        //补充账单总数和总金额
        List<EbPayment> ebPaymentList = paymentMapper.selectList(new LambdaQueryWrapper<>());
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
        Page<EbUser> page = lambdaQuery()
                .eq(StrUtil.isNotBlank(userPageQuery.getUserType()), EbUser::getUserType, userPageQuery.getUserType())
                .eq(StrUtil.isNotBlank(userPageQuery.getPhone()), EbUser::getPhone, userPageQuery.getPhone())
                .eq(StrUtil.isNotBlank(userPageQuery.getName()), EbUser::getUsername, userPageQuery.getName())
                .eq(StrUtil.isNotBlank(userPageQuery.getMeterNumber()), EbUser::getMeterNo, userPageQuery.getMeterNumber())
                .ge(userPageQuery.getStartDate() != null, EbUser::getLastPaymentDate, userPageQuery.getStartDate())
                .le(userPageQuery.getEndDate() != null, EbUser::getLastPaymentDate, userPageQuery.getEndDate())
                .page(ebUserPage);
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
        UserDetailVO userDetailVO = BeanUtils.copyBean(ebUser, UserDetailVO.class);
        if (ObjectUtils.isEmpty(userDetailVO)) {
            throw new BizIllegalException(Constant.CONVERT_ERROR);
        }
        log.debug("userDetailVO的数据:{}", userDetailVO);
        List<EbPayment> ebPaymentList = paymentMapper.selectList(new LambdaQueryWrapper<EbPayment>().eq(EbPayment::getUserId, userId));
        //如果支付列表为空，则返回空列表
        ArrayList<UserPaymentRecordVO> recordVOArrayList = new ArrayList<>();
        if (CollUtils.isEmpty(ebPaymentList)) {
            userDetailVO.setUserPaymentRecordVOList(CollUtils.emptyList());
        } else {
            ebPaymentList.forEach(ebPayment -> {
                UserPaymentRecordVO userPaymentRecordVO = new UserPaymentRecordVO();
                userPaymentRecordVO.setPaymentAmount(ebPayment.getAmount());
                userPaymentRecordVO.setPaymentStatus(ebPayment.getStatus());
                EbAdmin ebAdmin = adminMapper.selectById(ebPayment.getOperatorId());
                if (ObjectUtils.isEmpty(ebAdmin)) {
                    throw new DbException(Constant.DATA_QUERY_EMPTY);
                } else {
                    userPaymentRecordVO.setOperator(ebAdmin.getAccount());
                    userPaymentRecordVO.setRemark(ebPayment.getRemark());
                    userPaymentRecordVO.setPaymentTime(ebPayment.getPaymentTime());
                    userPaymentRecordVO.setPaymentMethod(ebPayment.getPaymentMethod());
                    recordVOArrayList.add(userPaymentRecordVO);
                }
                log.debug("userPaymentRecordVO的数据:{}", userPaymentRecordVO);
            });
        }
        userDetailVO.setUserPaymentRecordVOList(recordVOArrayList);
        return userDetailVO;
    }

    @Override
    public R insertUser(UserDTO userDTO) {
        log.debug("userDTO的数据:{}", userDTO);
        //判断用户是否存在
        EbUser ebUser = baseMapper.selectOne(new LambdaQueryWrapper<EbUser>().eq(EbUser::getPhone, userDTO.getPhone()));
        if (ObjectUtils.isNotEmpty(ebUser)) {
            throw new DbException(Constant.USER_EXIST);
        }
        EbUser user = BeanUtils.copyBean(userDTO, EbUser.class);
        user.setElectricityUsage(new BigDecimal(0));
        user.setLastPaymentDate(LocalDateTime.now());
        int insert = baseMapper.insert(user);
        if (insert != 1) {
            throw new DbException(Constant.DB_INSERT_FAILURE);
        }
        return R.ok();
    }

    @Override
    public R deleteUser(List<Long> userIds) {
        log.debug("userIds的数据:{}", userIds);
        int delete = baseMapper.deleteBatchIds(userIds);
        if (delete != userIds.size()) {
            throw new DbException(Constant.DB_DELETE_FAILURE);
        }
        return R.ok();
    }


    @Override
    public R updateUser(UserDTO userDTO) {
        log.debug("userDTO的数据:{}", userDTO);
        if (ObjectUtils.isEmpty(baseMapper.selectOne(new LambdaQueryWrapper<EbUser>().eq(EbUser::getPhone, userDTO.getPhone())))){
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        EbUser user = BeanUtils.copyBean(userDTO, EbUser.class);
        int update = baseMapper.update(user, new LambdaQueryWrapper<EbUser>().eq(EbUser::getPhone, userDTO.getPhone()));
        if (update != 1) {
            throw new DbException(Constant.DB_UPDATE_FAILURE);
        }
        return R.ok();
    }

    @Override
    public R pay(Long userId, Double money, String paymentMethod) {
        EbUser ebUser = baseMapper.selectById(userId);
        //缴费
        ebUser.setBalance(ebUser.getBalance().add(new BigDecimal(money)));
        if(ebUser.getBalance().compareTo(new BigDecimal(0)) > 0){
            ebUser.setAccountStatus(UserStatusType.NORMAL.getDesc());
        }
        int update = baseMapper.update(ebUser, new LambdaQueryWrapper<EbUser>().eq(EbUser::getId, userId));
        if (update != 1) {
            throw new DbException(Constant.DB_UPDATE_FAILURE);
        }
        //先将对账id生成
        long reconciliationId = IdUtil.getSnowflakeNextId();
        long paymentId = IdUtil.getSnowflakeNextId();
        //生成支付账单
        EbPayment ebPayment = new EbPayment();
        ebPayment.setId(paymentId);
        ebPayment.setUserId(ebUser.getId());
        ebPayment.setAmount(new BigDecimal(money));
        ebPayment.setPaymentMethod(paymentMethod);
        ebPayment.setPaymentTime(LocalDateTime.now());
        ebPayment.setReconciliationId(reconciliationId);
        ebPayment.setStatus("已支付");
        ebPayment.setOperatorId(UserContextUtils.getUser());
        int paymentInsert = ebPaymentMapper.insert(ebPayment);
        if (paymentInsert != 1) {
            throw new DbException(Constant.DB_INSERT_FAILURE);
        }

        //生成对账单
        EbReconciliation ebReconciliation = new EbReconciliation();;
        ebReconciliation.setReconciliationNo(reconciliationId);
        ebReconciliation.setUserId(ebUser.getId());
        ebReconciliation.setStartDate(LocalDate.now());
        ebReconciliation.setEndDate(LocalDate.now().plusDays(7));
        ebReconciliation.setStatus("待审批");
        ebReconciliation.setPaymentStatus("已支付");
        ebReconciliation.setPaymentId(paymentId);
        //查询电费单价
        List<EbRate> ebRateList = ebRateMapper.selectList(new LambdaQueryWrapper<>());
        //各个用户类型的电费率kv集合
        Map<String, BigDecimal> map = ebRateList.stream().collect(Collectors.toMap(EbRate::getUserType, EbRate::getPrice));
        BigDecimal moneyBigDecimal = BigDecimal.valueOf(money);
        if(ebUser.getUserType().equals(UserType.RESIDENT.getDesc())){;
            ebReconciliation.setTotalUsage(map.get(UserType.RESIDENT.getDesc()).multiply(moneyBigDecimal));
        }else if(ebUser.getUserType().equals(UserType.BUSINESSES.getDesc())){
            ebReconciliation.setTotalUsage(map.get(UserType.BUSINESSES.getDesc()).multiply(moneyBigDecimal));
        }
        ebReconciliation.setTotalAmount(BigDecimal.valueOf(money));
        int insert = reconciliationMapper.insert(ebReconciliation);
        if (insert != 1) {
            throw new DbException(Constant.DB_INSERT_FAILURE);
        }
        return R.ok();
    }

    @Override
    public UserPaymentVO queryUserPayment(Long userId) {
        EbUser ebUser = baseMapper.selectById(userId);
        if (ObjectUtils.isEmpty(ebUser)) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        UserPaymentVO userPaymentVO = new UserPaymentVO();
        userPaymentVO.setUsername(ebUser.getUsername());
        userPaymentVO.setUserType(ebUser.getUserType());
        userPaymentVO.setMeterNo(ebUser.getMeterNo());
        if(ebUser.getAccountStatus().equals("欠费")){
            userPaymentVO.setUnpaidAmount(ebUser.getBalance());
            userPaymentVO.setBalance(new BigDecimal(0));
        }else{
            userPaymentVO.setBalance(ebUser.getBalance());
            userPaymentVO.setUnpaidAmount(new BigDecimal(0));
        }
        return userPaymentVO;
    }

}
