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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final IEbNotificationService notificationService;
    private final IEbNotificationRecipientService notificationRecipientService;

    @Override
    public JSONObject getNotifications(String status, Integer pageNo, Integer pageSize) {
        // 获取当前用户的通知接收信息,有可能是反馈也有可能是通知
        List<EbNotificationRecipient> ebNotificationRecipientList = notificationRecipientService.lambdaQuery()
                .eq(EbNotificationRecipient::getRecipientId, UserContextUtils.getUserId())
                .eq(EbNotificationRecipient::getRecipientType, "user")
                .list();

        // 获取所有通知（不分页）
        List<EbNotification> records = notificationService.lambdaQuery()
                .ne(EbNotification::getType, NotificationType.INTERNAL_NOTIFICATION.getDesc())
                .eq(EbNotification::getValidType, ValidType.VALID.getValue())
                .list();
        //拿到公告通知
        List<EbNotification> announcementList = records.stream().filter(ebNotification -> ebNotification.getType().equals(NotificationType.ANNOUNCEMENT_NOTIFICATION.getDesc())).collect(Collectors.toList());
        List<EbNotification> feedbackList = records.stream().filter(ebNotification -> ebNotification.getType().equals(NotificationType.FEEDBACK_NOTIFICATION.getDesc())).collect(Collectors.toList());
        //如果是已读的就查看feedback通知是否存在当前数据
        //两个通知集合进行合并
        List<EbNotification> mergedRecords = new ArrayList<>();
        if(status.equals("未读")){
            List<EbNotification> unreadAnnocementList = announcementList.stream().filter(ebNotification ->
                    ebNotificationRecipientList.stream()
                            .anyMatch(ebNotificationRecipient -> !ebNotificationRecipient.getNotificationId().equals(ebNotification.getId()))).collect(Collectors.toList());
            List<EbNotification> unreadFeedbackList = feedbackList.stream().filter(ebNotification ->
                            ebNotificationRecipientList.stream()
                                    .anyMatch(ebNotificationRecipient -> ebNotificationRecipient.getNotificationId().equals(ebNotification.getId())))
                    .collect(Collectors.toList());
            mergedRecords.addAll(unreadAnnocementList);
            mergedRecords.addAll(unreadFeedbackList);
        }else {
            //全部的处理
            List<EbNotification> feedbackFilteredRecords = feedbackList.stream()
                    .filter(ebNotification ->
                            ebNotificationRecipientList.stream()
                                    .anyMatch(ebNotificationRecipient -> ebNotificationRecipient.getNotificationId().equals(ebNotification.getId())))
                    .collect(Collectors.toList());

            mergedRecords.addAll(feedbackFilteredRecords);
            mergedRecords.addAll(announcementList);
        }
        //按照时间倒序
        CollUtils.sort(mergedRecords, (o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
        // 构建结果
        JSONObject result = new JSONObject();
        JSONArray notificationList = new JSONArray();

        // 如果过滤后的数据为空，直接返回空分页
        if (CollUtils.isEmpty(mergedRecords)) {
            result.put("list", notificationList);
            result.put("total", 0);
            result.put("pageNo", pageNo);
            result.put("pageSize", pageSize);
            return result;
        }

        // 创建分页对象，手动设置分页逻辑
        int total = mergedRecords.size();  // 总记录数
        // 计算分页范围
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        // 获取当前页的数据
        List<EbNotification> pageData = mergedRecords.subList(fromIndex, toIndex);


        // 转换为 VO 对象
        for (EbNotification notification : pageData) {
            JSONObject item = new JSONObject();
            item.put("id", notification.getId());
            item.put("title", notification.getTitle());
            // 摘要：截取内容前60个字符
            String content = notification.getContent();
            item.put("content", content != null && content.length() > 60 ? content.substring(0, 60) + "..." : content);
            item.put("type", notification.getType());
            item.put("createdAt", notification.getCreatedAt());
            if (notification.getType().equals(NotificationType.ANNOUNCEMENT_NOTIFICATION.getDesc())) {
                EbNotificationRecipient recipient = notificationRecipientService.getOne(new LambdaQueryWrapper<EbNotificationRecipient>()
                        .eq(EbNotificationRecipient::getNotificationId, notification.getId())
                        .eq(EbNotificationRecipient::getRecipientId, UserContextUtils.getUserId())
                );
                if (!status.equals("未读")) {
                    item.put("readStatus", recipient == null ? 0 : recipient.getReadStatus());
                    item.put("readTime", recipient == null ? null : recipient.getReadTime());
                } else {
                    if (recipient != null) {
                        continue;
                    }
                }
            } else if (notification.getType().equals(NotificationType.FEEDBACK_NOTIFICATION.getDesc())) {
                for (EbNotificationRecipient recipient : ebNotificationRecipientList) {
                    if (recipient.getNotificationId().equals(notification.getId())) {
                        if (!status.equals("未读")) {
                            item.put("readStatus", recipient.getReadStatus());
                            item.put("readTime", recipient.getReadTime());
                        }
                    }
                }
            }
            notificationList.add(item);
        }

        result.put("list", notificationList);
        result.put("total", total);
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public JSONObject getNotificationDetail(Long id) {
        Long userId = UserContextUtils.getUserId();

        // 查询通知详情
        EbNotification notification = notificationService.getById(id);
        if (notification == null) {
            log.warn("通知不存在: {}", id);
            return new JSONObject();
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
            if (recipient.getReadStatus() == 0) {
                EbNotificationRecipient update = new EbNotificationRecipient();
                update.setReadStatus(1);
                update.setReadTime(LocalDateTime.now());
                notificationRecipientService.update(update, query);
            }
        } else {
            log.warn("未知的通知类型: {}", notification.getType());
            return new JSONObject();
        }

        return detail;
    }


}