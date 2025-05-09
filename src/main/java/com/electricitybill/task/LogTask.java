package com.electricitybill.task;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.entity.dto.notification.NotificationDTO;
import com.electricitybill.entity.po.EbBill;
import com.electricitybill.entity.po.EbNotification;
import com.electricitybill.entity.po.EbPayment;
import com.electricitybill.entity.po.EbScheduledTask;
import com.electricitybill.enums.BillType;
import com.electricitybill.enums.NotificationType;
import com.electricitybill.enums.ValidType;
import com.electricitybill.mapper.EbBillMapper;
import com.electricitybill.mapper.EbPaymentMapper;
import com.electricitybill.mapper.EbScheduledTaskMapper;
import com.electricitybill.mapper.EbSystemLogMapper;
import com.electricitybill.service.IEbBillService;
import com.electricitybill.service.IEbNotificationService;
import com.electricitybill.service.IEbSystemLogService;
import com.electricitybill.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class LogTask {
    @Resource
    private IEbSystemLogService ebSystemLogService;
    @Resource
    private IEbBillService ebBillService;
    @Resource
    private EbScheduledTaskMapper scheduledTaskMapper;
    @Resource
    private IEbNotificationService ebNotificationService;

    //每周一执行一次清除日志操作
    @Scheduled(cron = "0 0 0 ? * MON")
    public void clearLog(){
        ebSystemLogService.remove(new LambdaQueryWrapper<>());
    }

    //支付状态每10s执行一次
    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void compensateAlipayPayment() throws AlipayApiException {
        // 1. 查询未完成支付但已过回调时间的账单（如30分钟以上）
        List<EbBill> billList = ebBillService.lambdaQuery()
                .eq(EbBill::getStatus, BillType.UNPAID.getDesc())
                .orderByDesc(EbBill::getCreatedAt)
                .last("LIMIT 10")
                .list();
        for (EbBill ebBill : billList) {
            // 2. 调用支付宝接口主动查询支付状态（模拟验签 + 查询）
            String queryResult = ebBillService.queryStatus(ebBill.getId());
            }
    }


    /**
     * 每月1日 9:00 执行抄表提醒
     */
    @Scheduled(cron = "0 0 9 1 * ?")
    public void sendBillReminders() {
        List<EbScheduledTask> tasks = scheduledTaskMapper.selectList(
                new LambdaQueryWrapper<EbScheduledTask>()
                        .eq(EbScheduledTask::getTaskName, "账单提醒")
                        .eq(EbScheduledTask::getTaskStatus, 1)
        );

        for (EbScheduledTask task : tasks) {
            String userIds = task.getUserIds();
            if (StringUtils.isBlank(userIds)) continue;

            Arrays.stream(userIds.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(Long::parseLong)
                    .forEach(userId -> {
                        try {
                            EbNotification ebNotification = new EbNotification();
                            ebNotification.setTitle("账单提醒");
                            ebNotification.setContent("您好，本月的用电账单已经完成，请阅读！");
                            ebNotification.setType(NotificationType.ANNOUNCEMENT_NOTIFICATION.getDesc());
                            ebNotification.setExpireTime(LocalDateTime.now().plusDays(5));
                            ebNotification.setValidType(ValidType.VALID.getValue());
                            ebNotification.setSenderType("system");
                            ebNotification.setSenderId(0L);
                            ebNotificationService.save(ebNotification);
                        } catch (Exception e) {
                            log.error("发送抄表提醒失败，用户ID：" + userId, e);
                        }
                    });
        }
    }

    @Scheduled(cron = "0 0 9 1 * ?")
    public void sendPaymentReminders() {
        // 查询“缴费提醒”任务，状态为启用的任务
        List<EbScheduledTask> tasks = scheduledTaskMapper.selectList(
                new LambdaQueryWrapper<EbScheduledTask>()
                        .eq(EbScheduledTask::getTaskName, "缴费提醒")
                        .eq(EbScheduledTask::getTaskStatus, 1)
        );

        for (EbScheduledTask task : tasks) {
            String userIds = task.getUserIds();
            if (StringUtils.isBlank(userIds)) continue;

            // 遍历每个设置了提醒的用户
            Arrays.stream(userIds.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(Long::parseLong)
                    .forEach(userId -> {
                        try {
                            // 创建提醒通知
                            EbNotification ebNotification = new EbNotification();
                            ebNotification.setTitle("缴费提醒");
                            ebNotification.setContent("您好，您本月的电费账单已生成，请尽快完成缴费！");
                            ebNotification.setType(NotificationType.ANNOUNCEMENT_NOTIFICATION.getDesc());
                            ebNotification.setExpireTime(LocalDateTime.now().plusDays(5));
                            ebNotification.setValidType(ValidType.VALID.getValue());
                            ebNotification.setSenderType("system");
                            ebNotification.setSenderId(0L);
                            // 保存通知
                            ebNotificationService.save(ebNotification);
                        } catch (Exception e) {
                            log.error("发送缴费提醒失败，用户ID：" + userId, e);
                        }
                    });
        }
    }

}
