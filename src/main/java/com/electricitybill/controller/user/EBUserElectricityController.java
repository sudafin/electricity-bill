package com.electricitybill.controller.user;

import cn.hutool.json.JSONObject;
import com.electricitybill.entity.R;
import com.electricitybill.service.UserElectricityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/user/electricity")
@Api(tags = "用户端 - 用电记录接口")
public class EBUserElectricityController {
    @Resource
    UserElectricityService userElectricityService;

    @ApiOperation("获取用户用电统计数据")
    @PostMapping("statistics")
    public R<JSONObject> getElectricityStatistics(@RequestBody JSONObject jsonObject) {
        log.info("获取用户用电统计数据，参数: {}", jsonObject);
        JSONObject result = userElectricityService.getElectricityStatistics(jsonObject);
        return R.ok(result);
    }

    @ApiOperation("获取用户每日用电记录")
    @PostMapping("daily-usage")
    public R<JSONObject> getDailyElectricityUsage(@RequestBody JSONObject jsonObject) {
        log.info("获取用户每日用电记录，参数: {}", jsonObject);
        JSONObject result = userElectricityService.getDailyElectricityUsage(jsonObject);
        return R.ok(result);
    }

}