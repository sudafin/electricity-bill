package com.electricitybill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.po.EbElectricityUsage;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbElectricityUsageService extends IService<EbElectricityUsage> {

    void calculateElectricityUsageSummaryDay();

    void calculateMonthlyBill();
}
