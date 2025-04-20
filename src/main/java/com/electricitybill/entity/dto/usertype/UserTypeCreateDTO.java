package com.electricitybill.entity.dto.usertype;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
public class UserTypeCreateDTO {
    @NotNull(message = "描述不能为空")
    private String description;
    @NotNull(message = "用户类型名称不能为空")
    private String typeName;
}
