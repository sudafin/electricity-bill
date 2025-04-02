package com.electricitybill.enums;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

/**
 * 季节类型枚举（用于区分夏季电价政策）
 */
@Getter
public enum SeasonType {
    SUMMER(1, "夏季", 
            Arrays.asList(Month.JUNE, Month.JULY, Month.AUGUST, Month.SEPTEMBER), 
            "6-9月执行夏季电价政策"),
    NON_SUMMER(2, "非夏季", 
            Arrays.asList(Month.JANUARY, Month.FEBRUARY, Month.MARCH, 
                         Month.APRIL, Month.MAY, Month.OCTOBER, 
                         Month.NOVEMBER, Month.DECEMBER), 
            "其他月份执行常规电价");

    private final int value;
    private final String desc;
    private final List<Month> months;  // 包含的月份
    private final String policyNote;    // 政策说明

    SeasonType(int value, String desc, List<Month> months, String policyNote) {
        this.value = value;
        this.desc = desc;
        this.months = months;
        this.policyNote = policyNote;
    }

    /**
     * 根据判断是否夏季
     */
    public static boolean isSummerSeason(LocalDateTime localDateTime) {
        return SUMMER.getMonths().contains(localDateTime.getMonth());
    }

    /**
     * 根据月份获取季节类型
     */
    public static SeasonType of(Month month) {
        return SUMMER.getMonths().contains(month) ? SUMMER : NON_SUMMER;
    }

    /**
     * 获取季节政策说明
     */
    public static String getSeasonPolicy() {
        return "广东省季节划分：\n" + 
               SUMMER.getDesc() + "：" + SUMMER.getPolicyNote() + "\n" +
               NON_SUMMER.getDesc() + "：" + NON_SUMMER.getPolicyNote();
    }
}