package com.electricitybill.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "redis.ttl")
@Component
@Data
public class TTLProperties {
    private Range defaultRange = new Range(3600, 7200);    // 默认1-2小时
    private Range shortRange = new Range(60, 300);         // 短期1-5分钟
    private Range longRange = new Range(7200, 14400);      // 长期2-4小时
    private Range tempRange = new Range(10, 30); // 临时10-30秒

    @Data
    @AllArgsConstructor
    public static class Range {
        private int min;
        private int max;
    }
}