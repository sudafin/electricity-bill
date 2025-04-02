package com.electricitybill.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.dto.report.ReportDTO;
import com.electricitybill.entity.po.EbUsageSummary;
import com.electricitybill.entity.po.EbElectricityUsage;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.vo.report.ReportDataVO;
import com.electricitybill.enums.DateType;
import com.electricitybill.enums.PeriodType;
import com.electricitybill.enums.ReportType;
import com.electricitybill.enums.UserType;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.EbElectricityUsageMapper;
import com.electricitybill.mapper.EbMeterMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbUsageSummaryService;
import com.electricitybill.service.IEbElectricityUsageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.DateUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Future;
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
    @Override
    public List<ReportDataVO> getReportData(ReportDTO reportDTO) {
        List<EbElectricityUsage> ebElectricityUsageList = lambdaQuery().between(EbElectricityUsage::getStartTime, reportDTO.getStartDate(), reportDTO.getEndDate()).list();
        LocalDateTime startDate = reportDTO.getStartDate();
        LocalDateTime endDate = reportDTO.getEndDate();
        ArrayList<ReportDataVO> reportDataVOS = new ArrayList<>();
        if (ReportType.DAILY.getDesc().equals(reportDTO.getReportType())) {
            //将startDate日期和endDate日期之间的所有日期都添加到列表中
            while (startDate.isBefore(endDate)) {
                ReportDataVO reportDataVO = new ReportDataVO();
                reportDataVO.setDate(startDate.toLocalDate());
                //先将feeAmount和electricityUsage设置为0,后续如果当前日期有数据就修改
                reportDataVO.setFeeAmount(BigDecimal.ZERO);
                reportDataVO.setElectricityUsage(BigDecimal.ZERO);
                //如果找到了该日期的数据，就将该日期的数据赋值给reportDataVO
                LocalDateTime finalStartDate = startDate;
                ebElectricityUsageList.stream().filter(ebElectricityUsage -> finalStartDate.toLocalDate().equals(ebElectricityUsage.getStartTime().toLocalDate())).forEach(ebElectricityUsage -> {
                    reportDataVO.setElectricityUsage(ebElectricityUsage.getUsageAmount());
                });
                reportDataVOS.add(reportDataVO);
                startDate = startDate.plusDays(1);
            }
        } else if (ReportType.MONTHLY.getDesc().equals(reportDTO.getReportType())) {
            //将startDate的月份和endDate的月份之间的所有月份都添加到列表中
            YearMonth startYearMonth = YearMonth.from(startDate);
            YearMonth endYearMonth = YearMonth.from(endDate);
            while (!startYearMonth.isAfter(endYearMonth)) {
                ReportDataVO reportDataVO = new ReportDataVO();
                reportDataVO.setDate(startYearMonth.atDay(1)); // 设置为该月的第一天
                reportDataVO.setFeeAmount(BigDecimal.ZERO);
                reportDataVO.setElectricityUsage(BigDecimal.ZERO);

                YearMonth finalStartYearMonth = startYearMonth;
                ebElectricityUsageList.stream()
                        .filter(ebElectricityUsage -> YearMonth.from(ebElectricityUsage.getStartTime()).equals(finalStartYearMonth))
                        .forEach(ebElectricityUsage -> {
                            // 将这个月份的数据相加
                            reportDataVO.setElectricityUsage(reportDataVO.getElectricityUsage().add(ebElectricityUsage.getUsageAmount()));
                        });
                reportDataVOS.add(reportDataVO);
                startYearMonth = startYearMonth.plusMonths(1); // 递增一个月
            }
        } else if (ReportType.YEARLY.getDesc().equals(reportDTO.getReportType())) {
            int startYear = startDate.getYear();
            int endYear = endDate.getYear();
            while (startYear <= endYear) {
                ReportDataVO reportDataVO = new ReportDataVO();
                reportDataVO.setDate(LocalDate.of(startYear, 1, 1));
                reportDataVO.setFeeAmount(BigDecimal.ZERO);
                reportDataVO.setElectricityUsage(BigDecimal.ZERO);
                int finalStartYear = startYear;
                ebElectricityUsageList.stream().filter(ebElectricityUsage -> finalStartYear == ebElectricityUsage.getStartTime().getYear()).forEach(ebElectricityUsage -> {
                    reportDataVO.setElectricityUsage(reportDataVO.getElectricityUsage().add(ebElectricityUsage.getUsageAmount()));
                });
                reportDataVOS.add(reportDataVO);
                startYear++;
            }
        }
        return reportDataVOS;
    }
    @Override
    @Async("generateReportExecutor")
    public Future<String> export(ReportDTO reportDTO) throws IOException {
        List<EbElectricityUsage> ebElectricityUsageList = lambdaQuery()
                .between(EbElectricityUsage::getStartTime, reportDTO.getStartDate(), reportDTO.getEndDate())
                .list();

        // 获取报告类型和日期范围
        LocalDateTime startDate = reportDTO.getStartDate();
        LocalDateTime endDate = reportDTO.getEndDate();
        List<ReportDataVO> reportDataVOS = new ArrayList<>();

        // 根据报表类型获取数据
        if (ReportType.DAILY.getDesc().equals(reportDTO.getReportType())) {
            while (startDate.isBefore(endDate)) {
                ReportDataVO reportDataVO = new ReportDataVO();
                reportDataVO.setDate(startDate.toLocalDate());
                // 设置日期时间格式为 "yyyy-MM-dd"
                reportDataVO.setFeeAmount(BigDecimal.ZERO);
                reportDataVO.setElectricityUsage(BigDecimal.ZERO);

                LocalDateTime finalStartDate = startDate;
                ebElectricityUsageList.stream()
                        .filter(ebElectricityUsage -> finalStartDate.toLocalDate().equals(ebElectricityUsage.getStartTime().toLocalDate()))
                        .forEach(ebElectricityUsage -> {

                            reportDataVO.setElectricityUsage(reportDataVO.getElectricityUsage().add(ebElectricityUsage.getUsageAmount()));
                        });

                reportDataVOS.add(reportDataVO);
                startDate = startDate.plusDays(1);
            }
        } else if (ReportType.MONTHLY.getDesc().equals(reportDTO.getReportType())) {
            YearMonth startYearMonth = YearMonth.from(startDate);
            YearMonth endYearMonth = YearMonth.from(endDate);
            while (!startYearMonth.isAfter(endYearMonth)) {
                ReportDataVO reportDataVO = new ReportDataVO();
                reportDataVO.setDate(startYearMonth.atDay(1));  // 设置为每月的第一天
                // 设置日期时间格式为 "yyyy-MM"
                reportDataVO.setDateTimeStr(startYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
                reportDataVO.setFeeAmount(BigDecimal.ZERO);
                reportDataVO.setElectricityUsage(BigDecimal.ZERO);

                YearMonth finalStartYearMonth = startYearMonth;
                ebElectricityUsageList.stream()
                        .filter(ebElectricityUsage -> YearMonth.from(ebElectricityUsage.getStartTime()).equals(finalStartYearMonth))
                        .forEach(ebElectricityUsage -> {
                            reportDataVO.setElectricityUsage(reportDataVO.getElectricityUsage().add(ebElectricityUsage.getUsageAmount()));
                        });

                reportDataVOS.add(reportDataVO);
                startYearMonth = startYearMonth.plusMonths(1);
            }
        } else if (ReportType.YEARLY.getDesc().equals(reportDTO.getReportType())) {
            Year startYear = Year.from(startDate);
            Year endYear = Year.from(endDate);
            while (!startYear.isAfter(endYear)) {
                ReportDataVO reportDataVO = new ReportDataVO();
                reportDataVO.setDate(startYear.atDay(1));
                // 设置日期时间格式为 "yyyy"
                reportDataVO.setDateTimeStr(startYear.format(DateTimeFormatter.ofPattern("yyyy")));
                reportDataVO.setFeeAmount(BigDecimal.ZERO);
                reportDataVO.setElectricityUsage(BigDecimal.ZERO);

                Year finalStartYear = startYear;
                ebElectricityUsageList.stream()
                        .filter(ebElectricityUsage -> finalStartYear.equals(Year.from(ebElectricityUsage.getStartTime())))
                        .forEach(ebElectricityUsage -> {
                            reportDataVO.setElectricityUsage(reportDataVO.getElectricityUsage().add(ebElectricityUsage.getUsageAmount()));
                        });

                reportDataVOS.add(reportDataVO);
                startYear = startYear.plusYears(1);
            }
        }

        // 创建Excel文件
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Report Data");

        // 设置表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("日期");
        headerRow.createCell(1).setCellValue("金额");
        headerRow.createCell(2).setCellValue("用电量");

        // 填充数据
        int rowIndex = 1;
        List<String> dates = new ArrayList<>();
        List<Double> feeAmounts = new ArrayList<>();
        List<Double> electricityUsages = new ArrayList<>();

        for (ReportDataVO reportDataVO : reportDataVOS) {
            Row row = sheet.createRow(rowIndex++);
            // 使用 getDateTimeStr() 获取格式化后的日期时间字符串
            row.createCell(0).setCellValue(reportDataVO.getDateTimeStr());
            row.createCell(1).setCellValue(reportDataVO.getFeeAmount().doubleValue());
            row.createCell(2).setCellValue(reportDataVO.getElectricityUsage().doubleValue());

            dates.add(reportDataVO.getDateTimeStr());
            feeAmounts.add(reportDataVO.getFeeAmount().doubleValue());
            electricityUsages.add(reportDataVO.getElectricityUsage().doubleValue());
        }

        // 创建图表
        XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, 0, 15, 10);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Report Chart");
        chart.setTitleOverlay(false);

        // 创建类别轴和数值轴
        XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        categoryAxis.setTitle("Date");

        XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.LEFT);
        valueAxis.setTitle("Amount");

        // 创建数据源（从填充的数据中创建）
        XDDFDataSource<String> dateSource = XDDFDataSourcesFactory.fromStringCellRange((XSSFSheet) sheet, new CellRangeAddress(1, rowIndex - 1, 0, 0)); // 日期列
        XDDFNumericalDataSource<Double> feeAmountSource = XDDFDataSourcesFactory.fromNumericCellRange((XSSFSheet) sheet, new CellRangeAddress(1, rowIndex - 1, 1, 1)); // Fee Amount 列

        // 创建柱状图数据
        XDDFBarChartData barChartData = (XDDFBarChartData) chart.createData(ChartTypes.BAR, categoryAxis, valueAxis);
        XDDFBarChartData.Series series1 = (XDDFBarChartData.Series) barChartData.addSeries(dateSource, feeAmountSource);
        series1.setTitle("Fee Amount", null);

        // 创建折线图数据
        XDDFNumericalDataSource<Double> electricityUsageSource = XDDFDataSourcesFactory.fromNumericCellRange((XSSFSheet) sheet, new CellRangeAddress(1, rowIndex - 1, 2, 2)); // Electricity Usage 列

        XDDFLineChartData lineChartData = (XDDFLineChartData) chart.createData(ChartTypes.LINE, categoryAxis, valueAxis);
        XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) lineChartData.addSeries(dateSource, electricityUsageSource);
        series2.setTitle("Electricity Usage", null);

        // 设置折线图样式
        XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(PresetColor.BLUE));
        series2.setFillProperties(fill);

        // 绘制图表
        chart.plot(barChartData);
        chart.plot(lineChartData);

        //把execl保存到临时文件中
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = "report_" + System.currentTimeMillis() + ".xlsx";
        String filePath = tempDir + File.separator + fileName;
        FileOutputStream outputStream = new FileOutputStream(filePath);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        log.info("文件路径：{}", filePath);
        return new AsyncResult<>(filePath);
    }

    /**
     * 计算当天的用电量和电费总和计入表
     */
    @Override
    public void calculateElectricityUsageSummaryDay() {
        //拿到当天的所有数据
        List<EbElectricityUsage> ebElectricityUsageList = this.list(new LambdaQueryWrapper<EbElectricityUsage>()
                // 大于昨天的结束时间小于等于今天的结束时间,
                .gt(EbElectricityUsage::getStartTime, DateUtils.getDayStartTime(LocalDateTime.now(zoneId)))
                .le(EbElectricityUsage::getEndTime, DateUtils.getDayEndTime(LocalDateTime.now(zoneId)))
        );
        // 检查数据是否为空
        if (ebElectricityUsageList.isEmpty()) {
            log.warn("当日无用电数据");
            return;
        }
        //key是当前的电表id, 然后value是这些数据的对象
        Map<String, List<EbElectricityUsage>> ebElectricityListMap= ebElectricityUsageList.stream().collect(groupingBy(EbElectricityUsage::getMeterId));
        //拿到所有的用户id
        List<Long> userIds = ebElectricityUsageList.stream().map(EbElectricityUsage::getUserId).distinct().collect(Collectors.toList());
        // 分批次查询（每批 1000 个）
        List<List<Long>> userIdBatches = Lists.partition(userIds, 1000);
        //将用户id与用户进行映射
        Map<Long, EbUser> ebUserMap = userIdBatches.stream()
                .map(batch -> ebUserMapper.selectBatchIds(batch))
                .flatMap(List::stream)
                .collect(Collectors.toMap(EbUser::getId, Function.identity()));
        ArrayList<EbUsageSummary> ebDailyUsageSummaries = new ArrayList<>();
        ebElectricityListMap.forEach((meterId,ebElectricityUsages)-> {
            // 确保同一电表下的所有记录用户 ID 一致
            Set<Long> uniqueUserIds = ebElectricityUsages.stream()
                    .map(EbElectricityUsage::getUserId)
                    .collect(Collectors.toSet());
            if (uniqueUserIds.size() > 1) {
                throw new DbException("同一电表存在多个用户 ID: " + meterId);
            }
            AtomicReference<Long> userId = new AtomicReference<>(uniqueUserIds.iterator().next());
            //初始化数据
            EbUsageSummary ebUsageSummary = new EbUsageSummary();
            AtomicReference<BigDecimal> finalCalculatePrice = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> finalCalculateUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> peakUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> flatUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> valleyUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> peakCost = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> flatCost = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> valleyCost = new AtomicReference<>(BigDecimal.ZERO);
            ebElectricityUsages.forEach(ebElectricityUsage -> {
                //拿到用户类型
                userId.set(ebElectricityUsage.getUserId());
                EbUser ebUser = ebUserMap.get(ebElectricityUsage.getUserId());
                if (Objects.isNull(ebUser)) {
                    throw new DbException(Constant.USER_NOT_EXIST);
                }
                UserType userType = UserType.of(ebUser.getUserType());
                //根据当前数据算出每条的数据的电费
                PeriodType periodType = PeriodType.of(ebElectricityUsage.getPeriodType());
                BigDecimal calculatePrice = userType.calculatePrice(ebElectricityUsage.getUsageAmount(), periodType, ebElectricityUsage.getStartTime());
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
                        if (userType.isEnableSummerPeak()) {
                            peakUsage.set(peakUsage.get().add(ebElectricityUsage.getUsageAmount()));
                            peakCost.set(peakCost.get().add(calculatePrice));
                        }
                        break;
                }
                finalCalculateUsage.set(finalCalculateUsage.get().add(ebElectricityUsage.getUsageAmount()));
                finalCalculatePrice.set(finalCalculatePrice.get().add(calculatePrice));
            });
            ebUsageSummary.setUserId(userId.get());
            ebUsageSummary.setMeterId(meterId);
            ebUsageSummary.setSummaryDateStart(DateUtils.getDayStartTime(LocalDateTime.now()));
            ebUsageSummary.setSummaryDateEnd(DateUtils.getDayEndTime(LocalDateTime.now()));
            //每天的类型
            ebUsageSummary.setDateType(DateType.
                    DAILY.getDesc());
            ebUsageSummary.setPeakUsage(peakUsage.get());
            ebUsageSummary.setFlatUsage(flatUsage.get());
            ebUsageSummary.setValleyUsage(valleyUsage.get());
            ebUsageSummary.setTotalUsage(finalCalculateUsage.get());
            ebUsageSummary.setPeakCost(peakCost.get());
            ebUsageSummary.setFlatCost(flatCost.get());
            ebUsageSummary.setValleyCost(valleyCost.get());
            ebUsageSummary.setTotalCost(finalCalculatePrice.get());
            ebDailyUsageSummaries.add(ebUsageSummary);
        });
        ebUsageSummaryService.saveBatch(ebDailyUsageSummaries);
    }

    /**
     * 将每月的记录聚合到月度表中
     */
    @Override
    public void calculateElectricityUsageSummaryMonth() {
        //计算每月的数据,key是电表id，value是对应数据的List集合
        Map<String, List<EbUsageSummary>> ebUserIdUsageSummary = ebUsageSummaryService.lambdaQuery().
                eq(EbUsageSummary::getDateType, DateType.DAILY.getDesc())
                .gt(EbUsageSummary::getSummaryDateStart, DateUtils.getMonthBegin(LocalDate.now(zoneId)))
                .le(EbUsageSummary::getSummaryDateEnd, DateUtils.getMonthEnd(LocalDate.now(zoneId)))
                .list().stream().collect(groupingBy(EbUsageSummary::getMeterId));
        List<EbUsageSummary> ebUsageSummaries = new ArrayList<>();
        ebUserIdUsageSummary.forEach((meterId, ebDailyUsageSummaries) -> {
            EbUsageSummary ebUsageSummary = new EbUsageSummary();
            AtomicReference<BigDecimal> finalCalculatePrice = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> finalCalculateUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> peakUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> flatUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> valleyUsage = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> peakCost = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> flatCost = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<BigDecimal> valleyCost = new AtomicReference<>(BigDecimal.ZERO);
            AtomicReference<Long> userId = new AtomicReference<>();
            ebDailyUsageSummaries.forEach(ebDailyUsageSummary -> {
                finalCalculateUsage.set(finalCalculateUsage.get().add(ebDailyUsageSummary.getTotalUsage()));
                finalCalculatePrice.set(finalCalculatePrice.get().add(ebDailyUsageSummary.getTotalCost()));
                peakUsage.set(peakUsage.get().add(ebDailyUsageSummary.getPeakUsage()));
                flatUsage.set(flatUsage.get().add(ebDailyUsageSummary.getFlatUsage()));
                valleyUsage.set(valleyUsage.get().add(ebDailyUsageSummary.getValleyUsage()));
                peakCost.set(peakCost.get().add(ebDailyUsageSummary.getPeakCost()));
                flatCost.set(flatCost.get().add(ebDailyUsageSummary.getFlatCost()));
                valleyCost.set(valleyCost.get().add(ebDailyUsageSummary.getValleyCost()));
                userId.set(ebDailyUsageSummary.getUserId());
            });
            ebUsageSummary.setUserId(userId.get());
            ebUsageSummary.setMeterId(meterId);
            ebUsageSummary.setSummaryDateStart(DateUtils.getMonthBeginTime(LocalDate.now(zoneId)));
            ebUsageSummary.setSummaryDateEnd(DateUtils.getMonthEndTime(LocalDate.now(zoneId)));
            ebUsageSummary.setDateType(DateType.MONTHLY.getDesc());
            ebUsageSummary.setPeakUsage(peakUsage.get());
            ebUsageSummary.setFlatUsage(flatUsage.get());
            ebUsageSummary.setValleyUsage(valleyUsage.get());
            ebUsageSummary.setTotalUsage(finalCalculateUsage.get());
            ebUsageSummary.setPeakCost(peakCost.get());
            ebUsageSummary.setFlatCost(flatCost.get());
            ebUsageSummary.setValleyCost(valleyCost.get());
            ebUsageSummary.setTotalCost(finalCalculatePrice.get());
            ebUsageSummaries.add(ebUsageSummary);
        });
        ebUsageSummaryService.saveBatch(ebUsageSummaries);
    }

}
