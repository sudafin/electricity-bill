package com.electricitybill.controller.admin;

import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.feedback.FeedBackPageQuery;
import com.electricitybill.entity.vo.feedback.FeedBackPageVO;
import com.electricitybill.service.IEbUserFeedbackService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RequestMapping("/admin/feedback")
@RestController
@Api(tags = "管理端用户反馈")
public class EbAdminFeedBackController {
    @Resource
    private IEbUserFeedbackService ebUserFeedbackService;

    @GetMapping("page")
    public PageDTO<FeedBackPageVO> queryFeedBackPage(FeedBackPageQuery feedBackPageQuery) {
        return ebUserFeedbackService.queryFeedBackPage(feedBackPageQuery);
    }
}
