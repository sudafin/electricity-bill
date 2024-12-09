package com.electricitybill.controller.report;


import com.electricitybill.entity.dto.report.ReportDTO;
import com.electricitybill.entity.vo.report.ReportDataVO;
import com.electricitybill.service.IEbElectricityUsageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@RestController
@RequestMapping("/report")
public class EbReportController {
    @Resource
    private IEbElectricityUsageService ebElectricityUsageService;


    @ApiOperation("获取报表")
    @GetMapping
    public List<ReportDataVO> getReportData(ReportDTO reportDTO) {
        return ebElectricityUsageService.getReportData(reportDTO);
    }

}
