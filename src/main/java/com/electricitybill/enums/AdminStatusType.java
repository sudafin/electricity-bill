package com.electricitybill.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdminStatusType implements BaseEnum{
    DISABLE(0, "禁用"),
    ENABLE(1, "启用"),
    ;
    private final double value;
    private final String desc;

    public static AdminStatusType of(Double value) {
        if (value == null) {
            return null;
        }
        for (AdminStatusType adminStatusType : values()) {
            if (adminStatusType.getValue() == value) {
                return adminStatusType;
            }
        }
        return null;
    }

    public static String desc(Double value) {
        AdminStatusType status = of(value);
        return status.getDesc();
    }
}