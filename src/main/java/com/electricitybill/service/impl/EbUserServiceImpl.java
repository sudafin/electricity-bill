package com.electricitybill.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.user.UserCreateDTO;
import com.electricitybill.entity.dto.user.UserEditDTO;
import com.electricitybill.entity.dto.user.UserPageQuery;
import com.electricitybill.entity.dto.usertype.UserTypeCreateDTO;
import com.electricitybill.entity.po.*;
import com.electricitybill.entity.vo.user.UserDetailVO;
import com.electricitybill.entity.vo.user.UserInfoVO;
import com.electricitybill.entity.vo.user.UserPageVO;
import com.electricitybill.enums.*;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.*;
import com.electricitybill.service.IEbUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.service.IEbUserTypeService;
import com.electricitybill.utils.BeanUtils;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.UserContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

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
    private EbMeterMapper ebMeterMapper;

    @Resource
    private EbBillMapper ebBillMapper;

    @Resource
    private IEbUserTypeService ebUserTypeService;
    @Resource
    private EbScheduledTaskMapper ebScheduledTaskMapper;

    @Override
    public PageDTO<UserPageVO> queryUserPage(UserPageQuery userPageQuery) {
        log.debug("userPageQuery:{}", userPageQuery);

        /**
         * new Page<>(PageNo, PageSize); 其中第一个参数是当前页，第二个参数是每页显示的条数
         */

        // 分页查询条件
        Page<EbUser> ebUserPage = new Page<>(userPageQuery.getPageNo(), userPageQuery.getPageSize());
        // 查询数据库条件
        Page<EbUser> page = lambdaQuery().eq(EbUser::getValidType, ValidType.VALID.getValue()).eq(StrUtil.isNotBlank(userPageQuery.getUserType()), EbUser::getUserType, userPageQuery.getUserType()).eq(StrUtil.isNotBlank(userPageQuery.getPhone()), EbUser::getPhone, userPageQuery.getPhone()).like(StrUtil.isNotBlank(userPageQuery.getName()), EbUser::getUsername, userPageQuery.getName()).eq(StrUtil.isNotBlank(userPageQuery.getMeterId()), EbUser::getMeterId, userPageQuery.getMeterId()).ge(userPageQuery.getStartDate() != null, EbUser::getLastPaymentDate, userPageQuery.getStartDate()).le(userPageQuery.getEndDate() != null, EbUser::getLastPaymentDate, userPageQuery.getEndDate()).page(ebUserPage);
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
        if (!ebUser.getValidType().equals(ValidType.VALID.getValue())) {
            throw new BizIllegalException(Constant.USER_INVALID);
        }
        UserDetailVO userDetailVO = BeanUtils.copyBean(ebUser, UserDetailVO.class);
        if (ObjectUtils.isEmpty(userDetailVO)) {
            throw new BizIllegalException(Constant.CONVERT_ERROR);
        }
        log.debug("userDetailVO的数据:{}", userDetailVO);
        EbMeter ebMeter = ebMeterMapper.selectOne(new LambdaQueryWrapper<EbMeter>().eq(EbMeter::getUserId, userId));
        long unPaidBills = ebBillMapper.selectList(new LambdaQueryWrapper<EbBill>().eq(EbBill::getUserId, ebUser.getId())).stream().filter(ebBill -> !ebBill.getStatus().equals(BillType.PAID.getDesc())).count();
        userDetailVO.setOutstandingBill((int) unPaidBills);
        if (ObjectUtils.isEmpty(ebMeter)) {
           userDetailVO.setLastMeterReadingDate(null);
           return userDetailVO;
        }
        userDetailVO.setLastMeterReadingDate(ebMeter.getLastMeterReadingDate());
        return userDetailVO;
    }

    @Override
    public R insertUser(UserCreateDTO userCreateDTO) {
        //判断用户是否存在
        EbUser ebUser = baseMapper.selectOne(new LambdaQueryWrapper<EbUser>().eq(EbUser::getIdCardNo, userCreateDTO.idCardNo));
        if (ObjectUtils.isNotEmpty(ebUser)) {
            throw new DbException(Constant.USER_EXIST);
        }

        EbUser user = BeanUtils.copyBean(userCreateDTO, EbUser.class);
        user.setAccountStatus(AccountStatus.NORMAL.getDesc());
        //TODO初始密码,后面改
        user.setAccount(userCreateDTO.getPhone());
        user.setPassword("123");
        baseMapper.insert(user);
        return R.ok();
    }

    @Override
    @Transactional
    public R deleteUser(List<Long> userIds) {
        List<EbUser> ebUsers = listByIds(userIds);
        ebUsers.forEach(user -> {
            user.setValidType(ValidType.INVALID.getValue());
        });
        updateBatchById(ebUsers);
        return R.ok();
    }


    @Override
    public R adminUpdateUser(UserCreateDTO userCreateDTO) {
        if (ObjectUtils.isEmpty(baseMapper.selectOne(new LambdaQueryWrapper<EbUser>().eq(EbUser::getIdCardNo, userCreateDTO.idCardNo)))) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        EbUser user = BeanUtils.copyBean(userCreateDTO, EbUser.class);
        baseMapper.updateById(user);
        return R.ok();
    }

    @Override
    public List<String> getUserTypeList() {
        List<EbUserType> res = ebUserTypeService.lambdaQuery().eq(EbUserType::getStatus, ValidType.VALID.getValue()).list();
        if(res.isEmpty()){
            return ListUtil.empty();
        }
        return res.stream().map(EbUserType::getTypeName).collect(Collectors.toList());
    }

    @Override
    public R addUserType(@NotNull UserTypeCreateDTO userTypeCreateDTO) {
        ebUserTypeService.lambdaQuery().eq(EbUserType::getTypeName, userTypeCreateDTO.getTypeName()).oneOpt().ifPresent(type -> {
            throw new DbException(Constant.USER_TYPE_EXIST);
        });
        EbUserType ebUserType = BeanUtils.copyBean(userTypeCreateDTO, EbUserType.class);
        ebUserType.setStatus(ValidType.VALID.getValue());
        ebUserTypeService.save(ebUserType);
        return R.ok();
    }

    @Override
    public R userEditInfo(UserEditDTO userEditDTO) {
        Long userId = UserContextUtils.getUserId();
        if(ObjectUtils.isEmpty(userId)){
            throw new BadRequestException(Constant.REQUEST_PARAM_MISSING);
        }
        EbUser ebUser = getById(userId);
        if(ObjectUtils.isEmpty(ebUser)){
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        if (ebUser.getValidType().equals(ValidType.INVALID.getValue())) {
            throw new BizIllegalException(Constant.USER_INVALID);
        }
        ObjectUtils.assignIfNotNull(ebUser, userEditDTO.getAddress(), EbUser::setAddress);
        ObjectUtils.assignIfNotNull(ebUser, userEditDTO.getPhone(), EbUser::setPhone);
        ObjectUtils.assignIfNotNull(ebUser, userEditDTO.getUsername(), EbUser::setUsername);
        List<EbScheduledTask> ebScheduledTasks = ebScheduledTaskMapper.selectList(
                new LambdaQueryWrapper<EbScheduledTask>()
                        .eq(EbScheduledTask::getTaskType, TaskType.USER_TASK.getDesc())
        );
        if (ebScheduledTasks.isEmpty()) {
            throw new BizIllegalException(Constant.TASK_NOT_EXIST);
        }
        ebScheduledTasks.forEach(ebScheduledTask -> {
            //拿到账单提醒的任务
            if (ebScheduledTask.getId().equals((long) UserTaskType.BILL_REMINDER.getValue())) {
                List<String> userIdList = getStringList(ebScheduledTask, userId, userEditDTO.getBillReminder());
                ebScheduledTask.setUserIds(String.join(",", userIdList));
            }
            if (ebScheduledTask.getId().equals((long)UserTaskType.PAYMENT_REMINDER.getValue())) {
                List<String> userIdList = getStringList(ebScheduledTask, userId, userEditDTO.getPaymentReminder());
                ebScheduledTask.setUserIds(String.join(",", userIdList));
            }
            ebScheduledTaskMapper.updateById(ebScheduledTask);
        });

        updateById(ebUser);
        return R.ok();
    }

    private static List<String> getStringList(EbScheduledTask ebScheduledTask, Long userId, Boolean userEditDTO) {
        String userIds = ebScheduledTask.getUserIds();
        //,逗号切割,转为list
        List<String> userIdList = Arrays.asList(userIds.split(","));
        if (userIdList.contains(String.valueOf(userId)) && !userEditDTO) {
            userIdList.remove(String.valueOf(userId));
        } else if (!userIdList.contains(String.valueOf(userId)) && userEditDTO) {
            userIdList.add(String.valueOf(userId));
        }
        return userIdList;
    }

    @Override
    public UserInfoVO getUserInfo() {
        Long userId = UserContextUtils.getUserId();
        EbUser ebUser = getById(userId);
        if(ObjectUtils.isEmpty(ebUser)){
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        if (ebUser.getValidType().equals(ValidType.INVALID.getValue())) {
            throw new BizIllegalException(Constant.USER_INVALID);
        }
        UserInfoVO userInfoVO = BeanUtils.copyBean(ebUser, UserInfoVO.class);
        if (ObjectUtils.isEmpty(userInfoVO)) {
            throw new BizIllegalException(Constant.CONVERT_ERROR);
        }
        EbMeter ebMeter = ebMeterMapper.selectOne(new LambdaQueryWrapper<EbMeter>().eq(EbMeter::getUserId, userId));
        if (ObjectUtils.isEmpty(ebMeter)) {
            userInfoVO.setMeterModel(null);
        }
        userInfoVO.setMeterModel(ebMeter.getModel());
        List<EbScheduledTask> ebScheduledTasks = ebScheduledTaskMapper.selectList(
                new LambdaQueryWrapper<EbScheduledTask>()
                        .eq(EbScheduledTask::getTaskType, TaskType.USER_TASK.getDesc())
        );
        if (ebScheduledTasks.isEmpty()) {
            throw new BizIllegalException(Constant.TASK_NOT_EXIST);
        }
        ebScheduledTasks.forEach(ebScheduledTask -> {
            //拿到账单提醒的任务
            if (ebScheduledTask.getId().equals((long)UserTaskType.BILL_REMINDER.getValue())) {
                String userIds = ebScheduledTask.getUserIds();
                //,逗号切割,转为list
                List<String> userIdList = Arrays.asList(userIds.split(","));
                if (userIdList.contains(String.valueOf(userId))) {
                    userInfoVO.setBillReminder(true);
                }else
                    userInfoVO.setBillReminder(false);
            }
            if (ebScheduledTask.getId().equals((long) UserTaskType.PAYMENT_REMINDER.getValue())) {
                String userIds = ebScheduledTask.getUserIds();
                //,逗号切割,转为list
                List<String> userIdList = Arrays.asList(userIds.split(","));
                if (userIdList.contains(String.valueOf(userId))) {
                    userInfoVO.setPaymentReminder(true);
                }else
                    userInfoVO.setPaymentReminder(false);
            }
        });
        return userInfoVO;
    }

}
