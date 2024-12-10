package com.electricitybill.controller.payment;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.paymennt.PaymentPageQuery;
import com.electricitybill.entity.vo.payment.PaymentDetailVO;
import com.electricitybill.entity.vo.payment.PaymentPageVO;
import com.electricitybill.service.IEbPaymentService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@RestController
@RequestMapping("/payment")
public class EbPaymentController {
    @Resource
    private IEbPaymentService ebPaymentService;

    @GetMapping("page")
    public PageDTO<PaymentPageVO> queryPage(PaymentPageQuery paymentPageQuery){
        return ebPaymentService.queryPage(paymentPageQuery);
    }
    @GetMapping("detail/{id}")
    public PaymentDetailVO queryPaymentDetail(@PathVariable(name = "id") Long paymentId){
        return ebPaymentService.queryUserPayment(paymentId);
    }
    @DeleteMapping("delete")
    public R deletePayment(@RequestParam(name = "ids") List<Long> ids){
        return ebPaymentService.deleteUser(ids);
    }
    @PutMapping("refund/{id}")
    public R refundPayment(@PathVariable(name = "id") Long paymentId){
        return ebPaymentService.refundPayment(paymentId);
    }
    @GetMapping("/export")
    @ApiOperation("导出运营数据报表")
    public void export(HttpServletResponse response) throws IOException {
        ebPaymentService.export(response);
    }
}
