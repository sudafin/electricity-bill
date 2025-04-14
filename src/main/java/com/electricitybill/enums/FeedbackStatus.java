package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public enum FeedbackStatus implements BaseEnum {
    PENDING(0, "待处理"),
    PROCESSED(1, "已处理"),
    CLOSED(2, "已关闭");

    @EnumValue
    final int value;
    final String desc;

    FeedbackStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static FeedbackStatus of(Integer value) {
        for (FeedbackStatus status : FeedbackStatus.values()) {
            if (Objects.equals(status.getValue(), value)) {
                return status;
            }
        }
        throw new BadRequestException(Constant.FEEDBACK_STATUS_NOT_EXIST);
    }

    public static List<String> getFeedbackStatusList() {
        ArrayList<String> feedbackStatusList = new ArrayList<>();
        for (FeedbackStatus status : FeedbackStatus.values()) {
            feedbackStatusList.add(status.getDesc());
        }
        return feedbackStatusList;
    }
}