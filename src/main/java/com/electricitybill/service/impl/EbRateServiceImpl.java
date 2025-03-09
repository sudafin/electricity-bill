package com.electricitybill.service.impl;

import cn.hutool.json.JSONUtil;
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
import com.electricitybill.utils.StringUtils;
import com.electricitybill.utils.TTLGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<RateInfoVO> getRate() {
        String rateInfoJson = stringRedisTemplate.opsForValue().get(Constant.RATE_LIST_KEY);
        if(StringUtils.isNotBlank(rateInfoJson)){
            return JSONUtil.toList(rateInfoJson, RateInfoVO.class);
        }
        List<EbRate> list = list();
        if(CollUtils.isEmpty(list))
            return CollUtils.emptyList();

        List<RateInfoVO> rateInfoVOList = list.stream().map(ebRate -> {
            RateInfoVO rateInfoVO = new RateInfoVO();
            rateInfoVO.setRateId(ebRate.getId());
            rateInfoVO.setRateName(ebRate.getRateName());
            rateInfoVO.setRateValue(ebRate.getPrice());
            return rateInfoVO;
        }).collect(Collectors.toList());
        //缓存到redis
        stringRedisTemplate.opsForValue().set(Constant.RATE_LIST_KEY, JSONUtil.toJsonStr(rateInfoVOList),TTLGenerator.generateDefaultRandomTTL(), TimeUnit.SECONDS);
        return rateInfoVOList;
    }

    @Override
    public R editRate(Long id, BigDecimal rateValue) {
        EbRate ebRate = getById(id);
        if(ObjectUtils.isEmpty(ebRate)){
            throw new BadRequestException(Constant.RATE_NOT_EXIST);
        }
        ebRate.setPrice(rateValue);
        updateById(ebRate);
        //重新设置缓存
        stringRedisTemplate.opsForValue().set(Constant.RATE_LIST_KEY, "",TTLGenerator.generateDefaultRandomTTL(), TimeUnit.SECONDS);
        return R.ok();
    }
}
