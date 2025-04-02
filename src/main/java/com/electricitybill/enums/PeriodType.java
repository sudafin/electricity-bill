package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public enum PeriodType implements BaseEnum{
    PEAK(1, "峰时段"),        // 峰时段
    FLAT(2, "平时段"),        // 平时段
    VALLEY(3, "谷时段"),      // 谷时段
    SUMMER_PEAK(4, "夏季尖峰时段")  // 尖峰时段（仅工业夏季有效）
    ;
    @EnumValue
    private final int value;
    private final String desc;

    PeriodType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    public static PeriodType of(String value) {
        return Arrays.stream(values()).filter(type -> Objects.equals(type.getDesc(), value)).findFirst().orElseThrow(() -> new BadRequestException(Constant.INVALID_PERIOD_TYPE));
    }
}