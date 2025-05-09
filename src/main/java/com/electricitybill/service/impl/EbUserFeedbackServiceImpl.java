package com.electricitybill.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.feedback.FeedBackPageQuery;
import com.electricitybill.entity.dto.feedback.FeedBackProcessDTO;
import com.electricitybill.entity.dto.feedback.FeedBackSubmitDTO;
import com.electricitybill.entity.po.*;
import com.electricitybill.entity.vo.feedback.FeedBackDetailVO;
import com.electricitybill.entity.vo.feedback.FeedBackPageVO;
import com.electricitybill.enums.*;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.mapper.*;
import com.electricitybill.service.IEbUserFeedbackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-17
 */
@Service
public class EbUserFeedbackServiceImpl extends ServiceImpl<EbUserFeedbackMapper, EbUserFeedback> implements IEbUserFeedbackService {
    @Resource
    private EbUserMapper ebUserMapper;
    @Resource
    private EbAdminMapper ebAdminMapper;
    @Resource
    private EbNotificationRecipientMapper ebNotificationRecipientMapper;
    @Resource
    private EbNotificationMapper ebNotificationMapper;
    @Override
    public PageDTO<FeedBackPageVO> queryFeedBackPage(FeedBackPageQuery feedBackPageQuery) {
        Page<EbUserFeedback> page = new Page<>(feedBackPageQuery.getPageNo(), feedBackPageQuery.getPageSize());
        Page<EbUserFeedback> ebUserFeedbackPage = lambdaQuery().eq(StringUtils.isNotBlank(feedBackPageQuery.getFeedbackType()), EbUserFeedback::getFeedbackType, feedBackPageQuery.getFeedbackType())
                .eq(StringUtils.isNotBlank(feedBackPageQuery.getFeedbackStatus()), EbUserFeedback::getFeedbackStatus, feedBackPageQuery.getFeedbackStatus())
                .eq(StringUtils.isNotBlank(feedBackPageQuery.getFeedbackId()), EbUserFeedback::getId, feedBackPageQuery.getFeedbackId())
                .between(feedBackPageQuery.getStartDate() != null && feedBackPageQuery.getEndDate() != null, EbUserFeedback::getSubmitTime, feedBackPageQuery.getStartDate(), feedBackPageQuery.getEndDate())
                .orderByDesc(EbUserFeedback::getSubmitTime)
                .page(page);
        if(ebUserFeedbackPage.getRecords() == null){
            return PageDTO.empty(page);
        }
        // 封装数据
        List<EbUserFeedback> records = ebUserFeedbackPage.getRecords();
        List<FeedBackPageVO> feedBackPageVOS = records.stream().map(ebUserFeedback -> {
            FeedBackPageVO feedBackPageVO = BeanUtils.copyBean(ebUserFeedback, FeedBackPageVO.class);
            EbUser ebUser = ebUserMapper.selectById(ebUserFeedback.getUserId());
            if (ebUser == null) {
                throw new BizIllegalException(Constant.USER_NOT_EXIST);
            }
            feedBackPageVO.setUserName(ebUser.getUsername());
            return feedBackPageVO;
        }).collect(Collectors.toList());
        return PageDTO.of(ebUserFeedbackPage, feedBackPageVOS);
    }

    @Override
    public FeedBackDetailVO getFeedBackDetail(Long feedbackId) {
        EbUserFeedback ebUserFeedback = getById(feedbackId);
        if (ebUserFeedback == null){
            throw new BizIllegalException(Constant.FEEDBACK_NOT_EXIST);
        }
        FeedBackDetailVO feedBackDetailVO = BeanUtils.copyBean(ebUserFeedback, FeedBackDetailVO.class);

        Long userId = ebUserFeedback.getUserId();
        Long processorId = ebUserFeedback.getProcessorId();
        EbUser ebUser = ebUserMapper.selectById(userId);
        if (ebUser == null){
            throw new BizIllegalException(Constant.USER_NOT_EXIST);
        }
        EbAdmin ebAdmin = ebAdminMapper.selectById(processorId);
        if (ebAdmin != null){
            feedBackDetailVO.setProcessorName(ebAdmin.getAdminName());
        }
        feedBackDetailVO.setUserName(ebUser.getUsername());
        return feedBackDetailVO;
    }

