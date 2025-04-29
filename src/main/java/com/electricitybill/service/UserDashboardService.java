package com.electricitybill.service;

import com.alibaba.fastjson.JSONObject;

public interface UserDashboardService {
    
    /**
     * 获取用户仪表盘数据
     * 返回包括用户信息、电表信息和当前账单信息的综合数据
     * @return 仪表盘数据的JSON对象
     */
    JSONObject getDashboardData();
    
    /**
     * 获取用户未读通知数量
     * @return 未读通知数量
     */
    int getUnreadNotificationCount();
    
    /**
     * 获取用户最新通知列表
     * @param limit 返回通知数量限制
     * @return 最新通知列表的JSON对象
     */
    JSONObject getLatestNotifications(Integer limit);
}