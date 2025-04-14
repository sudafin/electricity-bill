package com.electricitybill.entity.vo.feedback;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedBackDetailVO {
    private String FeedBackType;
    private String FeedBackStatus;
    private String userName;
    private String FeedBackContent;
    private LocalDateTime submitTime;
    private LocalDateTime processTime;
    private Long processorName;
    private String response;
}
