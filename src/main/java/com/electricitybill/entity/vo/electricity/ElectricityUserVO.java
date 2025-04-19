package com.electricitybill.entity.vo.electricity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ElectricityUserVO {
    //昨天电量
    private BigDecimal yesterdayElectricity;
    //昨天电费
    private BigDecimal yesterdayElectricityFee;
    //当月电量
    private BigDecimal currentMonthElectricity;
    //当月电费
    private BigDecimal currentMonthElectricityFee;
}
