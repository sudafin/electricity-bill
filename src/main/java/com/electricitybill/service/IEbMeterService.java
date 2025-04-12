package com.electricitybill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.po.EbMeter;
import com.electricitybill.entity.vo.meter.MeterPageVO;
import com.electricitybill.entity.dto.PageDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-25
 */
public interface IEbMeterService extends IService<EbMeter> {

    PageDTO<MeterPageVO> queryMeterPage();
}
