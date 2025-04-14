package com.electricitybill.service;

import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.feedback.FeedBackPageQuery;
import com.electricitybill.entity.po.EbUserFeedback;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.feedback.FeedBackPageVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-17
 */
public interface IEbUserFeedbackService extends IService<EbUserFeedback> {

    PageDTO<FeedBackPageVO> queryFeedBackPage(FeedBackPageQuery feedBackPageQuery);
}
