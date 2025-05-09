package com.electricitybill.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.entity.dto.rate.RateCacheDTO;
import com.electricitybill.entity.po.*;
import com.electricitybill.enums.*;
import com.electricitybill.mapper.EbElectricityUsageMapper;
import com.electricitybill.mapper.EbMeterMapper;
import com.electricitybill.mapper.EbRateMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbBillService;
import com.electricitybill.service.IEbUsageSummaryService;
import com.electricitybill.service.IEbElectricityUsageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.DateUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.electricitybill.utils.DateUtils.zoneId;
import static java.util.stream.Collectors.groupingBy;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
@Slf4j
public class EbElectricityUsageServiceImpl extends ServiceImpl<EbElectricityUsageMapper, EbElectricityUsage> implements IEbElectricityUsageService {
    @Resource
    private EbMeterMapper ebMeterMapper;
    @Resource
    private EbUserMapper ebUserMapper;
    @Resource
    private IEbUsageSummaryService ebUsageSummaryService;
    @Resource
    private IEbBillService ebBillService;
    /**
     * 计算当天的用电量和电费总和计入表
     */
    @Override
    public void calculateElectricityUsageSummaryDay() {
        //拿到当天的所有数据
        List<EbElectricityUsage> ebElectricityUsageList = this.list(new LambdaQueryWrapper<EbElectricityUsage>()
                // 大于昨天的结束时间小于等于今天的结束时间,
                .gt(EbElectricityUsage::getStartTime, DateUtils.getDayStartTime(LocalDateTime.now(zoneId))).le(EbElectricityUsage::getEndTime, DateUtils.getDayEndTime(LocalDateTime.now(zoneId))));
        // 检查数据是否为空
        if (ebElectricityUsageList.isEmpty()) {
            log.warn("当日所有无用电数据");
            return;
        }
        //key是当前的电表id, 然后value是这些数据的对象
        Map<String, List<EbElectricityUsage>> ebElectricityListMap = ebElectricityUsageList.stream().collect(groupingBy(EbElectricityUsage::getMeterId));
        //拿到所有的用户id
        List<Long> userIds = ebElectricityUsageList.stream().map(EbElectricityUsage::getUserId).distinct().collect(Collectors.toList());
        // 分批次查询（每批 1000 个）
        List<List<Long>> userIdBatches = Lists.partition(userIds, 1000);
        //将用户id与用户进行映射
        Map<Long, EbUser> ebUserMap = userIdBatches.stream().map(batch -> ebUserMapper.selectBatchIds(batch)).flatMap(List::stream).collect(Collectors.toMap(EbUser::getId, Function.identity()));
        ArrayList<EbUsageSummary> ebDailyUsageSummaries = new ArrayList<>();
        for (Map.Entry<String, List<EbElectricityUsage>> entry : ebElectricityListMap.entrySet()) {
            String meterId = entry.getKey();
            List<EbElectricityUsage> ebElectricityUsages = entry.getValue();
            //创建一个usageSummary统计数据
            EbUsageSummary ebUsageSummary = new EbUsageSummary();
            // 确保同一电表下的所有记录用户 ID 一致
            Set<Long> uniqueUserIds = ebElectricityUsages.stream().map(EbElectricityUsage::getUserId).collect(Collectors.toSet());
            if (uniqueUserIds.size() > 1) {
                log.warn("当前电表{}存在多个用户 ID",meterId);
                continue;
            }
            //拿到用户id
            AtomicReference<Long> userId = new AtomicReference<>(uniqueUserIds.iterator().next());
            if(ebElectricityUsages.isEmpty()){
                log.warn("当前电表{}无电费记录",meterId);
                ebUsageSummary.setUserId(userId.get());
                ebUsageSummary.setMeterId(meterId);
                ebUsageSummary.setCreatedAt(LocalDateTime.now(zoneId));
                ebUsageSummary.setSummaryDateStart(DateUtils.getDayStartTime(LocalDateTime.now()));
                ebUsageSummary.setSummaryDateEnd(DateUtils.getDayEndTime(LocalDateTime.now()));
                ebUsageSummary.setDateType(DateType.DAILY.getDesc());
                ebUsageSummary.setTotalCost(BigDecimal.ZERO);
                ebUsageSummary.setTotalUsage(BigDecimal.ZERO);
                ebUsageSummary.setTotalCost(BigDecimal.ZERO);
                ebUsageSummary.setTotalUsage(BigDecimal.ZERO);
                ebUsageSummaryService.save(ebUsageSummary);
                continue;
            }
            //初始化数据
            AtomicReference<BigDecimal> finalCalculatePrice = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> finalCalculateUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> peakUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> flatUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> valleyUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> peakCost = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> flatCost = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> valleyCost = new AtomicReference<>(BigDecimal.ZERO);
            for (EbElectricityUsage ebElectricityUsage : ebElectricityUsages) {
                userId.set(ebElectricityUsage.getUserId());
                EbUser ebUser = ebUserMap.get(ebElectricityUsage.getUserId());
                if (Objects.isNull(ebUser)) {
                    log.warn("用户{}不存在",ebElectricityUsage.getUserId());
                    continue;
                }
                String userType = ebUser.getUserType();
                //根据当前数据算出每条的数据的电费
                PeriodType periodType = PeriodType.of(ebElectricityUsage.getPeriodType());
                BigDecimal calculatePrice = calculatePrice(ebElectricityUsage.getUsageAmount(), periodType, userType);
                switch (periodType) {
                    case PEAK:
                        peakUsage.set(peakUsage.get().add(ebElectricityUsage.getUsageAmount()));
                        peakCost.set(peakCost.get().add(calculatePrice));
                        break;  // 添加 break
                    case FLAT:
                        flatUsage.set(flatUsage.get().add(ebElectricityUsage.getUsageAmount()));
                        flatCost.set(flatCost.get().add(calculatePrice));
                        break;
                    case VALLEY:
                        valleyUsage.set(valleyUsage.get().add(ebElectricityUsage.getUsageAmount()));
                        valleyCost.set(valleyCost.get().add(calculatePrice));
                        break;
                    case SUMMER_PEAK:
                        peakUsage.set(peakUsage.get().add(ebElectricityUsage.getUsageAmount()));
                        peakCost.set(peakCost.get().add(calculatePrice));
                        break;
                }
                finalCalculateUsage.set(finalCalculateUsage.get().add(ebElectricityUsage.getUsageAmount()));
                finalCalculatePrice.set(finalCalculatePrice.get().add(calculatePrice));
            }
            ebUsageSummary.setUserId(userId.get());
            ebUsageSummary.setMeterId(meterId);
            ebUsageSummary.setSummaryDateStart(DateUtils.getDayStartTime(LocalDateTime.now()));
            ebUsageSummary.setSummaryDateEnd(DateUtils.getDayEndTime(LocalDateTime.now()));
            //这个是设置日类型
            ebUsageSummary.setDateType(DateType.DAILY.getDesc());
            ebUsageSummary.setPeakUsage(peakUsage.get());
            ebUsageSummary.setFlatUsage(flatUsage.get());
            ebUsageSummary.setValleyUsage(valleyUsage.get());
            ebUsageSummary.setTotalUsage(finalCalculateUsage.get());
            ebUsageSummary.setPeakCost(peakCost.get());
            ebUsageSummary.setFlatCost(flatCost.get());
            ebUsageSummary.setValleyCost(valleyCost.get());
            ebUsageSummary.setTotalCost(finalCalculatePrice.get());
            ebDailyUsageSummaries.add(ebUsageSummary);
        }
        ebUsageSummaryService.saveBatch(ebDailyUsageSummaries);
    }

