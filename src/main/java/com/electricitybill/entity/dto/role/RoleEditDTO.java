package com.electricitybill.entity.dto.role;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("修改角色")
public class RoleEditDTO {
    private String account;
    private String role;
    private String password;
}
