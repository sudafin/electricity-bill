package com.electricitybill.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatusType {
    NORMAL(1.0, "正常"),
    UNPAID(2.0, "欠费"),
    ;
    private final double value;
    private final String desc;

    public static UserStatusType of(Double value) {
        if (value == null) {
            return null;
        }
        for (UserStatusType userStatusType : values()) {
            if (userStatusType.getValue() == value) {
                return userStatusType;
            }
        }
        return null;
    }

    public static String desc(Double value) {
        UserStatusType status = of(value);
        return status.getDesc();
    }
}
