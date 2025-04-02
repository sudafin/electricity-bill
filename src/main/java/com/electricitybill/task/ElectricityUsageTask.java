package com.electricitybill.task;

import com.electricitybill.service.IEbElectricityUsageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author huangdada
 * @version 1.0
 * @description TODO
 * 2025/03/27/18:32
 */
@Component
@Slf4j
public class ElectricityUsageTask {
    @Resource
    private IEbElectricityUsageService ebElectricityUsageService;

    //每天0点执行一次
    @Scheduled(cron = "0 0 0 ? * *")
    public void setDailyUsageSummary() {
        ebElectricityUsageService.calculateElectricityUsageSummaryDay();
    }

    //每月月底执行一次
    @Scheduled(cron = "0 0 0 L * ?")
    public void setMonthlyUsageSummary() {
        ebElectricityUsageService.calculateElectricityUsageSummaryMonth();
    }

}
