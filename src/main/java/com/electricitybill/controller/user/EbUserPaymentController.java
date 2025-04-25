package com.electricitybill.controller.user;


import com.electricitybill.entity.vo.payment.PaymentUserVO;
import com.electricitybill.service.IEbPaymentService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@RestController
@RequestMapping("/user/payment")
@Slf4j
@Api(tags = "用户端缴费管理")
public class EbUserPaymentController {
    @Resource
    private IEbPaymentService ebPaymentService;

    @GetMapping()
    private PaymentUserVO getPaymentRecords() {
        return ebPaymentService.getPaymentRecords();
    }

}
