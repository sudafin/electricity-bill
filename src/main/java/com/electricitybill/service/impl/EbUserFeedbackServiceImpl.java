package com.electricitybill.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.feedback.FeedBackPageQuery;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.po.EbUserFeedback;
import com.electricitybill.entity.vo.feedback.FeedBackPageVO;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.mapper.EbUserFeedbackMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbUserFeedbackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.BeanUtils;
import com.electricitybill.utils.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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

    @Override
    public PageDTO<FeedBackPageVO> queryFeedBackPage(FeedBackPageQuery feedBackPageQuery) {
        Page<EbUserFeedback> page = new Page<>(feedBackPageQuery.getPageNo(), feedBackPageQuery.getPageSize());
        Page<EbUserFeedback> ebUserFeedbackPage = lambdaQuery().eq(StringUtils.isNotBlank(feedBackPageQuery.getFeedbackType()), EbUserFeedback::getFeedbackType, feedBackPageQuery.getFeedbackType())
                .eq(StringUtils.isNotBlank(feedBackPageQuery.getFeedbackStatus()), EbUserFeedback::getStatus, feedBackPageQuery.getFeedbackStatus())
                .eq(StringUtils.isNotBlank(feedBackPageQuery.getFeedbackId()), EbUserFeedback::getId, feedBackPageQuery.getFeedbackId())
                .between(feedBackPageQuery.getStartDate() != null && feedBackPageQuery.getEndDate() != null, EbUserFeedback::getSubmitTime, feedBackPageQuery.getStartDate(), feedBackPageQuery.getEndDate())
                .page(page);
        if(ebUserFeedbackPage.getRecords() != null){
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
}
