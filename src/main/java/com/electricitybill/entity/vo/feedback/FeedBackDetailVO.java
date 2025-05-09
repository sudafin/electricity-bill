package com.electricitybill.entity.vo.feedback;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedBackDetailVO {
    private String feedbackType;
    private String feedbackStatus;
    private String userName;
    private String content;
    private LocalDateTime submitTime;
    private LocalDateTime processTime;
    private String processorName;
    private String response;
}
