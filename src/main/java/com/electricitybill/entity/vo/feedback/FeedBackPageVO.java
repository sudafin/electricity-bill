package com.electricitybill.entity.vo.feedback;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FeedBackPageVO {
    private String FeeBackId;
    private String FeedBackType;
    private String FeedBackStatus;
    private String userName;
    private String FeedBackContent;
    private LocalDateTime submitTime;
}
