package com.electricitybill.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.electricitybill.entity.R;
import com.electricitybill.service.UserDashboardService;
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
@RequestMapping("/user")
@Api(tags = "用户端 - 仪表板接口")
@RequiredArgsConstructor
public class EBUserDashboardController {

    private final UserDashboardService userDashboardService;

    @GetMapping("/dashboard")
    @ApiOperation("获取用户仪表盘数据，包括用户信息、电表信息和当前账单信息")
    public R<JSONObject> getDashboardData() {
        log.info("获取用户仪表盘数据");
        JSONObject dashboardData = userDashboardService.getDashboardData();
        return R.ok(dashboardData);
    }

    @GetMapping("/notification/unread-count")
    @ApiOperation("获取用户未读通知数量")
    public R<Integer> getUnreadNotificationCount() {
        log.info("获取用户未读通知数量");
        int unreadCount = userDashboardService.getUnreadNotificationCount();
        return R.ok(unreadCount);
    }


    /**
     * 这里默认调用公告通知,而不是反馈通知
     */
    @GetMapping("/notification/latest")
    @ApiOperation("获取用户最新通知列表")

    public R<JSONObject> getLatestNotifications(
            @ApiParam(value = "返回通知数量限制", example = "3") @RequestParam(required = false, defaultValue = "3") Integer limit) {
        log.info("获取用户最新通知列表，limit: {}", limit);
        JSONObject latestNotifications = userDashboardService.getLatestNotifications(limit);
        return R.ok(latestNotifications);
    }
}