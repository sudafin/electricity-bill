package com.electricitybill.utils;

import com.electricitybill.config.properties.TTLProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * TTL生成工具类
 */
@Slf4j
public class TTLGenerator {


    private static TTLProperties properties;

    /**
     * 初始化配置
     * 由 TTLConfiguration 调用
     */
    public static void initProperties(TTLProperties ttlProperties) {
        properties = ttlProperties;
    }

    private TTLGenerator() {
        // 私有构造函数
    }

    /**
     * 生成默认范围内的随机TTL
     */
    public static long generateDefaultRandomTTL() {
        return generate(
                properties.getDefaultRange().getMin(),
                properties.getDefaultRange().getMax()
        );
    }

    /**
     * 生成短期TTL
     */
    public static long generateShortTTL() {
        return generate(
                properties.getShortRange().getMin(),
                properties.getShortRange().getMax()
        );
    }

    /**
     * 生成长期TTL
     */
    public static long generateLongTTL() {
        return generate(
                properties.getLongRange().getMin(),
                properties.getLongRange().getMax()
        );
    }

    /**
     * 生成临时TTL
     */
    public static long generateTempTTL() {
        return generate(
                properties.getTempRange().getMin(),
                properties.getTempRange().getMax()
        );
    }

    /**
     * 生成指定范围内的随机TTL
     */
    public static long generate(int minSeconds, int maxSeconds) {
        validateRange(minSeconds, maxSeconds);
        long ttl = ThreadLocalRandom.current().nextInt(minSeconds, maxSeconds + 1);
        log.debug("Generated TTL: {} seconds (range: {}-{})", ttl, minSeconds, maxSeconds);
        return ttl;
    }

    /**
     * 生成指定范围和时间单位的随机TTL
     */
    public static long generate(int min, int max, TimeUnit unit) {
        long minSeconds = unit.toSeconds(min);
        long maxSeconds = unit.toSeconds(max);
        validateRange((int) minSeconds, (int) maxSeconds);
        long ttl = ThreadLocalRandom.current().nextInt((int) minSeconds, (int) maxSeconds + 1);
        log.debug("Generated TTL: {} seconds (range: {}-{} {})",
                ttl, min, max, unit.name().toLowerCase());
        return ttl;
    }

    /**
     * 生成分钟范围的随机TTL
     */
    public static long generateMinutes(int minMinutes, int maxMinutes) {
        return generate(minMinutes, maxMinutes, TimeUnit.MINUTES);
    }

    /**
     * 生成小时范围的随机TTL
     */
    public static long generateHours(int minHours, int maxHours) {
        return generate(minHours, maxHours, TimeUnit.HOURS);
    }

    /**
     * 生成天范围的随机TTL
     */
    public static long generateDays(int minDays, int maxDays) {
        return generate(minDays, maxDays, TimeUnit.DAYS);
    }

    /**
     * 验证TTL范围的有效性
     */
    private static void validateRange(int minSeconds, int maxSeconds) {
        if (minSeconds < 0) {
            throw new IllegalArgumentException("Minimum TTL cannot be negative");
        }
        if (maxSeconds <= minSeconds) {
            throw new IllegalArgumentException("Maximum TTL must be greater than minimum TTL");
        }
        if (maxSeconds > TimeUnit.DAYS.toSeconds(30)) {
            throw new IllegalArgumentException("Maximum TTL cannot exceed 30 days");
        }
    }
}