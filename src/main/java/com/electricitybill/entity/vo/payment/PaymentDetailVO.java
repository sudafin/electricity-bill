package com.electricitybill.entity.vo.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDetailVO {
    private Long paymentId;
    private String username;
    private String userStatus;
    private BigDecimal balance;
    private String paymentMethod;
    private String status;
    private LocalDateTime paymentTime;
    private Boolean isReconciliate;
    private Long reconciliationId;
    private String reconciliationStatus;
    private String reconciliationRemark;

}
