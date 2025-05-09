package com.electricitybill.entity.dto.bill;

import com.electricitybill.entity.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BillPageQuery extends PageQuery {
    private Long billId;;
    private String status;
}
