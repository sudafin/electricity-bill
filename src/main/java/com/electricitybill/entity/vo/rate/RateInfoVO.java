package com.electricitybill.entity.vo.rate;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RateInfoVO {
    private Long rateId;
    private String rateName;
    private BigDecimal rateValue;
}
