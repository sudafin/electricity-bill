package com.electricitybill.entity.vo.meter;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MeterPageVO {
    private Long meterId;
    private String model;
    private String status;
    private LocalDate installDateBegin;
    private LocalDate installDateEnd;
}
