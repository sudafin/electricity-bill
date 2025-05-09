package com.electricitybill.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.entity.po.*;
import com.electricitybill.service.*; // Import all your IService interfaces
import com.electricitybill.utils.DateUtils; // Corrected import name
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils; // Use Spring's CollectionUtils

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor // Use constructor injection
public class StatisticsServiceImpl implements StatisticsService {

    // Inject necessary entity services
    private final IEbUserService ebUserService;
    private final IEbBillService ebBillService;
    private final IEbElectricityUsageService ebElectricityUsageService;
    private final IEbUserFeedbackService ebUserFeedbackService;
    private final IEbReconciliationService ebReconciliationService;
    // Add other services like IEbRateService if needed

    // Date Formatters
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER_DAILY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMATTER_MONTHLY = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_FORMATTER_YEARLY = DateTimeFormatter.ofPattern("yyyy");

    // Pattern for basic region extraction (adjust as needed)
    // Matches 省, 市, 自治区, or common 区/县 patterns. Prioritizes smaller units if multiple found.
    private static final Pattern REGION_PATTERN = Pattern.compile(
            "([^省]+自治区|[^市]+市|[^县]+县|[^区]+区|[^省]+省)" // Matches common administrative units
    );


    // --- Electricity Statistics ---
    @Override
    public JSONObject getElectricityStatistics(String granularity, String startDateStr, String endDateStr) {
        JSONObject result = new JSONObject();
        JSONObject summary = new JSONObject();
        JSONArray timeSeries = new JSONArray();
        JSONArray tableData = new JSONArray(); // Optional detailed table data

        LocalDateTime startDateTime = parseDateTime(startDateStr);
        LocalDateTime endDateTime = parseDateTime(endDateStr);
        if (startDateTime == null || endDateTime == null) {
            log.warn("Invalid date range provided for electricity statistics.");
            result.put("summary", summary);
            result.put("timeSeries", timeSeries);
            return result;
        }

        // 1. Fetch usage data within the date range using IEbElectricityUsageService
        LambdaQueryWrapper<EbElectricityUsage> usageWrapper = new LambdaQueryWrapper<>();
        usageWrapper.between(EbElectricityUsage::getEndTime, startDateTime, endDateTime) // Assuming end_time is relevant
                .select(EbElectricityUsage::getUsageAmount, EbElectricityUsage::getEndTime, EbElectricityUsage::getPeriodType); // Select necessary fields
        List<EbElectricityUsage> usageList = ebElectricityUsageService.list(usageWrapper);

        if (CollectionUtils.isEmpty(usageList)) {
            log.info("No electricity usage data found for the period.");
            result.put("summary", summary);
            result.put("timeSeries", timeSeries);
            return result;
        }

        // 2. Aggregate data based on granularity
        Map<String, JSONObject> aggregatedData = aggregateUsageByGranularity(usageList, granularity, startDateTime, endDateTime);

        // 3. Prepare timeSeries array for chart
        List<String> dates = new ArrayList<>(aggregatedData.keySet()).stream().sorted().collect(Collectors.toList());
        JSONArray totalUsageValues = new JSONArray();
        JSONArray peakUsageValues = new JSONArray();
        JSONArray flatUsageValues = new JSONArray();
        JSONArray valleyUsageValues = new JSONArray();

        BigDecimal totalUsageSum = BigDecimal.ZERO;
        BigDecimal totalPeakUsage = BigDecimal.ZERO;
        BigDecimal totalFlatUsage = BigDecimal.ZERO;
        BigDecimal totalValleyUsage = BigDecimal.ZERO;

        for (String dateKey : dates) {
            JSONObject dailyData = aggregatedData.get(dateKey);
            BigDecimal dayTotal = dailyData.getBigDecimal("total_usage");
            BigDecimal dayPeak = dailyData.getBigDecimal("peak_usage");
            BigDecimal dayFlat = dailyData.getBigDecimal("flat_usage");
            BigDecimal dayValley = dailyData.getBigDecimal("valley_usage");

            totalUsageValues.add(dayTotal);
            peakUsageValues.add(dayPeak);
            flatUsageValues.add(dayFlat);
            valleyUsageValues.add(dayValley);

            totalUsageSum = totalUsageSum.add(dayTotal);
            totalPeakUsage = totalPeakUsage.add(dayPeak);
            totalFlatUsage = totalFlatUsage.add(dayFlat);
            totalValleyUsage = totalValleyUsage.add(dayValley);

            // Populate optional table data
            JSONObject tableRow = new JSONObject();
            tableRow.put("date", dateKey);
            tableRow.put("total_usage", dayTotal);
            tableRow.put("peak_usage", dayPeak);
            tableRow.put("flat_usage", dayFlat);
            tableRow.put("valley_usage", dayValley);
            tableData.add(tableRow);
        }

        // 4. Prepare summary object
        summary.put("total_usage", totalUsageSum);
        summary.put("peak_usage", totalPeakUsage);
        summary.put("flat_usage", totalFlatUsage);
        summary.put("valley_usage", totalValleyUsage);
        summary.put("average_usage", dates.isEmpty() ? BigDecimal.ZERO :
                totalUsageSum.divide(BigDecimal.valueOf(dates.size()), 2, RoundingMode.HALF_UP));
        // TODO: Calculate usage_growth_rate (requires data from previous period)
        summary.put("usage_growth_rate", BigDecimal.ZERO); // Placeholder

        // 5. Construct final result JSON
        JSONObject timeSeriesData = new JSONObject();
        timeSeriesData.put("dates", dates);
        timeSeriesData.put("total_usage", totalUsageValues); // Corresponds to 'electricity' in frontend component
        timeSeriesData.put("peak_usage", peakUsageValues);
        timeSeriesData.put("flat_usage", flatUsageValues);
        timeSeriesData.put("valley_usage", valleyUsageValues);

        result.put("summary", summary);
        result.put("timeSeries", timeSeriesData); // Assuming frontend expects structured time series data
        result.put("tableData", tableData); // Include if needed

        return result;
    }

