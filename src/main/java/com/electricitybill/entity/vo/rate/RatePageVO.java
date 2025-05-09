package com.electricitybill.entity.vo.rate;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RatePageVO {
    private Long id;
    private String userType;
    private Integer status;
    private BigDecimal flatPrice;
    private BigDecimal peakPrice;
    private BigDecimal valleyPrice;
    private LocalDate effectiveDate;
    private LocalDate expireDate;

}
