package com.electricitybill.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.entity.po.EbUsageSummary;
import com.electricitybill.enums.DateType;
import com.electricitybill.service.IEbUsageSummaryService;
import com.electricitybill.service.UserElectricityService;
import com.electricitybill.utils.UserContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserElectricityServiceImpl implements UserElectricityService {

    private final IEbUsageSummaryService usageSummaryService;

    @Override
    public JSONObject getElectricityStatistics( JSONObject jsonObject) {
        Long userId = UserContextUtils.getUserId();
        String startDateStr = jsonObject.getStr("startDate");
        String endDateStr = jsonObject.getStr("endDate");

        // 设置默认日期范围（如果未提供）
        LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr) : LocalDate.now().minusMonths(1);
        LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : LocalDate.now();

        // 构建查询条件
        LambdaQueryWrapper<EbUsageSummary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EbUsageSummary::getUserId, userId)
                .ge(EbUsageSummary::getSummaryDateStart, startDate)
                .le(EbUsageSummary::getSummaryDateStart, endDate)
                .eq(EbUsageSummary::getDateType, DateType.DAILY.getDesc());

        // 查询指定日期范围内的用电记录
        List<EbUsageSummary> usageList = usageSummaryService.list(queryWrapper);

        if (usageList.isEmpty()) {
            log.warn("用户[{}]在日期范围[{} - {}]内没有用电记录", userId, startDate, endDate);
            return createEmptyStatistics(startDate, endDate);
        }

        // 计算统计数据
        BigDecimal totalConsumption = usageList.stream()
                .map(EbUsageSummary::getTotalUsage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCost = usageList.stream()
                .map(EbUsageSummary::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算分时用电量和电费
        BigDecimal peakUsage = usageList.stream()
                .map(EbUsageSummary::getPeakUsage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal flatUsage = usageList.stream()
                .map(EbUsageSummary::getFlatUsage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal valleyUsage = usageList.stream()
                .map(EbUsageSummary::getValleyUsage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal peakCost = usageList.stream()
                .map(EbUsageSummary::getPeakCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal flatCost = usageList.stream()
                .map(EbUsageSummary::getFlatCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal valleyCost = usageList.stream()
                .map(EbUsageSummary::getValleyCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算日均用电量
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal dailyAvgConsumption = days > 0 
                ? totalConsumption.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;

        // 构建返回结果
        JSONObject result = new JSONObject();
        result.set("totalConsumption", totalConsumption); // 总用电量 (kWh)
        result.set("totalCost", totalCost); // 总电费 (元)
        result.set("dailyAvgConsumption", dailyAvgConsumption); // 日均用电量 (kWh)
        result.set("startDate", startDate.toString());
        result.set("endDate", endDate.toString());
        result.set("days", days);
        
        // 添加分时用电和电费数据
        JSONObject timeSharing = new JSONObject();
        timeSharing.set("peakUsage", peakUsage);
        timeSharing.set("flatUsage", flatUsage);
        timeSharing.set("valleyUsage", valleyUsage);
        timeSharing.set("peakCost", peakCost);
        timeSharing.set("flatCost", flatCost);
        timeSharing.set("valleyCost", valleyCost);
        result.set("timeSharing", timeSharing);

        return result;
    }

    @Override
    public JSONObject getDailyElectricityUsage( JSONObject jsonObject) {
        Long userId = UserContextUtils.getUserId();
        String startDateStr = jsonObject.getStr("startDate");
        String endDateStr = jsonObject.getStr("endDate");
        Integer days = jsonObject.getInt("days");

        // 设置默认值
        if (days == null || days <= 0) {
            days = 30;
        }

        // 设置默认日期范围（如果未提供）
        LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : LocalDate.now();
        LocalDate startDate = startDateStr != null 
                ? LocalDate.parse(startDateStr) 
                : endDate.minusDays(days - 1);

        // 构建查询条件
        LambdaQueryWrapper<EbUsageSummary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EbUsageSummary::getUserId, userId)
                .ge(EbUsageSummary::getSummaryDateStart, startDate)
                .le(EbUsageSummary::getSummaryDateStart, endDate)
                .eq(EbUsageSummary::getDateType, DateType.DAILY.getDesc())
                .orderByAsc(EbUsageSummary::getSummaryDateStart);

        // 查询指定日期范围内的用电记录
        List<EbUsageSummary> usageList = usageSummaryService.list(queryWrapper);

        // 按日期分组
        Map<LocalDate, List<EbUsageSummary>> usageByDate = usageList.stream()
                .collect(Collectors.groupingBy(summary -> summary.getSummaryDateStart().toLocalDate()));

        // 构建每日用电量数据
        JSONArray dailyUsage = new JSONArray();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<EbUsageSummary> dayUsages = usageByDate.getOrDefault(date, List.of());
            
            // 当日所有电表的汇总数据
            BigDecimal totalConsumption = BigDecimal.ZERO;
            BigDecimal totalCost = BigDecimal.ZERO;
            BigDecimal peakUsage = BigDecimal.ZERO;
            BigDecimal flatUsage = BigDecimal.ZERO;
            BigDecimal valleyUsage = BigDecimal.ZERO;
            BigDecimal peakCost = BigDecimal.ZERO;
            BigDecimal flatCost = BigDecimal.ZERO;
            BigDecimal valleyCost = BigDecimal.ZERO;
            
            for (EbUsageSummary usage : dayUsages) {
                totalConsumption = totalConsumption.add(usage.getTotalUsage());
                totalCost = totalCost.add(usage.getTotalCost());
                peakUsage = peakUsage.add(usage.getPeakUsage());
                flatUsage = flatUsage.add(usage.getFlatUsage());
                valleyUsage = valleyUsage.add(usage.getValleyUsage());
                peakCost = peakCost.add(usage.getPeakCost());
                flatCost = flatCost.add(usage.getFlatCost());
                valleyCost = valleyCost.add(usage.getValleyCost());
            }
            
            JSONObject dayData = new JSONObject();
            dayData.set("date", date.toString());
            dayData.set("consumption", totalConsumption); // 当日用电量 (kWh)
            dayData.set("cost", totalCost); // 当日电费 (元)
            
            // 添加峰平谷分时数据
            JSONObject timeSharing = new JSONObject();
            timeSharing.set("peakUsage", peakUsage);
            timeSharing.set("flatUsage", flatUsage);
            timeSharing.set("valleyUsage", valleyUsage);
            timeSharing.set("peakCost", peakCost);
            timeSharing.set("flatCost", flatCost);
            timeSharing.set("valleyCost", valleyCost);
            dayData.set("timeSharing", timeSharing);
            
            dailyUsage.add(dayData);
        }

        // 构建返回结果
        JSONObject result = new JSONObject();
        result.set("startDate", startDate.toString());
        result.set("endDate", endDate.toString());
        result.set("days", ChronoUnit.DAYS.between(startDate, endDate) + 1);
        result.set("dailyUsage", dailyUsage);

        return result;
    }

    /**
     * 创建周期数据（用于趋势图）
     */
    private JSONObject createPeriodData(List<EbUsageSummary> usages, String timeLabel) {
        BigDecimal totalConsumption = usages.stream()
                .map(EbUsageSummary::getTotalUsage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal peakUsage = usages.stream()
                .map(EbUsageSummary::getPeakUsage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal flatUsage = usages.stream()
                .map(EbUsageSummary::getFlatUsage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal valleyUsage = usages.stream()
                .map(EbUsageSummary::getValleyUsage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        JSONObject periodData = new JSONObject();
        periodData.set("time", timeLabel);
        periodData.set("consumption", totalConsumption);
        
        JSONObject timeSharing = new JSONObject();
        timeSharing.set("peakUsage", peakUsage);
        timeSharing.set("flatUsage", flatUsage);
        timeSharing.set("valleyUsage", valleyUsage);
        periodData.set("timeSharing", timeSharing);
        
        return periodData;
    }
    
    /**
     * 获取指定日期的用电汇总数据
     */
    private JSONObject getUsageSummaryForDate(Long userId, LocalDate date, DateType dateType) {
        // 构建查询条件
        LambdaQueryWrapper<EbUsageSummary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EbUsageSummary::getUserId, userId)
                  .eq(EbUsageSummary::getSummaryDateStart, date)
                  .eq(EbUsageSummary::getDateType, dateType.getDesc());
        
        // 查询数据
        List<EbUsageSummary> usageList = usageSummaryService.list(queryWrapper);
        
        // 计算汇总
        BigDecimal totalConsumption = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal peakUsage = BigDecimal.ZERO;
        BigDecimal flatUsage = BigDecimal.ZERO;
        BigDecimal valleyUsage = BigDecimal.ZERO;
        BigDecimal peakCost = BigDecimal.ZERO;
        BigDecimal flatCost = BigDecimal.ZERO;
        BigDecimal valleyCost = BigDecimal.ZERO;
        
        for (EbUsageSummary usage : usageList) {
            totalConsumption = totalConsumption.add(usage.getTotalUsage());
            totalCost = totalCost.add(usage.getTotalCost());
            peakUsage = peakUsage.add(usage.getPeakUsage());
            flatUsage = flatUsage.add(usage.getFlatUsage());
            valleyUsage = valleyUsage.add(usage.getValleyUsage());
            peakCost = peakCost.add(usage.getPeakCost());
            flatCost = flatCost.add(usage.getFlatCost());
            valleyCost = valleyCost.add(usage.getValleyCost());
        }
        
        // 构建结果
        JSONObject result = new JSONObject();
        result.set("totalConsumption", totalConsumption);
        result.set("totalCost", totalCost);
        
        JSONObject timeSharing = new JSONObject();
        timeSharing.set("peakUsage", peakUsage);
        timeSharing.set("flatUsage", flatUsage);
        timeSharing.set("valleyUsage", valleyUsage);
        timeSharing.set("peakCost", peakCost);
        timeSharing.set("flatCost", flatCost);
        timeSharing.set("valleyCost", valleyCost);
        result.set("timeSharing", timeSharing);
        
        return result;
    }
    
    /**
     * 获取指定日期范围的用电汇总数据
     */
    private JSONObject getUsageSummaryForDateRange(Long userId, LocalDate startDate, LocalDate endDate, DateType dateType) {
        // 先尝试查询汇总记录
        LambdaQueryWrapper<EbUsageSummary> summaryQuery = new LambdaQueryWrapper<>();
        summaryQuery.eq(EbUsageSummary::getUserId, userId)
                   .eq(EbUsageSummary::getDateType, dateType.getDesc());
                   
        if (dateType == DateType.MONTHLY) {
            // 查询当月数据
            summaryQuery.eq(EbUsageSummary::getSummaryDateStart, startDate);
        } else if (dateType == DateType.YEARLY) {
            // 查询当年数据
            summaryQuery.eq(EbUsageSummary::getSummaryDateStart, startDate);
        }
        
        List<EbUsageSummary> summaryList = usageSummaryService.list(summaryQuery);
        
        // 如果找到汇总记录，直接计算
        if (!summaryList.isEmpty()) {
            return calculateSummary(summaryList);
        }
        
        // 如果没有汇总记录，则查询日记录并手动汇总
        LambdaQueryWrapper<EbUsageSummary> dailyQuery = new LambdaQueryWrapper<>();
        dailyQuery.eq(EbUsageSummary::getUserId, userId)
                 .ge(EbUsageSummary::getSummaryDateStart, startDate)
                 .le(EbUsageSummary::getSummaryDateStart, endDate)
                 .eq(EbUsageSummary::getDateType, DateType.DAILY.getDesc());
        
        List<EbUsageSummary> dailyList = usageSummaryService.list(dailyQuery);
        
        return calculateSummary(dailyList);
    }
    
    /**
     * 计算汇总数据
     */
    private JSONObject calculateSummary(List<EbUsageSummary> usageList) {
        BigDecimal totalConsumption = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal peakUsage = BigDecimal.ZERO;
        BigDecimal flatUsage = BigDecimal.ZERO;
        BigDecimal valleyUsage = BigDecimal.ZERO;
        BigDecimal peakCost = BigDecimal.ZERO;
        BigDecimal flatCost = BigDecimal.ZERO;
        BigDecimal valleyCost = BigDecimal.ZERO;
        
        for (EbUsageSummary usage : usageList) {
            totalConsumption = totalConsumption.add(usage.getTotalUsage());
            totalCost = totalCost.add(usage.getTotalCost());
            peakUsage = peakUsage.add(usage.getPeakUsage());
            flatUsage = flatUsage.add(usage.getFlatUsage());
            valleyUsage = valleyUsage.add(usage.getValleyUsage());
            peakCost = peakCost.add(usage.getPeakCost());
            flatCost = flatCost.add(usage.getFlatCost());
            valleyCost = valleyCost.add(usage.getValleyCost());
        }
        
        JSONObject result = new JSONObject();
        result.set("totalConsumption", totalConsumption);
        result.set("totalCost", totalCost);
        
        JSONObject timeSharing = new JSONObject();
        timeSharing.set("peakUsage", peakUsage);
        timeSharing.set("flatUsage", flatUsage);
        timeSharing.set("valleyUsage", valleyUsage);
        timeSharing.set("peakCost", peakCost);
        timeSharing.set("flatCost", flatCost);
        timeSharing.set("valleyCost", valleyCost);
        result.set("timeSharing", timeSharing);
        
        return result;
    }
    
    /**
     * 创建空的统计数据
     */
    private JSONObject createEmptyStatistics(LocalDate startDate, LocalDate endDate) {
        JSONObject result = new JSONObject();
        result.set("totalConsumption", BigDecimal.ZERO);
        result.set("totalCost", BigDecimal.ZERO);
        result.set("dailyAvgConsumption", BigDecimal.ZERO);
        result.set("startDate", startDate.toString());
        result.set("endDate", endDate.toString());
        result.set("days", ChronoUnit.DAYS.between(startDate, endDate) + 1);
        
        JSONObject timeSharing = new JSONObject();
        timeSharing.set("peakUsage", BigDecimal.ZERO);
        timeSharing.set("flatUsage", BigDecimal.ZERO);
        timeSharing.set("valleyUsage", BigDecimal.ZERO);
        timeSharing.set("peakCost", BigDecimal.ZERO);
        timeSharing.set("flatCost", BigDecimal.ZERO);
        timeSharing.set("valleyCost", BigDecimal.ZERO);
        result.set("timeSharing", timeSharing);
        
        return result;
    }
}