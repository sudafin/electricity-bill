package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public enum MeterModelType implements BaseEnum{
    A_MODEL(0, "A型号"),
    B_MODEL(1, "B型号"),
    ;
    @EnumValue
    final int value;
    final String desc;

    MeterModelType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static MeterModelType of(Integer value) {
        for (MeterModelType type : MeterModelType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_USER_TYPE);
    }
    public static List<String> getModelTypeList() {
        ArrayList<String> userTypeList = new ArrayList<>();
        for (MeterModelType type : MeterModelType.values()) {
            //按照顺序添加
            userTypeList.add(type.getDesc());
        }
        return userTypeList;
    }
}
