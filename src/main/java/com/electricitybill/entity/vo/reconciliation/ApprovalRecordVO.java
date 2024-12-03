package com.electricitybill.entity.vo.reconciliation;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalRecordVO {
    private Long reconciliationNo;
    private String approvalStatus;
    private LocalDateTime approvalTime;
    private String approvalOperator;
    private String comment;
}
