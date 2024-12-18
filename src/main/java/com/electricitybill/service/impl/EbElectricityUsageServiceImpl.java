package com.electricitybill.service.impl;

import com.electricitybill.entity.dto.report.ReportDTO;
import com.electricitybill.entity.po.EbElectricityUsage;
import com.electricitybill.entity.vo.report.ReportDataVO;
import com.electricitybill.enums.ReportType;
import com.electricitybill.mapper.EbElectricityUsageMapper;
import com.electricitybill.service.IEbElectricityUsageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

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
                    reportDataVO.setFeeAmount(ebElectricityUsage.getFeeAmount());
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
                            reportDataVO.setFeeAmount(reportDataVO.getFeeAmount().add(ebElectricityUsage.getFeeAmount()));
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
                    reportDataVO.setFeeAmount(reportDataVO.getFeeAmount().add(ebElectricityUsage.getFeeAmount()));
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
                            reportDataVO.setFeeAmount(reportDataVO.getFeeAmount().add(ebElectricityUsage.getFeeAmount()));
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
                            reportDataVO.setFeeAmount(reportDataVO.getFeeAmount().add(ebElectricityUsage.getFeeAmount()));
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
                            reportDataVO.setFeeAmount(reportDataVO.getFeeAmount().add(ebElectricityUsage.getFeeAmount()));
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

}
