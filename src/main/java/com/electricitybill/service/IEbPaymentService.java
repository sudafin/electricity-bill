package com.electricitybill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.po.EbPayment;
import com.electricitybill.entity.vo.payment.PaymentUserVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbPaymentService extends IService<EbPayment> {

    PaymentUserVO getPaymentRecords();
}
