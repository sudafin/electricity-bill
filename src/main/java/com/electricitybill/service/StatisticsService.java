package com.electricitybill.service;

import com.alibaba.fastjson.JSONObject;

public interface StatisticsService {

    /**
     * 获取电量统计数据
     *
     * @param granularity 时间粒度 (daily, monthly, yearly)
     * @param startDate   开始日期字符串
     * @param endDate     结束日期字符串
     * @return 包含统计数据的JSONObject
     */
    JSONObject getElectricityStatistics(String granularity, String startDate, String endDate);

    /**
     * 获取电费统计数据
     *
     * @param granularity 时间粒度
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return JSONObject
     */
    JSONObject getFeeStatistics(String granularity, String startDate, String endDate);

    /**
     * 获取反馈统计数据
     *
     * @param granularity 时间粒度
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return JSONObject
     */
    JSONObject getFeedbackStatistics(String granularity, String startDate, String endDate);

    /**
     * 获取对账统计数据
     *
     * @param granularity 时间粒度
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return JSONObject
     */
    JSONObject getReconciliationStatistics(String granularity, String startDate, String endDate);

    /**
     * 获取用户类型统计数据
     *
     * @param granularity 时间粒度
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return JSONObject
     */
    JSONObject getUserTypeStatistics(String granularity, String startDate, String endDate);

}