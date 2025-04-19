package com.electricitybill.controller.user;

import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.feedback.FeedBackSubmitDTO;
import com.electricitybill.service.IEbUserFeedbackService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user/feedback")
@Api(tags = "用户端用户反馈")
@Slf4j
public class EbUserFeedBackController {
    @Resource
    private IEbUserFeedbackService ebUserFeedbackService;

    @PostMapping("submit")
    public R submitFeedBack(FeedBackSubmitDTO feedBackSubmitDTO) {
        return ebUserFeedbackService.submitFeedBack(feedBackSubmitDTO);
    }

}
