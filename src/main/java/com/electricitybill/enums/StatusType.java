package com.electricitybill.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusType implements BaseEnum{
    DISABLE(0, "禁用"),
    ENABLE(1, "启用"),
    ;
    private final int value;
    private final String desc;

    public static StatusType of(Integer value) {
        if (value == null) {
            return null;
        }
        for (StatusType statusType : values()) {
            if (statusType.getValue() == value) {
                return statusType;
            }
        }
        return null;
    }

    public static String desc(Integer value) {
        StatusType status = of(value);
        return status.getDesc();
    }
}