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
public enum AccountStatus implements BaseEnum {
    NORMAL(1, "正常"),
    ARREARS(2, "欠费"),
    DISABLED(3, "停用");

    @EnumValue
    private final int value;  // 数据库存储的值
    private final String desc; // 描述

    AccountStatus(int value,  String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static AccountStatus of(Integer value) {
        for (AccountStatus status : AccountStatus.values()) {
            if (Objects.equals(status.getValue(), value)) {
                return status;
            }
        }
        throw new BadRequestException(Constant.INVALID_INSPECTION_STATUS);
    }


    public static List<String> getInspectionStatusList() {
        return Arrays.stream(AccountStatus.values()).map(AccountStatus::getDesc).collect(Collectors.toList());
    }
}