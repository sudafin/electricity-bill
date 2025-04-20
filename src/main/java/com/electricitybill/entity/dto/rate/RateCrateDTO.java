package com.electricitybill.entity.dto.rate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RateCrateDTO {
    private String userType;
    private Integer status;
    private BigDecimal flatPrice;
    private BigDecimal peakPrice;
    private BigDecimal valleyPrice;
    private BigDecimal summerPeakPrice;
    private LocalDate effectiveDate;
    private LocalDate expireDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime peakStart;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime peakEnd;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime valleyStart;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime valleyEnd;
    private String summerPeriod;
    private BigDecimal discount;
}
