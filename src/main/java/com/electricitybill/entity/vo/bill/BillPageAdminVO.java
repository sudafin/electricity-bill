package com.electricitybill.entity.vo.bill;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BillPageAdminVO {
    private Long paymentId;
    private Long billId;
    private Long userId;
    private String username;
    private String userType;
    private BigDecimal usageAmount;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentTime;
    private String status;

}
