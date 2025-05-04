package com.electricitybill.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.po.EbNotification;
import com.electricitybill.entity.po.EbNotificationRecipient;
import com.electricitybill.entity.vo.notification.NotificationPageVO;
import com.electricitybill.enums.NotificationType;
import com.electricitybill.enums.ReadStatusType;
import com.electricitybill.enums.ValidType;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.service.IEbNotificationRecipientService;
import com.electricitybill.service.IEbNotificationService;
import com.electricitybill.service.UserNotificationService;
import com.electricitybill.utils.AdminContextUtils;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.StringUtils;
import com.electricitybill.utils.UserContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final IEbNotificationService notificationService;
    private final IEbNotificationRecipientService notificationRecipientService;

    @Override
    public JSONObject getNotifications(String status, Integer pageNo, Integer pageSize) {
        Long currentUserId = UserContextUtils.getUserId();

        // 查询当前用户接收的所有通知记录（feedback 类型才有记录）
        List<EbNotificationRecipient> recipientList = notificationRecipientService.lambdaQuery()
                .eq(EbNotificationRecipient::getRecipientId, currentUserId)
                .eq(EbNotificationRecipient::getRecipientType, "user")
                .list();

        // 将 recipient 映射为 Map<通知ID, Recipient>，提高查询效率
        Map<Long, EbNotificationRecipient> recipientMap = recipientList.stream()
                .collect(Collectors.toMap(EbNotificationRecipient::getNotificationId, Function.identity(), (a, b) -> a));

        // 获取所有非内部通知
        List<EbNotification> allNotifications = notificationService.lambdaQuery()
                .ne(EbNotification::getType, NotificationType.INTERNAL_NOTIFICATION.getDesc())
                .eq(EbNotification::getValidType, ValidType.VALID.getValue())
                .list();

        List<EbNotification> filtered = new ArrayList<>();

        if ("未读".equals(status)) {
            for (EbNotification notification : allNotifications) {
                String type = notification.getType();
                Long id = notification.getId();
                EbNotificationRecipient recipient = recipientMap.get(id);

                if (NotificationType.ANNOUNCEMENT_NOTIFICATION.getDesc().equals(type)) {
                    // 公告：如果在 recipientMap 中不存在，说明未读
                    if (recipient == null) {
                        filtered.add(notification);
                    }
                } else if (NotificationType.FEEDBACK_NOTIFICATION.getDesc().equals(type)) {
                    // 反馈：如果存在，且未读
                    if (recipient != null &&recipient.getReadStatus().equals(ReadStatusType.UNREAD.getValue())) {
                        filtered.add(notification);
                    }
                }
            }
        } else {
            // status != 未读，说明是全部
            for (EbNotification notification : allNotifications) {
                String type = notification.getType();
                Long id = notification.getId();
                if (NotificationType.ANNOUNCEMENT_NOTIFICATION.getDesc().equals(type)) {
                    // 公告：都加入
                    filtered.add(notification);
                } else if (NotificationType.FEEDBACK_NOTIFICATION.getDesc().equals(type)) {
                    if (recipientMap.containsKey(id)) {
                        filtered.add(notification);
                    }
                }
            }
        }

        // 按创建时间倒序
        filtered.sort(Comparator.comparing(EbNotification::getCreatedAt).reversed());

        // 分页处理
        int total = filtered.size();
        int fromIndex = Math.min((pageNo - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<EbNotification> pageList = filtered.subList(fromIndex, toIndex);

        // 构造返回 JSON
        JSONArray jsonArray = new JSONArray();
        for (EbNotification notification : pageList) {
            JSONObject item = new JSONObject();
            item.put("id", notification.getId());
            item.put("title", notification.getTitle());

            String content = notification.getContent();
            item.put("content", content != null && content.length() > 60 ? content.substring(0, 60) + "..." : content);
            item.put("type", notification.getType());
            item.put("createdAt", notification.getCreatedAt());

            EbNotificationRecipient recipient = recipientMap.get(notification.getId());
            if (!"未读".equals(status)) {
                if (recipient != null) {
                    item.put("readStatus", recipient.getReadStatus());
                    item.put("readTime", recipient.getReadTime());
                } else {
                    item.put("readStatus", 0);
                    item.put("readTime", null);
                }
            }
            jsonArray.add(item);
        }

        JSONObject result = new JSONObject();
        result.put("list", jsonArray);
        result.put("total", total);
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }


    @Override
    @Transactional
    public JSONObject getNotificationDetail(Long id) {
        Long userId = UserContextUtils.getUserId();
        // 查询通知详情
        EbNotification notification = notificationService.getById(id);
        if (notification == null) {
            throw new BizIllegalException("通知不存在");
        }
        JSONObject detail = new JSONObject();
        detail.put("id", notification.getId());
        detail.put("title", notification.getTitle());
        detail.put("content", notification.getContent());
        detail.put("type", notification.getType());
        detail.put("createdAt", notification.getCreatedAt());
        detail.put("expireTime", notification.getExpireTime());
        detail.put("senderType", notification.getSenderType());
        if (notification.getType().equals(NotificationType.ANNOUNCEMENT_NOTIFICATION.getDesc())) {
            EbNotificationRecipient recipient = notificationRecipientService.getOne(new LambdaQueryWrapper<EbNotificationRecipient>()
                    .eq(EbNotificationRecipient::getNotificationId, id)
                    .eq(EbNotificationRecipient::getRecipientId, userId)
            );
            if (recipient != null) {
                return detail;
            }
            recipient = new EbNotificationRecipient();
            //插入数据
            recipient.setNotificationId(id);
            recipient.setRecipientId(userId);
            recipient.setReadStatus(ReadStatusType.READ.getValue());
            recipient.setReadTime(LocalDateTime.now());
            recipient.setRecipientType("user");
            notificationRecipientService.save(recipient);
        } else if (notification.getType().equals(NotificationType.FEEDBACK_NOTIFICATION.getDesc())) {
            // 如果是反馈那么 查询用户是否是该通知的接收者
            LambdaQueryWrapper<EbNotificationRecipient> query = new LambdaQueryWrapper<>();
            query.eq(EbNotificationRecipient::getNotificationId, id)
                    .eq(EbNotificationRecipient::getRecipientId, userId)
                    .eq(EbNotificationRecipient::getRecipientType, "user");
            EbNotificationRecipient recipient = notificationRecipientService.getOne(query);
            if (recipient == null) {
                log.warn("用户不是该通知的接收者: userId={}, notificationId={}", userId, id);
                return new JSONObject();
            }
            detail.put("readStatus", recipient.getReadStatus());
            detail.put("readTime", recipient.getReadTime());
            // 如果是未读状态，自动标记为已读
            if (recipient.getReadStatus() == ReadStatusType.UNREAD.getValue()) {
                recipient.setReadStatus(ReadStatusType.READ.getValue());
                recipient.setReadTime(LocalDateTime.now());
                notificationRecipientService.updateById(recipient);
            }
        } else {
            log.warn("未知的通知类型: {}", notification.getType());
            return new JSONObject();
        }

        return detail;
    }


}