    // Helper to aggregate usage data
    private Map<String, JSONObject> aggregateUsageByGranularity(List<EbElectricityUsage> usageList, String granularity, LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter formatter = getFormatterByGranularity(granularity);
        Map<String, JSONObject> aggregated = new HashMap<>();

        // Initialize map with all dates/months/years in the range
        List<String> dateKeysInRange = DateUtils.getDateKeysInRange(start, end, granularity, formatter);
        for (String key : dateKeysInRange) {
            JSONObject initialData = new JSONObject();
            initialData.put("total_usage", BigDecimal.ZERO);
            initialData.put("peak_usage", BigDecimal.ZERO);
            initialData.put("flat_usage", BigDecimal.ZERO);
            initialData.put("valley_usage", BigDecimal.ZERO);
            aggregated.put(key, initialData);
        }


        for (EbElectricityUsage usage : usageList) {
            String dateKey = usage.getEndTime().format(formatter);

            // Only aggregate if the dateKey is within our initialized range
            if (aggregated.containsKey(dateKey)) {
                JSONObject dayData = aggregated.get(dateKey);
                BigDecimal currentTotal = dayData.getBigDecimal("total_usage");
                dayData.put("total_usage", currentTotal.add(usage.getUsageAmount()));

                String periodType = usage.getPeriodType().toLowerCase();
                switch (periodType) {
                    case "peak":
                        BigDecimal currentPeak = dayData.getBigDecimal("peak_usage");
                        dayData.put("peak_usage", currentPeak.add(usage.getUsageAmount()));
                        break;
                    case "flat":
                        BigDecimal currentFlat = dayData.getBigDecimal("flat_usage");
                        dayData.put("flat_usage", currentFlat.add(usage.getUsageAmount()));
                        break;
                    case "valley":
                        BigDecimal currentValley = dayData.getBigDecimal("valley_usage");
                        dayData.put("valley_usage", currentValley.add(usage.getUsageAmount()));
                        break;
                }
            }
        }
        return aggregated;
    }


