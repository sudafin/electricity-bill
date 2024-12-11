package com.electricitybill.entity.dto.log;

import lombok.Data;

import java.util.Map;

@Data
public class LogDTO {
    private String method;
    private String path;
    private String requestParamMap;
    private String requestBody;
    private String responseBody;
    private String ip;
    private String userAgent;
    private String status;
    private String errorMsg;
}
