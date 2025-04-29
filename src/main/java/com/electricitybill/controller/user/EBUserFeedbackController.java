package com.electricitybill.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.electricitybill.entity.R;
import com.electricitybill.service.UserFeedbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/feedback")
@Api(tags = "用户端 - 反馈接口")
public class EBUserFeedbackController {

    @Resource
    private  UserFeedbackService userFeedbackService;

    @GetMapping("/types")
    @ApiOperation("获取反馈类型列表")
    public R<List<String>> getFeedbackTypes() {
        log.info("获取反馈类型列表");
        List<String> types = userFeedbackService.getFeedbackTypes();
        return R.ok(types);
    }

    @PostMapping("/submit")
    @ApiOperation("提交用户反馈")
    public R<Boolean> submitFeedback(@RequestBody JSONObject feedbackData) {
        log.info("提交用户反馈: {}", feedbackData);
        boolean result = userFeedbackService.submitFeedback(
            feedbackData.getString("category"),
            feedbackData.getString("title"),
            feedbackData.getString("content"),
            feedbackData.getString("contact")
        );
        return R.ok(result);
    }
}