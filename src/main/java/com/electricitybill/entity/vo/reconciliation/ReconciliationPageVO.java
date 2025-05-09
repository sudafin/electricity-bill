package com.electricitybill.entity.vo.reconciliation;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReconciliationPageVO {
    private Long reconciliationNo;
    private String username;
    private String userType;
    private String reconciliationStatus;
    private String meterNo;
    private LocalDateTime reconciliationTime;
    private BigDecimal balance;
}
