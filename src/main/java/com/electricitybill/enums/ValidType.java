package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public enum ValidType implements BaseEnum{
    VALID(0, "无效"),
    INVALID(1, "有效")
    ;
    @EnumValue
    final int value;
    final String desc;

    ValidType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static ValidType of(Integer value) {
        for (ValidType type : ValidType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_USER_TYPE);
    }
    public static List<String> getUserTypeList() {
        ArrayList<String> userTypeList = new ArrayList<>();
        for (ValidType type : ValidType.values()) {
            //按照顺序添加
            userTypeList.add(type.getDesc());
        }
        return userTypeList;
    }
}
