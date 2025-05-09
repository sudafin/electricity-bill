package com.electricitybill.service;

import com.alibaba.fastjson.JSONObject;

public interface UserNotificationService {

    /**
     * 获取用户通知列表
     * @param status 通知状态：'全部'或'已读'
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 通知列表数据的JSON对象
     */
    JSONObject getNotifications(String status, Integer pageNo, Integer pageSize);
    
    /**
     * 获取通知详情
     * @param id 通知ID
     * @return 通知详情的JSON对象
     */
    JSONObject getNotificationDetail(Long id);

}