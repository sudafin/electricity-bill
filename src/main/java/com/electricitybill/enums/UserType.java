package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum UserType implements BaseEnum{
    RESIDENT(1, "居民用户"),
    BUSINESSES(2, "商业用户 "),
    ;
    @EnumValue
    final
    int value;
    String desc;

    UserType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static UserType of(int value) {
        for (UserType type : UserType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_USER_TYPE);
    }
    public static List<String> getUserTypeList() {
        ArrayList<String> userTypeList = new ArrayList<>();
        for (UserType type : UserType.values()) {
            //按照顺序添加
            userTypeList.add(type.getDesc());
        }
        return userTypeList;
    }
}
