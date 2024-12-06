package com.electricitybill.entity.dto.paymennt;

import com.electricitybill.entity.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class PaymentPageQuery extends PageQuery {
    private Long paymentId;
    private String status;
    private String paymentMethod;
}
