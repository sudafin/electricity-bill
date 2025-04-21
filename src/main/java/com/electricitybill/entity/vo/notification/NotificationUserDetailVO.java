package com.electricitybill.entity.vo.notification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationUserDetailVO {
    private String content;
    private String title;
    private LocalDateTime createTime;
    private String type;
}
