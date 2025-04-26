package com.electricitybill.utils;

import cn.hutool.core.date.LocalDateTimeUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Stream;

/**
 * 时间工具类,用于本地时间操作,包含LocalDateTimeUtil的所有方法和自定义的LocalDateTime的操作方法及常量
 *
 * @author wusongsong
 * @version 1.0.0 1.0
 * @see 1.0
 * @since 从哪个版本开始支持该类的功能
 */
public class DateUtils extends LocalDateTimeUtil {

    public static final String DEFAULT_YEAR_FORMAT = "yyyy";
    public static final String DEFAULT_MONTH_FORMAT = "yyyy-MM";
    public static final String DEFAULT_MONTH_FORMAT_SLASH = "yyyy/MM";
    public static final String DEFAULT_MONTH_FORMAT_EN = "yyyy年MM月";
    public static final String DEFAULT_MONTH_FORMAT_COMPACT = "yyyyMM";
    public static final String DEFAULT_WEEK_FORMAT = "yyyy-ww";
    public static final String DEFAULT_WEEK_FORMAT_EN = "yyyy年ww周";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_FORMAT_EN = "yyyy年MM月dd日";
    public static final String DEFAULT_DATE_FORMAT_COMPACT = "yyyyMMdd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_TIME_COMPACT = "yyyyMMddHHmmss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static final String DAY = "DAY";
    public static final String MONTH = "MONTH";
    public static final String WEEK = "WEEK";
    public static final long MAX_MONTH_DAY = 30L;
    public static final long MAX_3_MONTH_DAY = 90L;
    public static final long MAX_YEAR_DAY = 365L;

    public static final DateTimeFormatter SIGN_DATE_SUFFIX_FORMATTER =
            DateTimeFormatter.ofPattern(":yyyyMM");

    public static final String TIME_ZONE_8 = "GMT+8";
    public static final ZoneId zoneId = ZoneId.of("Asia/Shanghai");

    /**
     * 获取utc时间
     *
     * @param localDateTime 转化时间
     * @return utc时间
     */
    public static LocalDateTime getUTCTime(LocalDateTime localDateTime) {
        ZoneId australia = ZoneId.of("Asia/Shanghai");
        ZonedDateTime dateAndTimeInSydney = ZonedDateTime.of(localDateTime, australia);
        ZonedDateTime utcDate = dateAndTimeInSydney.withZoneSameInstant(ZoneOffset.UTC);
        return utcDate.toLocalDateTime();
    }

    /**
     * 获取Asia时间
     *
     * @param localDateTime 转化时间
     * @return Asia时间
     */
    public static LocalDateTime getAsiaTime(LocalDateTime localDateTime) {
        ZoneId australia = ZoneId.of("Asia/Shanghai");
        ZonedDateTime dateAndTimeInSydney = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
        ZonedDateTime utcDate = dateAndTimeInSydney.withZoneSameInstant(australia);
        return utcDate.toLocalDateTime();
    }

