package com.electricitybill.entity.vo.reconciliation;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ApprovalDetailVO {
    private Long reconciliationNo;
    private String username;
    private BigDecimal balance;
    private String comment;
    private Boolean isApproved;
private String status;
    List<ApprovalRecordVO> approvalRecordList;
}
