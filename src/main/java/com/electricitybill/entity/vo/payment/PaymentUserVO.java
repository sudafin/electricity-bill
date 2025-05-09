package com.electricitybill.entity.vo.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentUserVO {
    private BigDecimal totalCost;
    //次数
    private Integer paymentCount;
    //当前欠费
    private BigDecimal currentDebt;

}
