package com.electricitybill.service;

import com.electricitybill.entity.R;
import com.electricitybill.entity.po.EbRate;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.rate.RateInfoVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbRateService extends IService<EbRate> {

    List<RateInfoVO> getRate();

    R editRate(Long id, BigDecimal rateValue);
}
