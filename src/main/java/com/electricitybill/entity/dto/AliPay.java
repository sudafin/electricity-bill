package com.electricitybill.entity.dto;

import lombok.Data;

@Data
public class AliPay {
    //订单号
    private String traceNo;
    //订单金额
    private double totalAmount;
    //订单物品
    private String subject;
}
