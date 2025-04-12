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
import com.electricitybill.entity.vo.user.UserDetailVO;
import com.electricitybill.entity.vo.user.UserPageVO;
import com.electricitybill.enums.BillType;
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
import java.util.*;

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
        if (ebUser.getValidType().equals(ValidType.VALID.getValue())) {
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
            user.setValidType(ValidType.INVALID.getValue());
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



}
