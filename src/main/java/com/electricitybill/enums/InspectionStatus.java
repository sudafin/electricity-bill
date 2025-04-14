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
public enum InspectionStatus implements BaseEnum {
    PENDING(0, "pending", "待处理"),
    PROCESSING(1, "processing", "处理中"),
    COMPLETED(2, "completed", "已完成");

    @EnumValue
    private final int value;  // 数据库存储的值
    private final String code; // 英文编码
    private final String desc; // 描述

    InspectionStatus(int value, String code, String desc) {
        this.value = value;
        this.code = code;
        this.desc = desc;
    }

    public static InspectionStatus of(Integer value) {
        for (InspectionStatus status : InspectionStatus.values()) {
            if (Objects.equals(status.getValue(), value)) {
                return status;
            }
        }
        throw new BadRequestException(Constant.INVALID_INSPECTION_STATUS);
    }

    public static InspectionStatus fromCode(String code) {
        for (InspectionStatus status : InspectionStatus.values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new BadRequestException(Constant.INVALID_INSPECTION_STATUS);
    }

    public static List<String> getInspectionStatusList() {
        return Arrays.stream(InspectionStatus.values()).map(InspectionStatus::getDesc).collect(Collectors.toList());
    }
}