package com.electricitybill.entity.dto.feedback;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FeedBackProcessDTO {
    @NotNull
    private String feedbackId;
    @NotNull
    private String feedbackStatus;
    private String response;
}
