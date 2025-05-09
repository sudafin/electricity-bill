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
public enum UserTaskType implements BaseEnum {
    //缴费提醒和账单提醒
    PAYMENT_REMINDER(101, "缴费提醒"),
    BILL_REMINDER(201, "账单提醒");
    @EnumValue
    private final int value;  // 数据库存储的值
    private final String desc; // 描述

    UserTaskType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static UserTaskType of(Integer value) {
        for (UserTaskType status : UserTaskType.values()) {
            if (Objects.equals(status.getValue(), value)) {
                return status;
            }
        }
        throw new BadRequestException(Constant.INVALID_INSPECTION_STATUS);
    }


    public static List<String> getInspectionStatusList() {
        return Arrays.stream(UserTaskType.values()).map(UserTaskType::getDesc).collect(Collectors.toList());
    }
}