
package com.electricitybill.entity.dto.meter;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MeterInspectionDTO {

    private String meterId;
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

    private LocalDateTime nextInspectionTime;

    private BigDecimal repairCost;

    private String remark;

    /**
     * 状态: pending（待处理）/processing（处理中）/completed（已完成）
     */
    private String inspectionStatus;
}
