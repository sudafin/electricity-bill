package com.electricitybill.entity.dto.meter;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MeterCreateDTO {
    private String model;
    private String status;
    private String installPlace;
    private LocalDateTime installDate;
}
