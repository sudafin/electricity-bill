package com.electricitybill.entity.vo.bill;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BillPageVO {
    private Long billId;;
    private BigDecimal usage;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private LocalDateTime dueDate;
    private String status;

}
