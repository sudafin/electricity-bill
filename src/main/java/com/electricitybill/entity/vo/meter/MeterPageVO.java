package com.electricitybill.entity.vo.meter;


import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MeterPageVO {
    private String meterId;
    private String model;
    private String status;
    private String userName;
    private LocalDate installDate;

    /**
     * 最近读表的时间
     */
    private LocalDateTime lastMeterReadingDate;

    /**
     * 一个周期内读表开始的度数 周期通常为为一个月
     */
    private BigDecimal startReading;
    /**
     * 一个周期内读表现在的度数
     */
    private BigDecimal endingReading;

    private String installPlace;
}
