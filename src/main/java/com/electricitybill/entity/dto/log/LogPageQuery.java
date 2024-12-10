package com.electricitybill.entity.dto.log;

import com.electricitybill.entity.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LogPageQuery extends PageQuery {
    private String operatorName;
    private String operationType;
    private String module;
}
