package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public enum FeedbackType implements BaseEnum {
    COMPLAINT(0, "投诉"),
    SUGGESTION(1, "建议"),
    QUESTION(2, "问题");

    @EnumValue
    final int value;
    final String desc;

    FeedbackType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static FeedbackType of(Integer value) {
        for (FeedbackType type : FeedbackType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_FEEDBACK_TYPE);
    }

    public static List<String> getFeedbackTypeList() {
        ArrayList<String> feedbackTypeList = new ArrayList<>();
        for (FeedbackType type : FeedbackType.values()) {
            feedbackTypeList.add(type.getDesc());
        }
        return feedbackTypeList;
    }
}