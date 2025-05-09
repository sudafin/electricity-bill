package com.electricitybill.service;


import cn.hutool.json.JSONObject;

public interface UserElectricityService {

    /**
     * 获取用户用电统计数据
     *
     * @param params 查询参数，包含startDate(开始日期)和endDate(结束日期)
     * @return 包含总用电量、总电费和日均用电量的统计数据
     */
    cn.hutool.json.JSONObject getElectricityStatistics(JSONObject params);

    /**
     * 获取用户每日用电记录
     * @param params 查询参数，包含startDate(开始日期)、endDate(结束日期)和days(查询天数)
     * @return 包含日期、用电量和电费的每日记录
     */
    JSONObject getDailyElectricityUsage(JSONObject params);

}