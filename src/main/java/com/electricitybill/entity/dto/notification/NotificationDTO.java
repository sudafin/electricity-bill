package com.electricitybill.entity.dto.notification;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NotificationDTO {
    private String title;
    private String content;
    private String type;
    private String level;
    private LocalDateTime expireTime;
    private List<String> senderList;
}
