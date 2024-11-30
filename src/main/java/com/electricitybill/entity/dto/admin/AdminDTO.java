package com.electricitybill.entity.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDTO {
    private String roleName;
    private String userName;
    private Long id;
}
