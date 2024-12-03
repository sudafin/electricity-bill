package com.electricitybill.entity.dto.reconciliation;

import com.electricitybill.entity.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ReconciliationPageQuery extends PageQuery {
    private String reconciliationNo;
    private String username;
    private String userType;
    private String reconciliationStatus;
    private String meterNo;
}
