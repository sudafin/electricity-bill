package com.electricitybill.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.mapper.EbSystemLogMapper;
import com.electricitybill.service.IEbSystemLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class LogTask {
    @Resource
    private IEbSystemLogService ebSystemLogService;

    //每周一执行一次清除日志操作
    @Scheduled(cron = "0 0 0 ? * MON")
    public void clearLog(){
        ebSystemLogService.remove(new LambdaQueryWrapper<>());
    }

}
