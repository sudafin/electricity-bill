package com.electricitybill.entity.vo.role;

import com.electricitybill.entity.dto.role.PermissionDTO;
import lombok.Data;

import java.util.List;

@Data
public class PermissionDetailVO {
    private String account;
    private String roleName;
    private String roleDesc;
    private List<PermissionDTO> permissionList;
}
