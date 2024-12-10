package com.electricitybill.entity.vo.report;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReportDataVO {
    private LocalDate date;
    private String dateTimeStr;
    private BigDecimal feeAmount;
    private BigDecimal electricityUsage;
}
