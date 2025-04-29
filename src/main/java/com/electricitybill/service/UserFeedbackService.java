package com.electricitybill.service;

import java.util.List;

public interface UserFeedbackService {
    
    /**
     * 获取反馈类型列表
     * @return 反馈类型列表
     */
    List<String> getFeedbackTypes();
    
    /**
     * 提交用户反馈
     * @param category 反馈类型
     * @param title 反馈标题
     * @param content 反馈内容
     * @param contact 联系方式
     * @return 提交是否成功
     */
    boolean submitFeedback(String category, String title, String content, String contact);
}