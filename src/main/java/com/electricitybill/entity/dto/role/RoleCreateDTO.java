package com.electricitybill.entity.dto.role;

import lombok.Data;

import java.util.List;

@Data
public class RoleCreateDTO {
    private Boolean isRole;
    private String account;
    private String role;
    private String password;
    private String roleDesc;
    private List<Long> permissionIdList;
}
