package com.electricitybill.entity.vo.notification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDetailVO {
    private String senderName;
    private String senderRole;
    private String content;
    private String type;
    private String level;
    private String title;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}