    // --- Fee Statistics ---
    @Override
    public JSONObject getFeeStatistics(String granularity, String startDateStr, String endDateStr) {
        JSONObject result = new JSONObject();
        JSONObject summary = new JSONObject();
        JSONArray timeSeries = new JSONArray();
        JSONArray tableData = new JSONArray(); // Optional

        LocalDateTime startDateTime = parseDateTime(startDateStr);
        LocalDateTime endDateTime = parseDateTime(endDateStr);
        if (startDateTime == null || endDateTime == null) {
            log.warn("Invalid date range provided for fee statistics.");
            result.put("summary", summary);
            result.put("timeSeries", timeSeries);
            return result;
        }

        // 1. Fetch bill data
        LambdaQueryWrapper<EbBill> billWrapper = new LambdaQueryWrapper<>();
        // Assuming 'created_at' or 'due_date' determines the period. Let's use created_at.
        billWrapper.between(EbBill::getCreatedAt, startDateTime, endDateTime)
                .select(EbBill::getTotalAmount, EbBill::getCreatedAt, EbBill::getStatus);
        List<EbBill> billList = ebBillService.list(billWrapper);

        if (CollectionUtils.isEmpty(billList)) {
            log.info("No bill data found for the period.");
            result.put("summary", summary);
            result.put("timeSeries", timeSeries);
            return result;
        }

        // 2. Aggregate fee data
        Map<String, JSONObject> aggregatedData = aggregateFeeByGranularity(billList, granularity, startDateTime, endDateTime);

        // 3. Prepare timeSeries
        List<String> dates = new ArrayList<>(aggregatedData.keySet()).stream().sorted().collect(Collectors.toList());
        JSONArray totalAmountValues = new JSONArray();
        JSONArray paidAmountValues = new JSONArray(); // Example: add if needed
        JSONArray unpaidAmountValues = new JSONArray(); // Example: add if needed

        BigDecimal totalAmountSum = BigDecimal.ZERO;
        BigDecimal totalPaidSum = BigDecimal.ZERO; // Example
        long totalBillsCount = 0;
        long paidBillsCount = 0;
        long overdueBillsCount = 0; // Example

        for (String dateKey : dates) {
            JSONObject periodData = aggregatedData.get(dateKey);
            BigDecimal periodTotal = periodData.getBigDecimal("total_amount");
            BigDecimal periodPaid = periodData.getBigDecimal("paid_amount");
            long periodBillCount = periodData.getLongValue("bill_count");
            long periodPaidCount = periodData.getLongValue("paid_count");
            long periodOverdueCount = periodData.getLongValue("overdue_count");

            totalAmountValues.add(periodTotal);
            paidAmountValues.add(periodPaid); // Populate if calculated
            unpaidAmountValues.add(periodTotal.subtract(periodPaid)); // Populate if needed

            totalAmountSum = totalAmountSum.add(periodTotal);
            totalPaidSum = totalPaidSum.add(periodPaid);
            totalBillsCount += periodBillCount;
            paidBillsCount += periodPaidCount;
            overdueBillsCount += periodOverdueCount;

            // Populate optional table data
            JSONObject tableRow = new JSONObject();
            tableRow.put("date", dateKey);
            tableRow.put("total_amount", periodTotal);
            tableRow.put("paid_amount", periodPaid);
            tableRow.put("unpaid_amount", periodTotal.subtract(periodPaid));
            tableRow.put("payment_rate", periodBillCount == 0 ? 0 :
                    BigDecimal.valueOf(periodPaidCount)
                            .divide(BigDecimal.valueOf(periodBillCount), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))); // Rate as percentage
            tableData.add(tableRow);
        }

