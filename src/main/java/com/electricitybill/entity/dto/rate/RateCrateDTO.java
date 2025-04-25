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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;

    @JsonFormat(pattern = "HH:mm:ss")  // 允许秒数
    private LocalTime peakStart;
    @JsonFormat(pattern = "HH:mm:ss")  // 允许秒数
    private LocalTime peakEnd;
    @JsonFormat(pattern = "HH:mm:ss")  // 允许秒数
    private LocalTime valleyStart;
    @JsonFormat(pattern = "HH:mm:ss")  // 允许秒数
    private LocalTime valleyEnd;

    private String summerPeriod;
    private BigDecimal discount;
}
