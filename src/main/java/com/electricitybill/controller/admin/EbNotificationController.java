package com.electricitybill.controller.admin;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.notification.NotificationDTO;
import com.electricitybill.entity.dto.notification.NotificationPageQuery;
import com.electricitybill.entity.vo.notification.NotificationDetailVO;
import com.electricitybill.entity.vo.notification.NotificationPageVO;
import com.electricitybill.service.IEbNotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
@RequestMapping("/admin/notification")
@Api(tags = "通知管理")
public class EbNotificationController {

    @Resource
    private IEbNotificationService ebNotificationService;

    @ApiOperation("分页查询")
    @GetMapping("/page")
    public PageDTO<NotificationPageVO> queryPage(NotificationPageQuery notificationPageQuery) {
        return ebNotificationService.queryPage(notificationPageQuery);
    }

    @ApiOperation("获取通知详情")
    @GetMapping("/detail/{id}")
    public NotificationDetailVO queryNotificationDetail(@PathVariable(name = "id") Long notificationId) {
        return ebNotificationService.queryNotificationDetail(notificationId);
    }

    @ApiOperation("新增通知")
    @PostMapping("/create")
    public R create(@RequestBody NotificationDTO notificationDTO) {
        return ebNotificationService.create(notificationDTO);
    }

    @ApiOperation("删除通知")
    @DeleteMapping("/delete")
    public R deleteNotification(@RequestParam(name = "ids") List<Long> ids) {
        return ebNotificationService.deleteNotification(ids);
    }
}
