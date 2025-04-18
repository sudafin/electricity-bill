package com.electricitybill.entity.vo.bill;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BillPageVO {
    private Long paymentId;
    private Long id;
    private BigDecimal usageAmount;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentTime;

}
