package com.electricitybill.entity.vo.meter;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MeterDetailVO {
    private String model;
    private String status;
    private String username;
    private Long userId;
    private LocalDateTime installDate;

    private LocalDateTime startMeterReadingDate;
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
    /**
     * 检测类型: routine（常规检查）/fault（故障检修）/calibration（校准）")
     */

    private String inspectionType;

    /**
     * 检测结果: normal（正常）/fault（故障）/fixed（已修复）
     */
    private String inspectionResult;

    /**
     * 故障描述
     */
    private String faultDescription;

    /**
     * 解决方案
     */

    private String solution;

    /**
     * 检测人员姓名
     */
    private String inspectorName;


    private LocalDateTime inspectionTime;

    private String remark;

    /**
     * 状态: pending（待处理）/processing（处理中）/completed（已完成）
     */
    private String inspectionStatus;

    private Long inspectionId;
}
