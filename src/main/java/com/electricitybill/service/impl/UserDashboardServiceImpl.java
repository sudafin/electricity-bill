package com.electricitybill.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.entity.po.*;
import com.electricitybill.enums.NotificationType;
import com.electricitybill.enums.ReadStatusType;
import com.electricitybill.enums.ValidType;
import com.electricitybill.service.*;
import com.electricitybill.utils.UserContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDashboardServiceImpl implements UserDashboardService {

    private final IEbUserService userService;
    private final IEbMeterService meterService;
    private final IEbBillService billService;
    private final IEbNotificationService notificationService;
    private final IEbNotificationRecipientService notificationRecipientService;
    private final IEbElectricityUsageService electricityUsageService;

    @Override
    public JSONObject getDashboardData() {
        JSONObject dashboardData = new JSONObject();
        
        // 获取当前登录用户ID
        Long userId = UserContextUtils.getUserId();
        
        // 获取用户基本信息
        EbUser user = userService.getById(userId);
        if (user == null) {
            log.error("用户不存在: {}", userId);
            return dashboardData;
        }
        
        JSONObject userInfo = new JSONObject();
        userInfo.put("userId", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("phone", user.getPhone());
        userInfo.put("address", user.getAddress());
        userInfo.put("userType", user.getUserType());
        userInfo.put("accountStatus", user.getAccountStatus());
        userInfo.put("lastPaymentDate", user.getLastPaymentDate());
        
        dashboardData.put("userInfo", userInfo);
        
        // 获取电表信息
        EbMeter meter = null;
        if (user.getMeterId() != null && !user.getMeterId().isEmpty()) {
            meter = meterService.getById(user.getMeterId());
        }
        
        JSONObject meterInfo = new JSONObject();
        if (meter != null) {
            meterInfo.put("meterId", meter.getId());
            meterInfo.put("installPlace", meter.getInstallPlace());
            meterInfo.put("status", meter.getStatus());
            meterInfo.put("lastMeterReadingDate", meter.getLastMeterReadingDate());
            meterInfo.put("currentReading", meter.getEndingReading());
            
            // 获取最近一个月的用电量统计
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            LambdaQueryWrapper<EbElectricityUsage> usageQuery = new LambdaQueryWrapper<>();
            usageQuery.eq(EbElectricityUsage::getUserId, userId)
                    .eq(EbElectricityUsage::getMeterId, meter.getId())
                    .ge(EbElectricityUsage::getEndTime, oneMonthAgo)
                    .orderByDesc(EbElectricityUsage::getEndTime);
            
            List<EbElectricityUsage> recentUsages = electricityUsageService.list(usageQuery);
            
            // 用电量统计
            double totalUsage = 0.0;
            double peakUsage = 0.0;
            double flatUsage = 0.0;
            double valleyUsage = 0.0;
            
            for (EbElectricityUsage usage : recentUsages) {
                double amount = usage.getUsageAmount().doubleValue();
                totalUsage += amount;
                
                String periodType = usage.getPeriodType().toLowerCase();
                if ("peak".equals(periodType)) {
                    peakUsage += amount;
                } else if ("flat".equals(periodType)) {
                    flatUsage += amount;
                } else if ("valley".equals(periodType)) {
                    valleyUsage += amount;
                }
            }
            
            JSONObject usageStats = new JSONObject();
            usageStats.put("totalUsage", totalUsage);
            usageStats.put("peakUsage", peakUsage);
            usageStats.put("flatUsage", flatUsage);
            usageStats.put("valleyUsage", valleyUsage);
            usageStats.put("period", "最近30天");
            
            meterInfo.put("recentUsage", usageStats);
        } else {
            meterInfo.put("meterId", "未绑定电表");
            meterInfo.put("status", "无效");
        }
        
        dashboardData.put("meterInfo", meterInfo);
        
        // 获取最近未缴费账单
        LambdaQueryWrapper<EbBill> billQuery = new LambdaQueryWrapper<>();
        billQuery.eq(EbBill::getUserId, userId)
                .eq(EbBill::getStatus, "未支付")
                .orderByDesc(EbBill::getCreatedAt)
                .last("LIMIT 1");
        
        EbBill latestUnpaidBill = billService.getOne(billQuery);
        
        // 获取最近已缴费账单
        LambdaQueryWrapper<EbBill> paidBillQuery = new LambdaQueryWrapper<>();
        paidBillQuery.eq(EbBill::getUserId, userId)
                .eq(EbBill::getStatus, "已支付")
                .orderByDesc(EbBill::getCreatedAt)
                .last("LIMIT 1");
        
        EbBill latestPaidBill = billService.getOne(paidBillQuery);
        
        JSONObject billInfo = new JSONObject();
        
        if (latestUnpaidBill != null) {
            JSONObject unpaidBill = new JSONObject();
            unpaidBill.put("billId", latestUnpaidBill.getId());
            unpaidBill.put("amount", latestUnpaidBill.getTotalAmount());
            unpaidBill.put("usageAmount", latestUnpaidBill.getUsageAmount());
            unpaidBill.put("dueDate", latestUnpaidBill.getDueDate());
            unpaidBill.put("createdAt", latestUnpaidBill.getCreatedAt());
            
            billInfo.put("unpaidBill", unpaidBill);
        }
        
        if (latestPaidBill != null) {
            JSONObject paidBill = new JSONObject();
            paidBill.put("billId", latestPaidBill.getId());
            paidBill.put("amount", latestPaidBill.getTotalAmount());
            paidBill.put("usageAmount", latestPaidBill.getUsageAmount());
            paidBill.put("paymentTime", latestPaidBill.getPaymentTime());
            paidBill.put("paymentMethod", latestPaidBill.getPaymentMethod());
            
            billInfo.put("paidBill", paidBill);
        }
        
        // 获取账单总览
        LambdaQueryWrapper<EbBill> billStatsQuery = new LambdaQueryWrapper<>();
        billStatsQuery.eq(EbBill::getUserId, userId);
        
        long totalBills = billService.count(billStatsQuery);
        
        billStatsQuery.clear();
        billStatsQuery.eq(EbBill::getUserId, userId)
                .eq(EbBill::getStatus, "未支付");
        long unpaidCount = billService.count(billStatsQuery);
        
        billInfo.put("totalBills", totalBills);
        billInfo.put("unpaidCount", unpaidCount);
        
        dashboardData.put("billInfo", billInfo);
        
        return dashboardData;
    }

    @Override
    public int getUnreadNotificationCount() {
        Long currentUserId = UserContextUtils.getUserId();

        // 用户通知接收记录（反馈通知）
        List<EbNotificationRecipient> recipientList = notificationRecipientService.lambdaQuery()
                .eq(EbNotificationRecipient::getRecipientId, currentUserId)
                .eq(EbNotificationRecipient::getRecipientType, "user")
                .list();

        Map<Long, EbNotificationRecipient> recipientMap = recipientList.stream()
                .collect(Collectors.toMap(EbNotificationRecipient::getNotificationId, Function.identity(), (a, b) -> a));

        // 所有有效通知（不包括内部通知）
        List<EbNotification> allNotifications = notificationService.lambdaQuery()
                .ne(EbNotification::getType, NotificationType.INTERNAL_NOTIFICATION.getDesc())
                .eq(EbNotification::getValidType, ValidType.VALID.getValue())
                .list();

        int unreadCount = 0;

        for (EbNotification notification : allNotifications) {
            String type = notification.getType();
            Long id = notification.getId();
            EbNotificationRecipient recipient = recipientMap.get(id);

            if (NotificationType.ANNOUNCEMENT_NOTIFICATION.getDesc().equals(type)) {
                // 公告：如果用户从未接收过，视为未读
                if (recipient == null) {
                    unreadCount++;
                }
            } else if (NotificationType.FEEDBACK_NOTIFICATION.getDesc().equals(type)) {
                // 反馈：有接收记录且状态为未读
                if (recipient != null && recipient.getReadStatus().equals(ReadStatusType.UNREAD.getValue())) {
                    unreadCount++;
                }
            }
        }

        return unreadCount;
    }

    @Override
    public JSONObject getLatestNotifications(Integer limit) {
        Long userId = UserContextUtils.getUserId();
        List<EbNotification> announcmentList = notificationService.lambdaQuery()
                .eq(EbNotification::getType, NotificationType.ANNOUNCEMENT_NOTIFICATION.getDesc())
                .eq(EbNotification::getValidType, ValidType.VALID.getValue())
                .orderByDesc(EbNotification::getCreatedAt)
                .page(new Page<>(1, limit)).getRecords();

        
        // 构建结果JSON
        JSONObject result = new JSONObject();
        JSONArray notifications = new JSONArray();
        
        for (EbNotification notification : announcmentList) {
            EbNotificationRecipient one = notificationRecipientService.lambdaQuery()
                    .eq(EbNotificationRecipient::getNotificationId, notification.getId())
                    .eq(EbNotificationRecipient::getRecipientId, userId).one();
            JSONObject notificationItem = new JSONObject();
            notificationItem.put("id", notification.getId());
            notificationItem.put("title", notification.getTitle());
            notificationItem.put("content", notification.getContent());
            notificationItem.put("type", notification.getType());
            notificationItem.put("createdAt", notification.getCreatedAt());
            notificationItem.put("readStatus", one == null ? 0 : 1);
            notificationItem.put("readTime", one != null ? one.getReadTime() : null);
            notifications.add(notificationItem);
        }
        
        result.put("list", notifications);
        result.put("total", notifications.size());
        
        return result;
    }
}