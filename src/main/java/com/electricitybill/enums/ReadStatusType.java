package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public enum ReadStatusType implements BaseEnum{
    UNREAD(0, "未读"),
    READ(1, "已读")
    ;
    @EnumValue
    final int value;
    final String desc;

    ReadStatusType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static ReadStatusType of(Integer value) {
        for (ReadStatusType type : ReadStatusType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_USER_TYPE);
    }
    public static List<String> getUserTypeList() {
        ArrayList<String> userTypeList = new ArrayList<>();
        for (ReadStatusType type : ReadStatusType.values()) {
            //按照顺序添加
            userTypeList.add(type.getDesc());
        }
        return userTypeList;
    }
}
