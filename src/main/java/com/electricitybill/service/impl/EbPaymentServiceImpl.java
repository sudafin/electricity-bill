package com.electricitybill.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.entity.po.EbBill;
import com.electricitybill.entity.po.EbPayment;
import com.electricitybill.entity.vo.payment.PaymentUserVO;
import com.electricitybill.enums.BillType;
import com.electricitybill.mapper.EbBillMapper;
import com.electricitybill.mapper.EbPaymentMapper;
import com.electricitybill.service.IEbPaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
@Slf4j
public class EbPaymentServiceImpl extends ServiceImpl<EbPaymentMapper, EbPayment> implements IEbPaymentService {
    @Resource
    private EbBillMapper ebBillMapper;

    @Override
    public PaymentUserVO getPaymentRecords() {
        //拿到当前年度的数据账单
        List<EbBill> ebBills = ebBillMapper.selectList(new LambdaQueryWrapper<EbBill>()
                .eq(EbBill::getUserId, UserContextUtils.getUserId())
                .ge(EbBill::getCreatedAt, DateUtil.beginOfYear(DateUtil.date()))
        );
        if(ebBills.isEmpty()){
            return new PaymentUserVO(BigDecimal.ZERO,0,BigDecimal.ZERO);
        }
        //拿到已经支付的账单和未支付的账单
        List<EbBill> paidBill = ebBills.stream().filter(ebBill -> ebBill.getStatus().equals(BillType.PAID.getDesc())).collect(Collectors.toList());
        List<EbBill> unpaidBill = ebBills.stream().filter(ebBill -> ebBill.getStatus().equals(BillType.UNPAID.getDesc())).collect(Collectors.toList());
        PaymentUserVO paymentUserVO = new PaymentUserVO();
        paymentUserVO.setPaymentCount(paidBill.size());
        paymentUserVO.setTotalCost(paidBill.stream().map(EbBill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        paymentUserVO.setCurrentDebt(unpaidBill.stream().map(EbBill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        return paymentUserVO;
    }
}
