package com.electricitybill.entity.vo.user;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserPaymentRecordVO {
    /**
     * 缴费时间
     */
    private LocalDateTime paymentTime;
    /**
     * 缴费金额
     */
    private BigDecimal paymentAmount;
    /**
     * 缴费状态
     */
    private String paymentStatus;
    /**
     * 支付方式
     */
    private String paymentMethod;
    /**
     * 操作人员
     */
    private String operator;
    /**
     * 备注
     */
    private String remark;
}
