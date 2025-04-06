package com.electricitybill.entity.vo.rate;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RateInfoVO {
    private Long rateId;
    private String rateUserType;
    private Integer status;
    private BigDecimal flatPrice;
    private BigDecimal peakPrice;
    private BigDecimal valleyPrice;
    private BigDecimal summerPeakPrice;
}