    /**
     * 获取某一天的开始：0点0分
     *
     * @param localDateTime 指定日期
     * @return 转换后的时间
     */
    public static LocalDateTime getDayStartTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toLocalDate().atStartOfDay();
    }

    /**
     * 获取某一天的结束：23点 59分 59秒的时间
     *
     * @param localDateTime 指定日期
     * @return 转换后的时间
     */
    public static LocalDateTime getDayEndTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.MAX);
    }

    public static Date addDays(int i) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_8));
        c.add(Calendar.DAY_OF_MONTH, i);
        return c.getTime();
    }



    public static LocalDate getMonthBegin(LocalDate date) {
        return LocalDate.of(date.getYear(), date.getMonth(), 1);
    }

    public static LocalDate getMonthEnd(LocalDate date) {
        return LocalDate.of(date.getYear(), date.getMonthValue() + 1, 1).minusDays(1);
    }

    public static LocalDateTime getMonthBeginTime(LocalDate date) {

        return LocalDate.of(date.getYear(), date.getMonth(), 1).atStartOfDay();
    }

    public static LocalDateTime getMonthEndTime(LocalDate date) {
        return LocalDate.of(date.getYear(), date.getMonthValue() + 1, 1)
                .minusDays(1).atTime(LocalTime.MAX);
    }

    public static LocalDateTime getWeekBeginTime(LocalDate now) {
        return now.minusDays(now.getDayOfWeek().getValue() - 1).atStartOfDay();
    }

    public static LocalDateTime getWeekEndTime(LocalDate now) {
        return LocalDateTime.of(now.plusDays(8 - now.getDayOfWeek().getValue()), LocalTime.MAX);
    }

    /**
     * 获取最近15天日期（不包含当天），格式MM.dd
     *
     * @return
     */
    public static List<String> last15Day(){
        // 1.定义日期列表
        List<String> days = new ArrayList<>();
        // 2.获取15天前的时间
        LocalDateTime time = now().minusDays(15);
        // 3.for循环遍历
        for (int count = 0; count < 15; count++){
            // 3.1.格式化时间
            days.add(String.format("%s.%s",
                    NumberUtils.repair0(time.getMonthValue(),2), NumberUtils.repair0(time.getDayOfMonth(), 2)));
            // 3.2.日期加1天
            time = time.plusDays(1);
        }
        // 4.返回结果
        return days;
    }
    /**
     * Generates a list of date keys (formatted strings) within a given date range,
     * based on the specified granularity.
     *
     * @param start       The start LocalDateTime of the range.
     * @param end         The end LocalDateTime of the range.
     * @param granularity The time granularity ("daily", "monthly", "yearly").
     * @param formatter   The DateTimeFormatter corresponding to the granularity.
     * @return A sorted List of date key strings. Returns an empty list if start or end is null.
     */
    public static List<String> getDateKeysInRange(LocalDateTime start, LocalDateTime end, String granularity, DateTimeFormatter formatter) {
        List<String> dateKeys = new ArrayList<>();

        if (start == null || end == null || start.isAfter(end)) {
            return dateKeys; // Return empty list for invalid range
        }

        granularity = granularity.toLowerCase();

        try {
            switch (granularity) {
                case "daily":
                    LocalDate startDateDaily = start.toLocalDate();
                    LocalDate endDateDaily = end.toLocalDate();
                    // Stream dates from start to end (inclusive)
                    Stream.iterate(startDateDaily, date -> date.plusDays(1))
                            .limit(ChronoUnit.DAYS.between(startDateDaily, endDateDaily) + 1)
                            .forEach(date -> dateKeys.add(date.format(formatter)));
                    break;

                case "monthly":
                    YearMonth startMonth = YearMonth.from(start);
                    YearMonth endMonth = YearMonth.from(end);
                    // Stream months from start to end (inclusive)
                    Stream.iterate(startMonth, month -> month.plusMonths(1))
                            .limit(ChronoUnit.MONTHS.between(startMonth, endMonth) + 1)
                            .forEach(month -> dateKeys.add(month.format(formatter)));
                    break;

                case "yearly":
                    Year startYear = Year.from(start);
                    Year endYear = Year.from(end);
                    // Stream years from start to end (inclusive)
                    Stream.iterate(startYear, year -> year.plusYears(1))
                            .limit(ChronoUnit.YEARS.between(startYear, endYear) + 1)
                            .forEach(year -> dateKeys.add(year.format(formatter)));
                    break;

                default:
                    // Default to daily if granularity is unknown
                    LocalDate startDateDefault = start.toLocalDate();
                    LocalDate endDateDefault = end.toLocalDate();
                    Stream.iterate(startDateDefault, date -> date.plusDays(1))
                            .limit(ChronoUnit.DAYS.between(startDateDefault, endDateDefault) + 1)
                            .forEach(date -> dateKeys.add(date.format(formatter)));
                    break;
            }
        } catch (Exception e) {
            // Log the error appropriately in a real application
            System.err.println("Error generating date keys for range: " + start + " to " + end + ", granularity: " + granularity + ". Error: " + e.getMessage());
            // Optionally, return an empty list or throw a custom exception
            return new ArrayList<>();
        }

        return dateKeys;
    }


}
