package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.dto.rate.RateCacheDTO;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public enum UserType implements BaseEnum{
    RESIDENT(1, "居民用户", false, false),

    COMMERCIAL_INDUSTRIAL(2, "工商业用户", true, false),

    AGRICULTURAL(3, "农业用户", false, true);

    @EnumValue
    private final int value;
    private final String desc;
    private final boolean enableSummerPeak; // 夏季尖峰电价
    private final boolean isAgricultural;   // 是否享受农业优惠


    UserType(int value, String desc, boolean enableSummerPeak, boolean isAgricultural) {
        this.value = value;
        this.desc = desc;
        this.enableSummerPeak = enableSummerPeak;
        this.isAgricultural = isAgricultural;
    }

    /**
     * 获取用户类型描述列表（用于前端展示）
     */
    public static List<String> getUserTypeList() {
        return Arrays.stream(values()).map(UserType::getDesc).collect(Collectors.toList());
    }

    /**
     * 根据value值匹配枚举
     */
    public static UserType of(String value) {
        return Arrays.stream(values()).filter(type -> Objects.equals(type.getDesc(), value)).findFirst().orElseThrow(() -> new BadRequestException(Constant.INVALID_USER_TYPE));
    }

}