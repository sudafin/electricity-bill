package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public enum BillType implements BaseEnum{
    UNPAID(0, "未支付"),
    PAID(1, "已支付"),
    OVERDUE(2, "已过期"),
    ;
    @EnumValue
    final int value;
    final String desc;

    BillType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static BillType of(Integer value) {
        for (BillType type : BillType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_USER_TYPE);
    }
    public static List<String> getUserTypeList() {
        ArrayList<String> userTypeList = new ArrayList<>();
        for (BillType type : BillType.values()) {
            //按照顺序添加
            userTypeList.add(type.getDesc());
        }
        return userTypeList;
    }
}