    /**
     * 计算每月的电量然后生成订单
     */
    @Override
    @Transactional
    public void calculateMonthlyBill() {
        // 1. 获取所有电表数据，构建userId到电表的映射
        Map<Long, EbMeter> ebMeterMap = ebMeterMapper.selectList(new LambdaQueryWrapper<>()).stream().collect(Collectors.toMap(EbMeter::getUserId, Function.identity(), (existing, replacement) -> existing)); // 处理可能的重复键

        // 2. 查询当月每日用电数据并按电表ID分组
        LocalDate now = LocalDate.now(zoneId);
        Map<String, List<EbUsageSummary>> dailyUsageByMeter = ebUsageSummaryService.lambdaQuery().eq(EbUsageSummary::getDateType, DateType.DAILY.getDesc()).gt(EbUsageSummary::getSummaryDateStart, DateUtils.getMonthBegin(now)).le(EbUsageSummary::getSummaryDateEnd, DateUtils.getMonthEnd(now)).list().stream().collect(Collectors.groupingBy(EbUsageSummary::getMeterId));

        // 3. 处理每个电表的月度数据
        List<EbUsageSummary> monthlySummaries = new ArrayList<>();
        List<EbBill> monthlyBills = new ArrayList<>();

        for (Map.Entry<String, List<EbUsageSummary>> entry : dailyUsageByMeter.entrySet()) {
            String meterId = entry.getKey();
            List<EbUsageSummary> dailySummaries = entry.getValue();
            if (dailySummaries.isEmpty()) {
                log.warn("电表{}没有日数据", meterId);
                continue;
            }

            // 获取第一个日记录的用户ID(假设所有日记录用户ID相同)
            Long userId = dailySummaries.get(0).getUserId();
            EbMeter meter = ebMeterMap.get(userId);
            if (meter == null) {
                log.warn("电表{}对应的用户不存在", meterId);
                continue;
            }

            // 计算月度汇总数据
            MonthlyUsageSummary summary = calculateMonthlyUsage(dailySummaries);

            // 创建月度用电汇总记录
            EbUsageSummary monthlySummary = createMonthlyUsageSummary(userId, meterId, now, summary);
            monthlySummaries.add(monthlySummary);

            // 创建月度账单
            EbBill bill = createMonthlyBill(userId, meter, summary, now);
            monthlyBills.add(bill);
        }

        // 4. 批量保存数据
        if (!monthlySummaries.isEmpty()) {
            ebUsageSummaryService.saveBatch(monthlySummaries);
        }
        if (!monthlyBills.isEmpty()) {
            ebBillService.saveBatch(monthlyBills);
        }
    }

