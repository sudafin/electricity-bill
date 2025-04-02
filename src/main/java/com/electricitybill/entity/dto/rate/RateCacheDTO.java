package com.electricitybill.entity.dto.rate;

import cn.hutool.json.JSONUtil;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.po.EbRate;
import com.electricitybill.enums.UserType;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.service.IEbRateService;
import com.electricitybill.utils.StringUtils;
import com.electricitybill.utils.TTLGenerator;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author huangdada
 * @version 1.0
 * @description TODO
 * 2025/03/27/11:01
 */
@Data
public class RateCacheDTO {
    @Resource
    private IEbRateService ebRateService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public double basePrice; //基础价格率
    public double peakMultiplier;    // 峰段电价倍率
    public double valleyMultiplier;  // 谷段电价倍率
    public double summerPeak; // 夏季尖峰电价
    public double discountRate; //折扣率

    public RateCacheDTO RateCacheDTOUserType(UserType userType) {
        String rateInfoJson = stringRedisTemplate.opsForValue().get(Constant.RATE_USER_TYPE + userType.getDesc());
        if(StringUtils.isNotBlank(rateInfoJson)){
            return JSONUtil.toBean(rateInfoJson,RateCacheDTO.class);
        }
        EbRate ebRate = ebRateService.getById(userType.getValue());
        if(ebRate == null){
            throw new BadRequestException(Constant.RATE_USER_TYPE_NOT_EXIST);
        }
        peakMultiplier =  ebRate.getPeakPrice() == null ? 0 : ebRate.getPeakPrice().doubleValue();
        valleyMultiplier = ebRate.getValleyPrice() == null ? 0 : ebRate.getValleyPrice().doubleValue();
        summerPeak = ebRate.getSummerPeakPrice() == null ? 0 : ebRate.getSummerPeakPrice().doubleValue();
        discountRate = ebRate.getDiscount() == null ? 0 : ebRate.getDiscount().doubleValue();
        stringRedisTemplate.opsForValue().set(Constant.RATE_USER_TYPE +userType.getDesc(), JSONUtil.toJsonStr(RateCacheDTO.this),TTLGenerator.generateDays(30,180));
        return RateCacheDTO.this;
    }

}
