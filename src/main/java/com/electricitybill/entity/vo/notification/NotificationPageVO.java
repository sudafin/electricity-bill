package com.electricitybill.entity.vo.notification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationPageVO {
    private Long id;
    private String content;
    private String type;
    private String level;
    private String title;
    private LocalDateTime createTime;
    private Integer readStatus;
}
