package com.electricitybill.entity.vo.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardVO {
    /**
     * 当月新增用户总数
     */
    private Long currentMonthlyAddingUserTotal;
    /**
     * 总用电量
     */
    private Long currentMonthlyElectricityUsageTotal;
    /**
     * 总收入
     */
    private BigDecimal currentMonthlyAmountTotal;
    /**
     * 当月欠费账单总数
     */
    private Long currentMonthlyDebtBillTotal;

    /**
     * 用户类型分布
     */
    private Map<String, Long> userTypeMap;

    /**
     * 已处理反馈数量
     */
    private Long processedFeedbackCount;

    /**
     * 未处理反馈数量
     */
    private Long unprocessedFeedbackCount;

    /**
     * 系统日志数量
     */
    private Long systemLogCount;


    private Long totalReconciliation;
}
