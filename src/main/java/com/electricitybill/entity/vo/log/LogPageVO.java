package com.electricitybill.entity.vo.log;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogPageVO {
    private Long id;
    private String operatorName;
    private String operationType;
    private String module;
    private String description;
    private String ip;
    private String status;
    private LocalDateTime createTime;
}
