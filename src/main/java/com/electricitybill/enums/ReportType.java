package com.electricitybill.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.electricitybill.constants.Constant;
import com.electricitybill.expcetions.BadRequestException;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter

public enum ReportType implements BaseEnum{
    DAILY(1, "daily"),
    MONTHLY(2, "monthly"),
    YEARLY(3, "yearly")
    ;
    @EnumValue
    final int value;
    final String desc;

    ReportType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static ReportType of(Integer value) {
        for (ReportType type : ReportType.values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new BadRequestException(Constant.INVALID_Report_TYPE);
    }
    public static List<String> getReportTypeList() {
        ArrayList<String> ReportTypeList = new ArrayList<>();
        for (ReportType type : ReportType.values()) {
            //按照顺序添加
            ReportTypeList.add(type.getDesc());
        }
        return ReportTypeList;
    }

}
