package com.electricitybill.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class ExecutorConfig {
    @Bean
    //对于执行IO密集型任务, 线程池线程数设置为CPU核心数的两倍
    public Executor generateReportExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 获取CPU核心数
        int cpuCores = Runtime.getRuntime().availableProcessors();
        // 核心线程数 = CPU核心数 * 2
        executor.setCorePoolSize(cpuCores * 2);
        // 最大线程数 = 核心线程数 * 2
        executor.setMaxPoolSize(cpuCores * 4);
        // 队列容量
        executor.setQueueCapacity(50);
        // 线程名称前缀
        executor.setThreadNamePrefix("report-export-");
        // 线程空闲时间，超过该时间空闲线程会被回收（单位：秒）
        executor.setKeepAliveSeconds(60);
        // 允许核心线程超时，这样空闲的核心线程也会被回收
        executor.setAllowCoreThreadTimeOut(true);
        // 拒绝策略：由调用者所在的线程来执行任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池
        executor.initialize();

        return executor;
    }
}
