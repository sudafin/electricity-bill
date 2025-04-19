package com.electricitybill.controller.user;


import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.query.PageQuery;
import com.electricitybill.entity.vo.notification.NotificationUserVO;
import com.electricitybill.service.IEbNotificationService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/user/notification")
@Api(tags = "用户端通知管理")
public class EbUserNotificationController {
    @Resource
    private IEbNotificationService ebUserNotificationService;

    /**
     * 获取最新的通知列表前三条
     */
    @GetMapping("/newList")
    public PageDTO<NotificationUserVO> getNewNotificationList(PageQuery pageQuery) {
        return ebUserNotificationService.getNewNotificationList(pageQuery);
    }

}
