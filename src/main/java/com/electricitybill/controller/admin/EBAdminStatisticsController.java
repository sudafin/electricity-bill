package com.electricitybill.controller.admin;

import com.alibaba.fastjson.JSONObject;
import com.electricitybill.entity.R;
import com.electricitybill.service.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/statistics")
@Api(tags = "管理端 - 数据统计接口")
@RequiredArgsConstructor // Use constructor injection
public class EBAdminStatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/electricity")
    @ApiOperation("获取电量统计")
    public R<JSONObject> getElectricityStatistics(
            @ApiParam(value = "时间粒度", example = "daily", required = true) @RequestParam String granularity,
            @ApiParam(value = "开始日期 (yyyy-MM-dd HH:mm:ss)", required = true) @RequestParam String startDate,
            @ApiParam(value = "结束日期 (yyyy-MM-dd HH:mm:ss)", required = true) @RequestParam String endDate) {
        log.info("Request received for electricity statistics: granularity={}, startDate={}, endDate={}", granularity, startDate, endDate);
        JSONObject data = statisticsService.getElectricityStatistics(granularity, startDate, endDate);
        return R.ok(data);
    }

    @GetMapping("/fee")
    @ApiOperation("获取电费统计")
    public R<JSONObject> getFeeStatistics(
            @ApiParam(value = "时间粒度", example = "daily", required = true) @RequestParam String granularity,
            @ApiParam(value = "开始日期 (yyyy-MM-dd HH:mm:ss)", required = true) @RequestParam String startDate,
            @ApiParam(value = "结束日期 (yyyy-MM-dd HH:mm:ss)", required = true) @RequestParam String endDate) {
        log.info("Request received for fee statistics: granularity={}, startDate={}, endDate={}", granularity, startDate, endDate);
        JSONObject data = statisticsService.getFeeStatistics(granularity, startDate, endDate);
        return R.ok(data);
    }

   

    @GetMapping("/feedback")
    @ApiOperation("获取反馈统计")
    public R<JSONObject> getFeedbackStatistics(
            @ApiParam(value = "时间粒度", example = "daily", required = true) @RequestParam String granularity,
            @ApiParam(value = "开始日期 (yyyy-MM-dd HH:mm:ss)", required = true) @RequestParam String startDate,
            @ApiParam(value = "结束日期 (yyyy-MM-dd HH:mm:ss)", required = true) @RequestParam String endDate) {
        log.info("Request received for feedback statistics: granularity={}, startDate={}, endDate={}", granularity, startDate, endDate);
        JSONObject data = statisticsService.getFeedbackStatistics(granularity, startDate, endDate);
        return R.ok(data);
    }

    @GetMapping("/reconciliation")
    @ApiOperation("获取对账统计")
    public R<JSONObject> getReconciliationStatistics(
            @ApiParam(value = "时间粒度", example = "daily", required = true) @RequestParam String granularity,
            @ApiParam(value = "开始日期 (yyyy-MM-dd HH:mm:ss)", required = true) @RequestParam String startDate,
            @ApiParam(value = "结束日期 (yyyy-MM-dd HH:mm:ss)", required = true) @RequestParam String endDate) {
        log.info("Request received for reconciliation statistics: granularity={}, startDate={}, endDate={}", granularity, startDate, endDate);
        JSONObject data = statisticsService.getReconciliationStatistics(granularity, startDate, endDate);
        return R.ok(data);
    }

    @GetMapping("/user-type")
    @ApiOperation("获取用户类型统计")
    public R<JSONObject> getUserTypeStatistics(
            @ApiParam(value = "时间粒度", example = "daily", required = true) @RequestParam String granularity,
            @ApiParam(value = "开始日期 (yyyy-MM-dd HH:mm:ss)", required = true) @RequestParam String startDate,
            @ApiParam(value = "结束日期 (yyyy-MM-dd HH:mm:ss)", required = true) @RequestParam String endDate) {
        log.info("Request received for user type statistics: granularity={}, startDate={}, endDate={}", granularity, startDate, endDate);
        JSONObject data = statisticsService.getUserTypeStatistics(granularity, startDate, endDate);
        return R.ok(data);
    }

}