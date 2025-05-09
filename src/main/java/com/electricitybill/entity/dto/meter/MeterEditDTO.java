
package com.electricitybill.entity.dto.meter;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MeterEditDTO {
    @NotNull(message = "电表ID不能为空")
    private String id;
    private Long inspectionId;
    private String model;
    private String status;
    private String installPlace;
    private LocalDateTime installDate;
    private LocalDateTime lastMeterReadingDate;
    private LocalDateTime startMeterReadingDate;
    private BigDecimal startReading;
    private BigDecimal endingReading;
    private String inspectionResult;
    private String inspectionStatus;
    private LocalDateTime inspectionTime;
    private String inspectionType;
    private String inspectorName;
    private String remark;

}
