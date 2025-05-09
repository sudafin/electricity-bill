package com.electricitybill.service;

import com.electricitybill.entity.R;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.rate.RateCrateDTO;
import com.electricitybill.entity.dto.rate.RatePageQuery;
import com.electricitybill.entity.po.EbRate;
import com.electricitybill.entity.vo.rate.RateDetailVO;
import com.electricitybill.entity.vo.rate.RatePageVO;

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

    RateDetailVO getRateDetail(Long id);


    PageDTO<RatePageVO> queryRatePage(RatePageQuery pageDTO);

    R createRate(RateCrateDTO rateCrateDTO);

    R deleteRate(List<Long> ids);

    R editRate(Long id, RateCrateDTO rateCrateDTO);
}
