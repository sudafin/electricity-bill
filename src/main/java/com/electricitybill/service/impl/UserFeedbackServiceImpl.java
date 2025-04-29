package com.electricitybill.service.impl;

import com.electricitybill.entity.po.EbUserFeedback;
import com.electricitybill.enums.FeedbackStatusType;
import com.electricitybill.enums.FeedbackType;
import com.electricitybill.service.IEbUserFeedbackService;
import com.electricitybill.service.UserFeedbackService;
import com.electricitybill.utils.UserContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFeedbackServiceImpl implements UserFeedbackService {

    private final IEbUserFeedbackService ebUserFeedbackService;


    @Override
    public List<String> getFeedbackTypes() {
        // 返回预定义的类型列表
        // 也可以从数据库动态获取，如果有专门的类型表
        return FeedbackType.getFeedbackTypeList();
    }

    @Override
    public boolean submitFeedback(String category, String title, String content, String contact) {
        Long userId = UserContextUtils.getUserId();
        
        // 检查必填项
        if (category == null || content == null || content.trim().isEmpty()) {
            log.warn("反馈提交缺少必要字段: category={}, content={}", category, content);
            return false;
        }

        
        try {
            // 创建反馈记录
            EbUserFeedback feedback = new EbUserFeedback();
            feedback.setUserId(userId);
            feedback.setFeedbackType(category);
            
            // 处理标题和内容
            String fullContent;
            if (title != null && !title.trim().isEmpty()) {
                fullContent = "标题: " + title + "\n\n" + content;
            } else {
                fullContent = content;
            }
            
            // 如果有联系方式，添加到内容中
            if (contact != null && !contact.trim().isEmpty()) {
                fullContent += "\n\n联系方式: " + contact;
            }
            
            feedback.setContent(fullContent);
            feedback.setFeedbackStatus(FeedbackStatusType.PENDING.getDesc()); // 待处理状态
            feedback.setSubmitTime(LocalDateTime.now());
            feedback.setUserId(userId);
            // 保存到数据库
            return ebUserFeedbackService.save(feedback);
            
        } catch (Exception e) {
            log.error("提交反馈失败: ", e);
            return false;
        }
    }
}