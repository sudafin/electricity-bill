package com.electricitybill.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.rate.RateCrateDTO;
import com.electricitybill.entity.dto.rate.RatePageQuery;
import com.electricitybill.entity.po.EbRate;
import com.electricitybill.entity.vo.rate.RateDetailVO;
import com.electricitybill.entity.vo.rate.RatePageVO;
import com.electricitybill.enums.PeriodType;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.EbRateMapper;
import com.electricitybill.service.IEbRateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
public class EbRateServiceImpl extends ServiceImpl<EbRateMapper, EbRate> implements IEbRateService {
    @Override
    public RateDetailVO getRateDetail(Long id) {
        EbRate ebRate = getById(id);
        if(ObjectUtils.isEmpty(ebRate)){
            throw new BadRequestException(Constant.RATE_NOT_EXIST);
        }
        return BeanUtils.copyBean(ebRate, RateDetailVO.class);
    }


    @Override
    public R editRate(Long id, RateCrateDTO rateCrateDTO) {
        EbRate ebRate = getById(id);
        if (ObjectUtils.isEmpty(ebRate)) {
            throw new BadRequestException(Constant.RATE_NOT_EXIST);
        }
        BeanUtils.copyProperties(rateCrateDTO, ebRate);
        updateById(ebRate);
        return R.ok();
    }

    @Override
    public PageDTO<RatePageVO> queryRatePage(RatePageQuery ratePageQuery) {
        Page<EbRate> page = new Page<>(ratePageQuery.getPageNo(), ratePageQuery.getPageSize());
        Page<EbRate> ebRatePage = lambdaQuery().eq(StringUtils.isNotBlank(ratePageQuery.getUserType()), EbRate::getUserType, ratePageQuery.getUserType())
                .eq(StringUtils.isNotBlank(ratePageQuery.getStatus()), EbRate::getStatus, ratePageQuery.getStatus())
                .eq(StringUtils.isNotBlank(ratePageQuery.getRateId()), EbRate::getId, ratePageQuery.getRateId())
                .between(ratePageQuery.getStartDate() != null && ratePageQuery.getEndDate() != null, EbRate::getEffectiveDate, ratePageQuery.getStartDate(), ratePageQuery.getEndDate())
                .orderByDesc(EbRate::getEffectiveDate)
                .page(page);
        if (ebRatePage.getTotal() == 0) {
            return PageDTO.empty(page);
        }
        List<EbRate> records = ebRatePage.getRecords();
        List<RatePageVO> ratePageVOS = records.stream().map(ebRate -> BeanUtils.copyBean(ebRate, RatePageVO.class)).collect(Collectors.toList());
        return PageDTO.of(page, ratePageVOS);
    }

    @Override
    public R createRate(RateCrateDTO rateCrateDTO) {
        String userType = rateCrateDTO.getUserType();
        if (StringUtils.isBlank(userType)) {
            throw new BadRequestException(Constant.INVALID_USER_TYPE);
        }
//        lambdaQuery().eq(EbRate::getUserType, userType).oneOpt().ifPresent(ebRate -> {
//            throw new BadRequestException(Constant.RATE_USER_TYPE_EXIST);
//        });
        EbRate ebRate = BeanUtils.copyBean(rateCrateDTO, EbRate.class);
        save(ebRate);
        return R.ok();
    }

    @Override
    @Transactional
    public R deleteRate(List<Long> ids) {
        int deleteBatchIds = baseMapper.deleteBatchIds(ids);
        if (deleteBatchIds != ids.size()) {
            throw new DbException(Constant.DB_DELETE_FAILURE);
        }
        return R.ok();
    }
}
