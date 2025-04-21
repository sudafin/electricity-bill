package com.electricitybill.entity.vo.notification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationUserPageVO {
    private Long id;
    private String content;
    private String title;
    private LocalDateTime createTime;
    private Integer readStatus;
}
