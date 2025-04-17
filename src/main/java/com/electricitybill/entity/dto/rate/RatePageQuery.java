package com.electricitybill.entity.dto.rate;

import com.electricitybill.entity.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RatePageQuery extends PageQuery {
    private String rateId;
    private String userType;
    private String status;
}
