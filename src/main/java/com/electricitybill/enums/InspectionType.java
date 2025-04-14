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
public enum InspectionType implements BaseEnum {
    ROUTINE(0, "routine", "常规检查"),
    FAULT(1, "fault", "故障检修"),
    CALIBRATION(2, "calibration", "校准");

    @EnumValue
    private final int value;
    private final String code;
    private final String desc;

    InspectionType(int value, String code, String desc) {
        this.value = value;
        this.code = code;
        this.desc = desc;
    }

    public static InspectionType of(Integer value) {
        for (InspectionType type : InspectionType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_INSPECTION_TYPE);
    }

    public static InspectionType fromCode(String code) {
        for (InspectionType type : InspectionType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_INSPECTION_TYPE);
    }
    public static List<String> getInspectionTypeList() {
        return Arrays.stream(InspectionType.values()).map(InspectionType::getDesc).collect(Collectors.toList());
    }
}