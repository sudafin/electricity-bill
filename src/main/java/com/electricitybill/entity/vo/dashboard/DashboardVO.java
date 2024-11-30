package com.electricitybill.entity.vo.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardVO {
    /**
     * 用户总数
     */
    private Long totalUser;
    /**
     * 总用电量
     */
    private Long totalElectricityUsage;
    /**
     * 总收入
     */
    private Long totalAmount;
    /**
     * 账单总数
     */
    private Long totalPaymentBill;

    /**
     * 最近一周用电量分布
     */
    List<Double> electricityWeekUsageList;

    /**
    用户类型分布
     */
    Map<String,Integer> userTypeMap;
}
