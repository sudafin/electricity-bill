package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public enum PaymentType implements BaseEnum{
    UNPAID(0, "未支付"),
    PAID(1, "已支付"),
    REFUND(2, "已退款"),
    FAILED(3, "支付失败");
    @EnumValue
    final int value;
    final String desc;

    PaymentType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static PaymentType of(Integer value) {
        for (PaymentType type : PaymentType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_USER_TYPE);
    }
    public static List<String> getUserTypeList() {
        ArrayList<String> userTypeList = new ArrayList<>();
        for (PaymentType type : PaymentType.values()) {
            //按照顺序添加
            userTypeList.add(type.getDesc());
        }
        return userTypeList;
    }
}