    private MonthlyUsageSummary calculateMonthlyUsage(List<EbUsageSummary> dailySummaries) {
        MonthlyUsageSummary summary = new MonthlyUsageSummary();
        summary.totalUsage = BigDecimal.ZERO;
        summary.totalCost = BigDecimal.ZERO;
        summary.peakUsage = BigDecimal.ZERO;
        summary.flatUsage = BigDecimal.ZERO;
        summary.valleyUsage = BigDecimal.ZERO;
        summary.peakCost = BigDecimal.ZERO;
        summary.flatCost = BigDecimal.ZERO;
        summary.valleyCost = BigDecimal.ZERO;

        for (EbUsageSummary daily : dailySummaries) {
            summary.totalUsage = summary.totalUsage.add(daily.getTotalUsage());
            summary.totalCost = summary.totalCost.add(daily.getTotalCost());
            summary.peakUsage = summary.peakUsage.add(daily.getPeakUsage());
            summary.flatUsage = summary.flatUsage.add(daily.getFlatUsage());
            summary.valleyUsage = summary.valleyUsage.add(daily.getValleyUsage());
            summary.peakCost = summary.peakCost.add(daily.getPeakCost());
            summary.flatCost = summary.flatCost.add(daily.getFlatCost());
            summary.valleyCost = summary.valleyCost.add(daily.getValleyCost());
        }
        return summary;
    }

    private EbUsageSummary createMonthlyUsageSummary(Long userId, String meterId, LocalDate month, MonthlyUsageSummary summary) {
        EbUsageSummary monthlySummary = new EbUsageSummary();
        monthlySummary.setUserId(userId);
        monthlySummary.setMeterId(meterId);
        monthlySummary.setSummaryDateStart(DateUtils.getMonthBeginTime(month));
        monthlySummary.setSummaryDateEnd(DateUtils.getMonthEndTime(month));
        monthlySummary.setDateType(DateType.MONTHLY.getDesc());
        monthlySummary.setPeakUsage(summary.peakUsage);
        monthlySummary.setFlatUsage(summary.flatUsage);
        monthlySummary.setValleyUsage(summary.valleyUsage);
        monthlySummary.setTotalUsage(summary.totalUsage);
        monthlySummary.setPeakCost(summary.peakCost);
        monthlySummary.setFlatCost(summary.flatCost);
        monthlySummary.setValleyCost(summary.valleyCost);
        monthlySummary.setTotalCost(summary.totalCost);
        return monthlySummary;
    }

    private EbBill createMonthlyBill(Long userId, EbMeter meter, MonthlyUsageSummary summary, LocalDate month) {
        EbBill bill = new EbBill();
        bill.setUserId(userId);
        bill.setUsageAmount(summary.totalUsage);
        bill.setTotalAmount(summary.totalCost);
        bill.setStatus(BillType.UNPAID.getDesc());
        bill.setMeterId(meter.getId());
        bill.setStartReading(meter.getStartReading());
        bill.setEndingReading(meter.getEndingReading());
        bill.setDueDate(DateUtils.getDayEndTime(LocalDateTime.now().plusDays(7)));
        return bill;
    }

    // 辅助类和方法
    private static class MonthlyUsageSummary {
        BigDecimal totalUsage;
        BigDecimal totalCost;
        BigDecimal peakUsage;
        BigDecimal flatUsage;
        BigDecimal valleyUsage;
        BigDecimal peakCost;
        BigDecimal flatCost;
        BigDecimal valleyCost;
    }
    /**
     * 计算分时电价（单位：元/度）
     *         平段基准电价
     * @param periodType
     *         时段类型（PEAK/FLAT/VALLEY/SUMMER_PEAK）
     *
     */
    public BigDecimal calculatePrice(BigDecimal usageAmount, PeriodType periodType, String userType) {
        //拿到当前用户类型的电价缓存
        RateCacheDTO rateCacheDTO = new RateCacheDTO().RateCacheDTOUserType(userType);
        // 计算折后电价
        usageAmount = usageAmount.multiply(BigDecimal.valueOf(rateCacheDTO.discountRate)).setScale(4, RoundingMode.HALF_UP);
        switch (periodType) {
            case PEAK:
                return usageAmount.multiply(BigDecimal.valueOf(rateCacheDTO.peakMultiplier)).setScale(4, RoundingMode.HALF_UP);
            case VALLEY:
                return usageAmount.multiply(BigDecimal.valueOf(rateCacheDTO.valleyMultiplier)).setScale(4, RoundingMode.HALF_UP);
            case SUMMER_PEAK:
                //如果是夏季，则使用夏季电价
                return usageAmount.multiply(BigDecimal.valueOf(rateCacheDTO.valleyMultiplier)).multiply(BigDecimal.valueOf(rateCacheDTO.summerPeak)).setScale(4, RoundingMode.HALF_UP);
            default: // FLAT
                return usageAmount.setScale(4, RoundingMode.HALF_UP);
        }
    }
}
