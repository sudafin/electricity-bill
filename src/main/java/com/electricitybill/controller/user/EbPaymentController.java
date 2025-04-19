package com.electricitybill.controller.user;


import com.electricitybill.annotation.ExportExcel;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.paymennt.PaymentPageQuery;
import com.electricitybill.entity.vo.payment.PaymentDetailVO;
import com.electricitybill.entity.vo.payment.PaymentPageVO;
import com.electricitybill.entity.vo.payment.PaymentUserVO;
import com.electricitybill.service.IEbPaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
public class EbPaymentController {
    @Resource
    private IEbPaymentService ebPaymentService;

    @GetMapping()
    private PaymentUserVO getPaymentRecords() {
        return ebPaymentService.getPaymentRecords();
    }

}
