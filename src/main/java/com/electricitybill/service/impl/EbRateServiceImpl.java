package com.electricitybill.service.impl;

import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.po.EbRate;
import com.electricitybill.entity.vo.rate.RateInfoVO;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.mapper.EbRateMapper;
import com.electricitybill.service.IEbRateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
public class EbRateServiceImpl extends ServiceImpl<EbRateMapper, EbRate> implements IEbRateService {

    @Override
    public List<RateInfoVO> getRate() {
        List<EbRate> list = list();
        if(CollUtils.isEmpty(list))
            return CollUtils.emptyList();
        return list.stream().map(ebRate -> {
            RateInfoVO rateInfoVO = new RateInfoVO();
            rateInfoVO.setRateId(ebRate.getId());
            rateInfoVO.setRateName(ebRate.getRateName());
            rateInfoVO.setRateValue(ebRate.getPrice());
            return rateInfoVO;
        }).collect(Collectors.toList());
    }

    @Override
    public R editRate(Long id, BigDecimal rateValue) {
        EbRate ebRate = getById(id);
        if(ObjectUtils.isEmpty(ebRate)){
            throw new BadRequestException(Constant.RATE_NOT_EXIST);
        }
        ebRate.setPrice(rateValue);
        return R.ok();
    }
}
