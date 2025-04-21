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
public enum TaskType implements BaseEnum {
    USER_TASK(1, "用户任务"),
    SYSTEM_TASK(2, "系统任务");

    @EnumValue
    private final int value;  // 数据库存储的值
    private final String desc; // 描述

    TaskType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static TaskType of(Integer value) {
        for (TaskType status : TaskType.values()) {
            if (Objects.equals(status.getValue(), value)) {
                return status;
            }
        }
        throw new BadRequestException(Constant.INVALID_INSPECTION_STATUS);
    }


    public static List<String> getInspectionStatusList() {
        return Arrays.stream(TaskType.values()).map(TaskType::getDesc).collect(Collectors.toList());
    }
}