    @Override
    @Transactional
    public R<Object> processFeedBack(FeedBackProcessDTO feedBackProcessDTO) {
        EbUserFeedback ebUserFeedback = getById(feedBackProcessDTO.getFeedbackId());
        if (ebUserFeedback == null){
            throw new BizIllegalException(Constant.FEEDBACK_NOT_EXIST);
        }
        Long adminId = AdminContextUtils.getAdminId();
        EbAdmin ebAdmin = ebAdminMapper.selectById(adminId);
        if (ebAdmin == null){
            throw new BizIllegalException(Constant.ADMIN_NOT_EXIST);
        }
        if(!FeedbackStatusType.getFeedbackTypeList().contains(feedBackProcessDTO.getFeedbackStatus())){
            throw new BizIllegalException("状态非法");
        }
        FeedbackStatusType feedbackStatusType = FeedbackStatusType.value(feedBackProcessDTO.getFeedbackStatus());
        if (ebUserFeedback.getFeedbackStatus().equals(feedbackStatusType.getDesc())) {
            throw new BizIllegalException("重复状态");
        }
        ebUserFeedback.setFeedbackStatus(feedbackStatusType.getDesc());
        if(StringUtils.isNotBlank(feedBackProcessDTO.getResponse())){
            ebUserFeedback.setResponse(feedBackProcessDTO.getResponse());
        }
        updateById(ebUserFeedback);
        //设置通知
        EbNotification ebNotification = new EbNotification();
        long notificationId = IdUtil.getSnowflakeNextId();
        ebNotification.setId(notificationId);
        ebNotification.setValidType(ValidType.VALID.getValue());
        ebNotification.setContent(feedBackProcessDTO.getResponse());
        ebNotification.setSenderId(adminId);
        ebNotification.setSenderType("admin");
        ebNotification.setTitle("反馈处理返回信息："+ebUserFeedback.getContent());
        ebNotification.setType(NotificationType.FEEDBACK_NOTIFICATION.getDesc());
        ebNotificationMapper.insert(ebNotification);
        //设置接受人
        EbNotificationRecipient ebNotificationRecipient = new EbNotificationRecipient();
        ebNotificationRecipient.setNotificationId(notificationId);
        ebNotificationRecipient.setRecipientType("user");
        ebNotificationRecipient.setRecipientId(ebUserFeedback.getUserId());
        ebNotificationRecipient.setReadStatus(ReadStatusType.UNREAD.getValue());
        ebNotificationRecipientMapper.insert(ebNotificationRecipient);
        return R.ok(null);
    }

    @Override
    public Map<String, List<String>> getFeedbackType() {
        HashMap<String, List<String>> res = new HashMap<>();
        if(CollUtils.isNotEmpty(FeedbackStatusType.getFeedbackTypeList())) {
            res.put("feedbackStatus", FeedbackStatusType.getFeedbackTypeList());
        }
        if(CollUtils.isNotEmpty(FeedbackType.getFeedbackTypeList())) {
            res.put("feedbackType", FeedbackType.getFeedbackTypeList());
        }
        return res;
    }

    @Override
    public R submitFeedBack(FeedBackSubmitDTO feedBackSubmitDTO) {
        if (StringUtils.isBlank(feedBackSubmitDTO.getContent())){
            return R.error("反馈内容不能为空");
        }
        if (StringUtils.isBlank(feedBackSubmitDTO.getFeedbackType())){
            return R.error("反馈类型不能为空");
        }
        if (!FeedbackType.getFeedbackTypeList().contains(feedBackSubmitDTO.getFeedbackType())){
            return R.error("反馈类型错误");
        }
        EbUserFeedback ebUserFeedback = new EbUserFeedback();
        ebUserFeedback.setContent(feedBackSubmitDTO.getContent());
        ebUserFeedback.setFeedbackType(feedBackSubmitDTO.getFeedbackType());
        ebUserFeedback.setFeedbackStatus(FeedbackStatusType.PENDING.getDesc());
        ebUserFeedback.setUserId(UserContextUtils.getUserId() );
        save(ebUserFeedback);
        return R.ok();
    }
}
