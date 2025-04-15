package com.electricitybill.controller.admin;

import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.feedback.FeedBackPageQuery;
import com.electricitybill.entity.dto.feedback.FeedBackProcessDTO;
import com.electricitybill.entity.vo.feedback.FeedBackDetailVO;
import com.electricitybill.entity.vo.feedback.FeedBackPageVO;
import com.electricitybill.service.IEbUserFeedbackService;
import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;


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

    @GetMapping("detail/{feedbackId}")
    public FeedBackDetailVO getFeedBackDetail(@PathVariable Long feedbackId) {
        return ebUserFeedbackService.getFeedBackDetail(feedbackId);
    }

    @PostMapping("process/{feedbackId}")
    public R<Object> processFeedBack(@RequestBody @Validated FeedBackProcessDTO feedBackProcessDTO){
        return ebUserFeedbackService.processFeedBack(feedBackProcessDTO);
    }
}
