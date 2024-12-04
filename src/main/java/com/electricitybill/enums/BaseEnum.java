package com.electricitybill.enums;

public interface BaseEnum {
    double getValue();
    String getDesc();

    default boolean equalsValue(Double value){
        if (value == null) {
            return false;
        }
        return getValue() == value;
    }
}
