package com.electricitybill.service.impl;

import com.electricitybill.entity.dto.report.ReportDTO;
import com.electricitybill.entity.po.EbElectricityUsage;
import com.electricitybill.entity.vo.report.ReportDataVO;
import com.electricitybill.enums.ReportType;
import com.electricitybill.mapper.EbElectricityUsageMapper;
import com.electricitybill.service.IEbElectricityUsageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.CollUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
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
}
