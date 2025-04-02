package com.electricitybill.enums;

import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 季节类型枚举（用于区分夏季电价政策）
 */
@Getter
public enum DateType {
    DAILY(1, "日"),
    WEEKLY(2, "周"),
    MONTHLY(3, "月"),
    QUARTERLY(4, "季度"),
    YEARLY(5, "年");

    private final int value;
    private final String desc;

    DateType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }



    /**
     * 根据月份获取季节类型
     */
    public static DateType of(String desc) {
        return Arrays.stream(values()).filter(type -> Objects.equals(type.getDesc(), desc)).findFirst().orElseThrow(() -> new BadRequestException(Constant.INVALID_DATE_TYPE));
    }


}