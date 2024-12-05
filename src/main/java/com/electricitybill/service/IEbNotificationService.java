package com.electricitybill.service;

import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.notification.NotificationDTO;
import com.electricitybill.entity.dto.notification.NotificationPageQuery;
import com.electricitybill.entity.po.EbNotification;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.notification.NotificationDetailVO;
import com.electricitybill.entity.vo.notification.NotificationPageVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbNotificationService extends IService<EbNotification> {

    PageDTO<NotificationPageVO> queryPage(NotificationPageQuery notificationPageQuery);

    NotificationDetailVO queryNotificationDetail(Long notificationId);

    R create(NotificationDTO notificationDTO);

    R deleteNotification(List<Long> ids);
}
