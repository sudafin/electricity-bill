package com.electricitybill.service.impl;

import com.electricitybill.entity.po.EbUsageSummary;
import com.electricitybill.mapper.EbUsageSummaryMapper;
import com.electricitybill.service.IEbUsageSummaryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 每日用电量及电费汇总表 服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-27
 */
@Service
public class EbUsageSummaryServiceImpl extends ServiceImpl<EbUsageSummaryMapper, EbUsageSummary> implements IEbUsageSummaryService {
}
