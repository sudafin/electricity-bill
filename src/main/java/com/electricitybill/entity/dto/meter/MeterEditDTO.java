
package com.electricitybill.entity.dto.meter;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class MeterEditDTO {
    private String idCardNo;
    @NotNull(message = "电表ID不能为空")
    private Long meterId;
    private String model;
    private String status;
    private String installPlace;
    private LocalDateTime installDate;
}
