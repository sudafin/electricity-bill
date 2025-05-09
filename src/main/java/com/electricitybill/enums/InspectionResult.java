package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public enum InspectionResult implements BaseEnum {
    NORMAL(0, "normal", "正常"),
    FAULT(1, "fault", "故障"),
    FIXED(2, "fixed", "已修复");

    @EnumValue
    private final int value;
    private final String code;
    private final String desc;

    InspectionResult(int value, String code, String desc) {
        this.value = value;
        this.code = code;
        this.desc = desc;
    }

    public static InspectionResult of(Integer value) {
        for (InspectionResult result : InspectionResult.values()) {
            if (Objects.equals(result.getValue(), value)) {
                return result;
            }
        }
        throw new BadRequestException(Constant.INVALID_INSPECTION_RESULT);
    }
    public static List<String> getInspectionResultList() {
        return Arrays.stream(InspectionResult.values()).map(InspectionResult::getDesc).collect(Collectors.toList());
    }
}