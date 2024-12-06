package com.electricitybill.entity.vo.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentPageVO {
    private Long paymentId;
    private String username;
    private BigDecimal balance;
    private String paymentMethod;
    private String status;
    private LocalDateTime paymentTime;
}
