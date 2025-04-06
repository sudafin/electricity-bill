package com.electricitybill.task;

import com.electricitybill.entity.po.EbScheduledTask;
import com.electricitybill.service.IEbElectricityUsageService;
import com.electricitybill.service.IEbScheduledTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author huangdada
 * @version 1.0
 * 2025/03/27/18:32
 */
@Component
@Slf4j
public class ElectricityUsageTask {
    @Resource
    private IEbElectricityUsageService ebElectricityUsageService;
    @Resource
    private IEbScheduledTaskService taskService;
    //每天0点执行一次，生成每天的用电量
    @Scheduled(cron = "0 0 0 ? * *")
    public void setDailyUsageSummary() {
        EbScheduledTask ebScheduledTask = taskService.lambdaQuery().eq(EbScheduledTask::getTaskName, "setDailyUsageSummary").one();
        if(ebScheduledTask == null){
            ebScheduledTask.setTaskDesc("生成每日的用电量");
            ebScheduledTask.setTaskName("setDailyUsageSummary");
            ebScheduledTask.setTaskType("抄表");
            ebScheduledTask.setCronExpression("0 0 0 ? * *");
            ebScheduledTask.setTaskStatus(1);
            taskService.save(ebScheduledTask);
        }
        if(ebScheduledTask.getTaskStatus() == 0){
            return;
        }
        ebElectricityUsageService.calculateElectricityUsageSummaryDay();
    }

    //每月月底执行一次，生成每月的订单
    @Scheduled(cron = "0 0 0 L * ?")
    public void getMonthlyBill() {
        EbScheduledTask ebScheduledTask = taskService.lambdaQuery().eq(EbScheduledTask::getTaskName, "getMonthlyBill").one();
        if(ebScheduledTask == null){
            ebScheduledTask.setTaskDesc("生成每月的订单");
            ebScheduledTask.setTaskName("getMonthlyBill");
            ebScheduledTask.setTaskType("账单生成");
            ebScheduledTask.setCronExpression("0 0 0 L * ?");
            ebScheduledTask.setTaskStatus(1);
        }
        if(ebScheduledTask.getTaskStatus() == 0){
            return;
        }
        ebElectricityUsageService.calculateMonthlyBill();
    }

}
