package com.electricitybill.entity.vo.role;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RolePageVO {
    private String account;
    private Long adminId;
    private String role;
    private String roleDesc;
    private LocalDateTime createTime;
    private Integer status;
}
