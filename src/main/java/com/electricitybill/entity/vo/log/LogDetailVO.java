package com.electricitybill.entity.vo.log;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogDetailVO {
    private String operatorName;
    private String operationType;
    private String module;
    private String description;
    private String ip;
    private String status;
    private String requestParams;
    private String requestBody;
    private String responseData;
    private String errorMsg;
    private LocalDateTime createTime;
}
