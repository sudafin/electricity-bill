package com.electricitybill.entity.dto.role;

import com.electricitybill.entity.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RolePageQuery extends PageQuery {
    private String account;
    private String adminId;
    private String role;
}
