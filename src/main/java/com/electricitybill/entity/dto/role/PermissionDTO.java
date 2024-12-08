package com.electricitybill.entity.dto.role;

import com.electricitybill.entity.vo.role.PermissionDetailVO;
import lombok.Data;

import java.util.List;

@Data
public class PermissionDTO {
    private Long permissionId;
    private String permissionName;
    private List<PermissionDTO> children;
}
