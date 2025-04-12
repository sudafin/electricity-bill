package com.electricitybill.service.impl;

import com.electricitybill.entity.po.EbMeter;
import com.electricitybill.entity.vo.meter.MeterPageVO;
import com.electricitybill.mapper.EbMeterMapper;
import com.electricitybill.service.IEbMeterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.electricitybill.entity.dto.PageDTO;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-25
 */
@Service
public class EbMeterServiceImpl extends ServiceImpl<EbMeterMapper, EbMeter> implements IEbMeterService {

    @Override
    public PageDTO<MeterPageVO> queryMeterPage() {
        return null;
    }
}
