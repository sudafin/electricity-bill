package com.electricitybill.entity.vo.reconciliation;

import com.electricitybill.entity.vo.user.UserPaymentRecordVO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReconciliationDetailVO {
    private Long reconciliationNo;
    private String username;
    private String userType;
    private String reconciliationStatus;
    private String meterNo;
    private LocalDateTime createTime;
    private BigDecimal balance;
    private String paymentStatus;
    private LocalDateTime approvalTime;
    private String  approvalOperator;
    private String approvalComment;
    private List<UserPaymentRecordVO> userPaymentRecordVOList;
}
