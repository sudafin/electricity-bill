package com.electricitybill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.po.EbBill;
import com.electricitybill.entity.po.EbMeter;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.vo.user.UserBillVO;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.EbBillMapper;
import com.electricitybill.mapper.EbMeterMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbBillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.BeanUtils;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-17
 */
@Service
public class EbBillServiceImpl extends ServiceImpl<EbBillMapper, EbBill> implements IEbBillService {
    @Resource
    private EbUserMapper userMapper;
    @Resource
    private EbMeterMapper meterMapper;
    @Override
    public List<UserBillVO> queryUserBill(Long userId) {
        EbUser ebUser = userMapper.selectById(userId);
        if (ObjectUtils.isEmpty(ebUser)) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        EbMeter ebMeter = meterMapper.selectById(ebUser.getMeterId());
        if (ObjectUtils.isEmpty(ebMeter)) {
            throw new DbException(Constant.METER_NOT_EXIST);
        }
        List<EbBill> ebBills = this.list(new LambdaQueryWrapper<EbBill>().eq(EbBill::getUserId, userId));
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
