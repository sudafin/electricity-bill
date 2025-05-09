package com.electricitybill.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.electricitybill.entity.R;
import com.electricitybill.service.UserNotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/user/notification")
@Api(tags = "用户端 - 通知接口")
public class EBUserNotificationController {

    @Resource
    private UserNotificationService userNotificationService;


    @GetMapping("/list")
    @ApiOperation("获取通知列表")
    public R<JSONObject> getNotifications(
            @ApiParam(value = "通知状态：'全部'或'已读'") @RequestParam(required = false) String status,
            @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @ApiParam(value = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取通知列表: status={}, pageNo={}, pageSize={}", status, pageNo, pageSize);
        JSONObject notificationsList = userNotificationService.getNotifications(status, pageNo, pageSize);
        return R.ok(notificationsList);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("获取通知详情")
    public R<JSONObject> getNotificationDetail(
            @ApiParam(value = "通知ID") @PathVariable Long id) {
        log.info("获取通知详情: id={}", id);
        JSONObject detail = userNotificationService.getNotificationDetail(id);
        return R.ok(detail);
    }



}