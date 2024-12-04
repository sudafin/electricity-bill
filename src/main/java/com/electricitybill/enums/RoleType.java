package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

@Getter
public enum RoleType implements BaseEnum {
    ADMIN(1.0, "系统管理员"),
    OPERATOR(2.0, "操作员"),
            ;
    @EnumValue
    final
    double value;
    final String desc;

    RoleType(Double value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static RoleType of(Double value) {
        for (RoleType type : RoleType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_ROLE_TYPE);
    }
}
