package com.electricitybill.entity.vo.rate;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RateDetailVO {
    private String userType;
    private Integer status;
    private BigDecimal flatPrice;
    private BigDecimal peakPrice;
    private BigDecimal valleyPrice;
    private BigDecimal summerPeakPrice;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private LocalTime peakStart;
    private LocalTime peakEnd;
    private LocalTime valleyStart;
    private LocalTime valleyEnd;
    private String summerPeriod;
    private BigDecimal discount;
}
