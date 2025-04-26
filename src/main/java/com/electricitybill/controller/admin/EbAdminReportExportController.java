package com.electricitybill.controller.admin;

import com.electricitybill.service.ReportExportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(tags = "报表导出接口")
@RestController
@RequestMapping("/admin/reports")
public class EbAdminReportExportController {

    @Autowired
    private ReportExportService reportExportService;

    @ApiOperation("导出报表数据")
    @GetMapping("/export")
    public void exportReport(
            @ApiParam(value = "报表类型", required = true)
            @RequestParam String reportType,
            
            @ApiParam(value = "开始时间", required = true)
            @RequestParam String startDate,
            
            @ApiParam(value = "结束时间", required = true)
            @RequestParam String endDate,
            
            @ApiParam(value = "数据粒度", required = false, defaultValue = "daily")
            @RequestParam(required = false, defaultValue = "daily") String granularity,
            
            @ApiParam(value = "导出格式", required = false, defaultValue = "excel")
            @RequestParam(required = false, defaultValue = "excel") String format,
            
            HttpServletResponse response) throws IOException {
        
        switch (reportType) {
            case "electricity":
                reportExportService.exportElectricityReport(startDate, endDate, granularity, format, response);
                break;
            case "fee":
                reportExportService.exportFeeReport(startDate, endDate, granularity, format, response);
                break;
            case "feedback":
                reportExportService.exportFeedbackReport(startDate, endDate, granularity, format, response);
                break;
            case "reconciliation":
                reportExportService.exportReconciliationReport(startDate, endDate, granularity, format, response);
                break;
            case "userType":
                reportExportService.exportUserTypeReport(startDate, endDate, granularity, format, response);
                break;
            default:
                throw new IllegalArgumentException("不支持的报表类型: " + reportType);
        }
    }
}