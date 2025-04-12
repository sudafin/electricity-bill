package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public enum FeedbackStatusType implements BaseEnum{
    UNPROCESSED(0, "未处理"),
    PROCESSING(1, "正在处理"),
    PROCESSED(2, "已经处理")
    ;
    @EnumValue
    final int value;
    final String desc;

    FeedbackStatusType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static FeedbackStatusType of(Integer value) {
        for (FeedbackStatusType type : FeedbackStatusType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_USER_TYPE);
    }
    public static List<String> getUserTypeList() {
        ArrayList<String> userTypeList = new ArrayList<>();
        for (FeedbackStatusType type : FeedbackStatusType.values()) {
            //按照顺序添加
            userTypeList.add(type.getDesc());
        }
        return userTypeList;
    }
}
