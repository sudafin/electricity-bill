package com.electricitybill.service;

import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.paymennt.PaymentPageQuery;
import com.electricitybill.entity.po.EbPayment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.payment.PaymentDetailVO;
import com.electricitybill.entity.vo.payment.PaymentPageVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbPaymentService extends IService<EbPayment> {

    PageDTO<PaymentPageVO> queryPage(PaymentPageQuery paymentPageQuery);

    PaymentDetailVO queryUserPayment(Long paymentId);

    R deletePayment(List<Long> ids);

    R refundPayment(Long paymentId);

    void export(HttpServletResponse response) throws IOException;
}
