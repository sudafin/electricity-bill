package com.electricitybill.controller.admin;


import com.electricitybill.annotation.ExportExcel;
import com.electricitybill.entity.dto.report.ReportDTO;
import com.electricitybill.entity.vo.report.ReportDataVO;
import com.electricitybill.service.IEbElectricityUsageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
@Api(tags = "管理端报表管理")
public class EbAdminReportController {
    @Resource
    private IEbElectricityUsageService ebElectricityUsageService;


    @ApiOperation("获取报表")
    @GetMapping
    public List<ReportDataVO> getReportData(ReportDTO reportDTO) {
        return ebElectricityUsageService.getReportData(reportDTO);
    }

    @GetMapping("/export")
    //自定义响应文件给前端的注解
    @ExportExcel
    @ApiOperation("导出运营数据报表")
    public String export(ReportDTO reportDTO) throws IOException, ExecutionException, InterruptedException {
        Future<String> future = ebElectricityUsageService.export( reportDTO);
        // 等待异步任务完成
        return future.get();
    }

}