        // 4. Prepare summary
        summary.put("total_amount", totalAmountSum);
        summary.put("average_amount", dates.isEmpty() ? BigDecimal.ZERO :
                totalAmountSum.divide(BigDecimal.valueOf(dates.size()), 2, RoundingMode.HALF_UP));
        summary.put("total_bills", totalBillsCount);
        summary.put("paid_bills", paidBillsCount);
        summary.put("overdue_bills", overdueBillsCount); // Assuming calculated
        summary.put("payment_rate", totalBillsCount == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(paidBillsCount)
                        .divide(BigDecimal.valueOf(totalBillsCount), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))); // Overall rate
        // TODO: Calculate revenue_growth_rate
        summary.put("revenue_growth_rate", BigDecimal.ZERO); // Placeholder

        // 5. Construct final result
        JSONObject timeSeriesData = new JSONObject();
        timeSeriesData.put("dates", dates);
        timeSeriesData.put("total_amount", totalAmountValues); // Corresponds to 'fee' or 'revenues' in frontend
        timeSeriesData.put("paid_amount", paidAmountValues); // Example extra data

        result.put("summary", summary);
        result.put("timeSeries", timeSeriesData);
        result.put("tableData", tableData); // Optional

        return result;
    }

    // Helper to aggregate fee data
    private Map<String, JSONObject> aggregateFeeByGranularity(List<EbBill> billList, String granularity, LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter formatter = getFormatterByGranularity(granularity);
        Map<String, JSONObject> aggregated = new HashMap<>();

        List<String> dateKeysInRange = DateUtils.getDateKeysInRange(start, end, granularity, formatter);
        for (String key : dateKeysInRange) {
            JSONObject initialData = new JSONObject();
            initialData.put("total_amount", BigDecimal.ZERO);
            initialData.put("paid_amount", BigDecimal.ZERO);
            initialData.put("bill_count", 0L);
            initialData.put("paid_count", 0L);
            initialData.put("overdue_count", 0L);
            aggregated.put(key, initialData);
        }

        for (EbBill bill : billList) {
            String dateKey = bill.getCreatedAt().format(formatter);

            if (aggregated.containsKey(dateKey)) {
                JSONObject periodData = aggregated.get(dateKey);
                periodData.put("total_amount", periodData.getBigDecimal("total_amount").add(bill.getTotalAmount()));
                periodData.put("bill_count", periodData.getLongValue("bill_count") + 1);

                // Status: "未支付", "已支付", "逾期"
                String status = bill.getStatus();
                if ("已支付".equals(status)) {
                    periodData.put("paid_amount", periodData.getBigDecimal("paid_amount").add(bill.getTotalAmount()));
                    periodData.put("paid_count", periodData.getLongValue("paid_count") + 1);
                } else if ("逾期".equals(status)) {
                    periodData.put("overdue_count", periodData.getLongValue("overdue_count") + 1);
                }
            }
        }
        return aggregated;
    }


    // --- Feedback Statistics ---
    @Override
    public JSONObject getFeedbackStatistics(String granularity, String startDateStr, String endDateStr) {
        JSONObject result = new JSONObject();
        JSONObject summary = new JSONObject();
        JSONArray timeSeries = new JSONArray();
        JSONArray tableData = new JSONArray(); // For recent feedback list

        LocalDateTime startDateTime = parseDateTime(startDateStr);
        LocalDateTime endDateTime = parseDateTime(endDateStr);
        if (startDateTime == null || endDateTime == null) {
            log.warn("Invalid date range provided for feedback statistics.");
            result.put("summary", summary);
            result.put("timeSeries", timeSeries);
            result.put("tableData", tableData);
            return result;
        }

        // 1. Fetch feedback data
        LambdaQueryWrapper<EbUserFeedback> feedbackWrapper = new LambdaQueryWrapper<>();
        feedbackWrapper.between(EbUserFeedback::getSubmitTime, startDateTime, endDateTime)
                .select(EbUserFeedback::getId, EbUserFeedback::getUserId, EbUserFeedback::getFeedbackType,
                        EbUserFeedback::getFeedbackStatus, EbUserFeedback::getSubmitTime,
                        EbUserFeedback::getProcessTime, EbUserFeedback::getProcessorId, EbUserFeedback::getContent); // Select fields for table too
        List<EbUserFeedback> feedbackList = ebUserFeedbackService.list(feedbackWrapper);

        // Also get recent feedback for the table (e.g., last 10)
        LambdaQueryWrapper<EbUserFeedback> recentFeedbackWrapper = new LambdaQueryWrapper<>();
        recentFeedbackWrapper.between(EbUserFeedback::getSubmitTime, startDateTime, endDateTime)
                .orderByDesc(EbUserFeedback::getSubmitTime)
                .last("LIMIT 10"); // Get latest 10 for table display
        List<EbUserFeedback> recentFeedbacks = ebUserFeedbackService.list(recentFeedbackWrapper);
        tableData.addAll(recentFeedbacks);


        if (CollectionUtils.isEmpty(feedbackList)) {
            log.info("No feedback data found for the period.");
            result.put("summary", summary);
            result.put("timeSeries", timeSeries);
            result.put("tableData", tableData); // Still return empty table if needed
            return result;
        }

        // 2. Aggregate by granularity and calculate summary stats
        Map<String, JSONObject> aggregatedData = aggregateFeedbackByGranularity(feedbackList, granularity, startDateTime, endDateTime);

        // 3. Prepare timeSeries
        List<String> dates = new ArrayList<>(aggregatedData.keySet()).stream().sorted().collect(Collectors.toList());
        JSONArray totalFeedbackValues = new JSONArray();
        JSONArray pendingFeedbackValues = new JSONArray();
        JSONArray processedFeedbackValues = new JSONArray();

        long totalFeedbackCount = 0;
        long totalPendingCount = 0;
        long totalProcessedCount = 0;
        long totalClosedCount = 0; // Example
        long totalProcessingTimeMillis = 0;
        long processedItemsWithTime = 0;
        Map<String, Long> typeDistribution = new HashMap<>(); // e.g., complaint -> count

        for (String dateKey : dates) {
            JSONObject periodData = aggregatedData.get(dateKey);
            long periodTotal = periodData.getLongValue("total_feedback");
            long periodPending = periodData.getLongValue("pending_count");
            long periodProcessed = periodData.getLongValue("processed_count");

            totalFeedbackValues.add(periodTotal);
            pendingFeedbackValues.add(periodPending);
            processedFeedbackValues.add(periodProcessed);
        }

        // Calculate overall summary stats by iterating through the full list again
        for (EbUserFeedback feedback : feedbackList) {
            totalFeedbackCount++;
            String status = feedback.getFeedbackStatus().toLowerCase();
            String type = feedback.getFeedbackType().toLowerCase();
            typeDistribution.put(type, typeDistribution.getOrDefault(type, 0L) + 1);

            switch (status) {
                case "pending":
                    totalPendingCount++;
                    break;
                case "processed":
                    totalProcessedCount++;
                    if (feedback.getSubmitTime() != null && feedback.getProcessTime() != null) {
                        totalProcessingTimeMillis += ChronoUnit.MILLIS.between(feedback.getSubmitTime(), feedback.getProcessTime());
                        processedItemsWithTime++;
                    }
                    break;
                case "closed":
                    totalClosedCount++;
                    // Include closed in processed count? Depends on definition. Assuming yes for now.
                    // totalProcessedCount++;
                    if (feedback.getSubmitTime() != null && feedback.getProcessTime() != null) {
                        totalProcessingTimeMillis += ChronoUnit.MILLIS.between(feedback.getSubmitTime(), feedback.getProcessTime());
                        processedItemsWithTime++;
                    }
                    break;
            }
        }


        // 4. Prepare summary
        summary.put("total_feedback", totalFeedbackCount);
        summary.put("pending_count", totalPendingCount);
        summary.put("processed_count", totalProcessedCount + totalClosedCount); // Including closed as processed
        summary.put("closed_count", totalClosedCount);
        double avgHours = processedItemsWithTime == 0 ? 0 : (double) totalProcessingTimeMillis / processedItemsWithTime / (1000.0 * 60 * 60);
        summary.put("average_process_time_hours", BigDecimal.valueOf(avgHours).setScale(1, RoundingMode.HALF_UP));
        summary.put("type_distribution", typeDistribution);
        // TODO: Calculate feedback_growth_rate
        summary.put("feedback_growth_rate", BigDecimal.ZERO); // Placeholder

        // 5. Construct final result
        JSONObject timeSeriesData = new JSONObject();
        timeSeriesData.put("dates", dates);
        timeSeriesData.put("total_feedback", totalFeedbackValues);
        timeSeriesData.put("pending_count", pendingFeedbackValues);
        timeSeriesData.put("processed_count", processedFeedbackValues);


        result.put("summary", summary);
        result.put("timeSeries", timeSeriesData);
        result.put("tableData", tableData); // Add recent feedback list


        return result;
    }

    // Helper to aggregate feedback data
    private Map<String, JSONObject> aggregateFeedbackByGranularity(List<EbUserFeedback> feedbackList, String granularity, LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter formatter = getFormatterByGranularity(granularity);
        Map<String, JSONObject> aggregated = new HashMap<>();

        List<String> dateKeysInRange = DateUtils.getDateKeysInRange(start, end, granularity, formatter);
        for (String key : dateKeysInRange) {
            JSONObject initialData = new JSONObject();
            initialData.put("total_feedback", 0L);
            initialData.put("pending_count", 0L);
            initialData.put("processed_count", 0L);
            initialData.put("closed_count", 0L);
            aggregated.put(key, initialData);
        }

        for (EbUserFeedback feedback : feedbackList) {
            String dateKey = feedback.getSubmitTime().format(formatter);
            if (aggregated.containsKey(dateKey)) {
                JSONObject periodData = aggregated.get(dateKey);
                periodData.put("total_feedback", periodData.getLongValue("total_feedback") + 1);
                String status = feedback.getFeedbackStatus().toLowerCase();
                switch (status) {
                    case "pending":
                        periodData.put("pending_count", periodData.getLongValue("pending_count") + 1);
                        break;
                    case "processed":
                        periodData.put("processed_count", periodData.getLongValue("processed_count") + 1);
                        break;
                    case "closed":
                        periodData.put("closed_count", periodData.getLongValue("closed_count") + 1);
                        // Optionally add to processed count for the period chart as well?
                        // periodData.put("processed_count", periodData.getLongValue("processed_count") + 1);
                        break;
                }
            }
        }
        return aggregated;
    }


    // --- Reconciliation Statistics ---
    @Override
    public JSONObject getReconciliationStatistics(String granularity, String startDateStr, String endDateStr) {
        JSONObject result = new JSONObject();
        JSONObject summary = new JSONObject();
        JSONArray timeSeries = new JSONArray();
        JSONArray tableData = new JSONArray(); // For recent records

        LocalDateTime startDateTime = parseDateTime(startDateStr);
        LocalDateTime endDateTime = parseDateTime(endDateStr);
        if (startDateTime == null || endDateTime == null) {
            log.warn("Invalid date range provided for reconciliation statistics.");
            result.put("summary", summary);
            result.put("timeSeries", timeSeries);
            result.put("tableData", tableData);
            return result;
        }

        // 1. Fetch reconciliation data
        LambdaQueryWrapper<EbReconciliation> wrapper = new LambdaQueryWrapper<>();
        // Use created_at for time series aggregation
        wrapper.between(EbReconciliation::getCreatedAt, startDateTime, endDateTime)
                .select(EbReconciliation::getId, EbReconciliation::getUserId, EbReconciliation::getStartDate,
                        EbReconciliation::getEndDate, EbReconciliation::getTotalAmount, EbReconciliation::getStatus,
                        EbReconciliation::getPaymentStatus, EbReconciliation::getApproverId, EbReconciliation::getCreatedAt);
        List<EbReconciliation> reconciliationList = ebReconciliationService.list(wrapper);

        // Fetch recent data for table
        LambdaQueryWrapper<EbReconciliation> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.between(EbReconciliation::getCreatedAt, startDateTime, endDateTime)
                .orderByDesc(EbReconciliation::getCreatedAt)
                .last("LIMIT 10");
        tableData.addAll(ebReconciliationService.list(recentWrapper));


        if (CollectionUtils.isEmpty(reconciliationList)) {
            log.info("No reconciliation data found for the period.");
            result.put("summary", summary);
            result.put("timeSeries", timeSeries);
            result.put("tableData", tableData);
            return result;
        }

        // 2. Aggregate and summarize
        Map<String, JSONObject> aggregatedData = aggregateReconciliationByGranularity(reconciliationList, granularity, startDateTime, endDateTime);

        // 3. Prepare timeSeries
        List<String> dates = new ArrayList<>(aggregatedData.keySet()).stream().sorted().collect(Collectors.toList());
        JSONArray totalCountValues = new JSONArray();
        JSONArray pendingCountValues = new JSONArray();
        JSONArray completedCountValues = new JSONArray();


        BigDecimal totalReconciliationAmount = BigDecimal.ZERO;
        long totalReconciliations = 0;
        long totalPending = 0;
        long totalCompleted = 0;
        Map<String, Long> statusDistribution = new HashMap<>(); // pending -> count, completed -> count

        for (String dateKey : dates) {
            JSONObject periodData = aggregatedData.get(dateKey);
            totalCountValues.add(periodData.getLongValue("total_count"));
            pendingCountValues.add(periodData.getLongValue("pending_count"));
            completedCountValues.add(periodData.getLongValue("completed_count"));
        }

        // Calculate overall summary
        for(EbReconciliation reconciliation : reconciliationList) {
            totalReconciliations++;
            totalReconciliationAmount = totalReconciliationAmount.add(reconciliation.getTotalAmount());
            String status = reconciliation.getStatus().toLowerCase();
            statusDistribution.put(status, statusDistribution.getOrDefault(status, 0L) + 1);
            if ("pending".equals(status)) {
                totalPending++;
            } else if ("completed".equals(status)) {
                totalCompleted++;
            }
        }


        // 4. Prepare summary
        summary.put("total_reconciliations", totalReconciliations);
        summary.put("pending_count", totalPending);
        summary.put("completed_count", totalCompleted);
        summary.put("total_reconciliation_amount", totalReconciliationAmount);
        summary.put("status_distribution", statusDistribution);

        // 5. Construct final result
        JSONObject timeSeriesData = new JSONObject();
        timeSeriesData.put("dates", dates);
        timeSeriesData.put("total_count", totalCountValues);
        timeSeriesData.put("pending_count", pendingCountValues);
        timeSeriesData.put("completed_count", completedCountValues);


        result.put("summary", summary);
        result.put("timeSeries", timeSeriesData);
        result.put("tableData", tableData);

        return result;
    }

    // Helper to aggregate reconciliation data
    private Map<String, JSONObject> aggregateReconciliationByGranularity(List<EbReconciliation> reconciliationList, String granularity, LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter formatter = getFormatterByGranularity(granularity);
        Map<String, JSONObject> aggregated = new HashMap<>();

        List<String> dateKeysInRange = DateUtils.getDateKeysInRange(start, end, granularity, formatter);
        for (String key : dateKeysInRange) {
            JSONObject initialData = new JSONObject();
            initialData.put("total_count", 0L);
            initialData.put("pending_count", 0L);
            initialData.put("completed_count", 0L);
            aggregated.put(key, initialData);
        }


        for (EbReconciliation reconciliation : reconciliationList) {
            String dateKey = reconciliation.getCreatedAt().format(formatter);
            if (aggregated.containsKey(dateKey)) {
                JSONObject periodData = aggregated.get(dateKey);
                periodData.put("total_count", periodData.getLongValue("total_count") + 1);
                String status = reconciliation.getStatus().toLowerCase();
                if ("pending".equals(status)) {
                    periodData.put("pending_count", periodData.getLongValue("pending_count") + 1);
                } else if ("completed".equals(status)) {
                    periodData.put("completed_count", periodData.getLongValue("completed_count") + 1);
                }
            }
        }
        return aggregated;
    }

    // --- User Type Statistics ---
    @Override
    public JSONObject getUserTypeStatistics(String granularity, String startDateStr, String endDateStr) {
        JSONObject result = new JSONObject();
        JSONObject summary = new JSONObject();
        JSONArray distribution = new JSONArray(); // Array of { user_type, user_count, total_usage, ... }
        JSONArray timeSeries = new JSONArray(); // For user growth trend

        LocalDateTime startDateTime = parseDateTime(startDateStr);
        LocalDateTime endDateTime = parseDateTime(endDateStr);
        if (startDateTime == null || endDateTime == null) {
            log.warn("Invalid date range provided for user type statistics.");
            result.put("summary", summary);
            result.put("distribution", distribution);
            result.put("timeSeries", timeSeries);
            return result;
        }


        // 1. Fetch all users (simpler approach, might fetch more than needed)
        LambdaQueryWrapper<EbUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.select(EbUser::getId, EbUser::getUserType, EbUser::getCreatedAt); // Select needed
        List<EbUser> userList = ebUserService.list(userWrapper);

        if (CollectionUtils.isEmpty(userList)) {
            log.info("No user data found.");
            result.put("summary", summary);
            result.put("distribution", distribution);
            result.put("timeSeries", timeSeries);
            return result;
        }

        // 2. Calculate Summary and Distribution
        long totalUsers = userList.size(); // Total users overall
        long residentialUsers = 0;
        long commercialUsers = 0;
        Map<String, JSONObject> distributionMap = new HashMap<>();

        // Initialize distribution map (can fetch types from DB if dynamic)
        distributionMap.put("居民用户", createInitialUserTypeStats("居民用户"));
        distributionMap.put("商业用户", createInitialUserTypeStats("商业用户"));

        for (EbUser user : userList) {
            String userType = user.getUserType(); // e.g., "居民用户", "商业用户"
            if ("居民用户".equals(userType)) {
                residentialUsers++;
            } else if ("商业用户".equals(userType)) {
                commercialUsers++;
            }

            JSONObject typeStats = distributionMap.computeIfAbsent(userType, k -> createInitialUserTypeStats(k));
            typeStats.put("user_count", typeStats.getLongValue("user_count") + 1);
            ((List<Long>)typeStats.get("userIds")).add(user.getId());
        }

        // Fetch usage and fee data per user type (Alternative Implementation)
        for (JSONObject stats : distributionMap.values()) {
            List<Long> userIdsOfType = (List<Long>) stats.get("userIds");
            if (!userIdsOfType.isEmpty()) {
                // --- Calculate Total Usage for this type within the period ---
                LambdaQueryWrapper<EbElectricityUsage> usageQuery = new LambdaQueryWrapper<EbElectricityUsage>()
                        .in(EbElectricityUsage::getUserId, userIdsOfType)
                        .between(EbElectricityUsage::getEndTime, startDateTime, endDateTime);
                List<EbElectricityUsage> usageListForType = ebElectricityUsageService.list(usageQuery);
                BigDecimal totalUsageType = usageListForType.stream()
                        .map(EbElectricityUsage::getUsageAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // --- Calculate Total Fee for this type within the period ---
                LambdaQueryWrapper<EbBill> billQuery = new LambdaQueryWrapper<EbBill>()
                        .in(EbBill::getUserId, userIdsOfType)
                        .between(EbBill::getCreatedAt, startDateTime, endDateTime); // Using created_at for bills
                List<EbBill> billListForType = ebBillService.list(billQuery);
                BigDecimal totalFeeType = billListForType.stream()
                        .map(EbBill::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                stats.put("total_usage", totalUsageType);
                stats.put("total_fee", totalFeeType);
                long count = stats.getLongValue("user_count");
                stats.put("average_usage", count == 0 ? BigDecimal.ZERO : totalUsageType.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
                stats.put("average_fee", count == 0 ? BigDecimal.ZERO : totalFeeType.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            }
            stats.remove("userIds"); // Clean up temporary list
            distribution.add(stats);
        }


        // 3. Aggregate user growth by granularity for timeSeries chart
        Map<String, JSONObject> aggregatedGrowth = aggregateUserGrowthByGranularity(userList, granularity, startDateTime, endDateTime);

        // 4. Prepare timeSeries for growth chart
        List<String> dates = new ArrayList<>(aggregatedGrowth.keySet()).stream().sorted().collect(Collectors.toList());
        JSONArray totalUsersValues = new JSONArray();
        JSONArray newUsersValues = new JSONArray(); // Count of users created in the period

        // Recalculate total users cumulatively for the chart
        long cumulativeUsers = ebUserService.count(new LambdaQueryWrapper<EbUser>().lt(EbUser::getCreatedAt, startDateTime)); // Users before the start date
        for (String dateKey : dates) {
            JSONObject periodData = aggregatedGrowth.get(dateKey);
            long newInPeriod = periodData.getLongValue("new_users");
            newUsersValues.add(newInPeriod);
            cumulativeUsers += newInPeriod;
            totalUsersValues.add(cumulativeUsers);
        }


        // 5. Prepare summary
        summary.put("total_users", totalUsers); // Total users *overall*
        summary.put("residential_users", residentialUsers);
        summary.put("commercial_users", commercialUsers);
        // TODO: Calculate active_users and user_growth_rate
        summary.put("active_users", 0); // Placeholder - requires definition (e.g., users with usage/bills in period)
        summary.put("user_growth_rate", BigDecimal.ZERO); // Placeholder - requires previous period data
        summary.put("activity_period_days", 30); // Example

        // 6. Construct final result
        JSONObject timeSeriesData = new JSONObject();
        timeSeriesData.put("dates", dates);
        timeSeriesData.put("total_users", totalUsersValues);
        timeSeriesData.put("new_users", newUsersValues);

        result.put("summary", summary);
        result.put("distribution", distribution);
        result.put("timeSeries", timeSeriesData);
        result.put("tableData", distribution); // Use distribution data for the table in frontend

        return result;
    }

    // Helper to create initial stats object for a user type
    private JSONObject createInitialUserTypeStats(String userType) {
        JSONObject stats = new JSONObject();
        stats.put("user_type", userType);
        stats.put("user_count", 0L);
        stats.put("total_usage", BigDecimal.ZERO);
        stats.put("total_fee", BigDecimal.ZERO);
        stats.put("average_usage", BigDecimal.ZERO);
        stats.put("average_fee", BigDecimal.ZERO);
        stats.put("userIds", new ArrayList<Long>()); // Temporary list
        return stats;
    }

    // Helper to aggregate user growth
    private Map<String, JSONObject> aggregateUserGrowthByGranularity(List<EbUser> userList, String granularity, LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter formatter = getFormatterByGranularity(granularity);
        Map<String, JSONObject> aggregated = new HashMap<>();

        List<String> dateKeysInRange = DateUtils.getDateKeysInRange(start, end, granularity, formatter);
        for (String key : dateKeysInRange) {
            JSONObject initialData = new JSONObject();
            initialData.put("new_users", 0L);
            aggregated.put(key, initialData);
        }

        for (EbUser user : userList) {
            // Check if user was created within the overall period first
            // Ensure end date is effectively inclusive for the entire day/month/year
            LocalDateTime effectiveEnd = end;
            if ("daily".equalsIgnoreCase(granularity)) effectiveEnd = end.plusDays(1).truncatedTo(ChronoUnit.DAYS);
            else if ("monthly".equalsIgnoreCase(granularity)) effectiveEnd = end.plusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            else if ("yearly".equalsIgnoreCase(granularity)) effectiveEnd = end.plusYears(1).withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);


            if (user.getCreatedAt() != null && !user.getCreatedAt().isBefore(start) && user.getCreatedAt().isBefore(effectiveEnd)) {
                String dateKey = user.getCreatedAt().format(formatter);
                if (aggregated.containsKey(dateKey)) { // Check if the creation date falls within the generated keys
                    JSONObject periodData = aggregated.get(dateKey);
                    periodData.put("new_users", periodData.getLongValue("new_users") + 1);
                }
            }
        }
        return aggregated;
    }


    // --- Utility Methods ---

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            // Prioritize the full format
            if (dateTimeStr.length() == 19) { // YYYY-MM-DD HH:mm:ss
                return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
            }
            // Handle potential shorter formats from date picker type changes
            if (dateTimeStr.length() == 10) { // YYYY-MM-DD
                return LocalDate.parse(dateTimeStr).atStartOfDay();
            }
            if (dateTimeStr.length() == 7) { // YYYY-MM
                // For start date, use start of month. For end date, logic might need adjustment based on picker behavior.
                // Assuming range picker gives start/end months, we use start of month for both here for simplicity.
                // In fetchData, we adjust end date properly using getFormattedDateRangeForAPI.
                return YearMonth.parse(dateTimeStr, DATE_FORMATTER_MONTHLY).atDay(1).atStartOfDay();
            }
            if (dateTimeStr.length() == 4) { // YYYY
                // Similar to month, use start of year.
                return Year.parse(dateTimeStr, DATE_FORMATTER_YEARLY).atDay(1).atStartOfDay();
            }
            log.warn("Unparseable date-time format: {}", dateTimeStr);
            return null; // Or throw exception

        } catch (Exception e) {
            log.error("Failed to parse date-time string: {}", dateTimeStr, e);
            return null;
        }
    }

    // Java 11 compatible switch statement
    private DateTimeFormatter getFormatterByGranularity(String granularity) {
        switch (granularity.toLowerCase()) {
            case "monthly":
                return DATE_FORMATTER_MONTHLY;
            case "yearly":
                return DATE_FORMATTER_YEARLY;
            case "daily":
            default: // Default to daily
                return DATE_FORMATTER_DAILY;
        }
    }

    // More generic region extraction (still basic)
    private String extractRegion(String address) {
        if (address == null || address.isEmpty()) {
            return "未知区域";
        }
        Matcher matcher = REGION_PATTERN.matcher(address);
        String lastMatch = null;
        while (matcher.find()) {
            lastMatch = matcher.group(1); // Keep finding matches, the last one is likely the smallest unit
        }

        if (lastMatch != null) {
            return lastMatch;
        }

        // Fallback if no pattern matched
        if (address.length() >= 5) { // Slightly longer fallback
            return address.substring(0, 5);
        }

        return address; // Return full address if very short or no match
    }

}