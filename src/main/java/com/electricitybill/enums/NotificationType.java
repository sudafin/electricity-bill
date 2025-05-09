package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public enum NotificationType implements BaseEnum{
    INTERNAL_NOTIFICATION(0, "内部通知"),
    ANNOUNCEMENT_NOTIFICATION(1, "公告通知"),
    FEEDBACK_NOTIFICATION(2, "反馈通知"),
    ;
    @EnumValue
    final int value;
    final String desc;

    NotificationType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static NotificationType of(Integer value) {
        for (NotificationType type : NotificationType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_USER_TYPE);
    }
    public static List<String> getUserTypeList() {
        ArrayList<String> userTypeList = new ArrayList<>();
        for (NotificationType type : NotificationType.values()) {
            //按照顺序添加
            userTypeList.add(type.getDesc());
        }
        return userTypeList;
    }
}
