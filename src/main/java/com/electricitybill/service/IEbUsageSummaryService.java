package com.electricitybill.service;

import com.electricitybill.entity.po.EbUsageSummary;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.electricity.ElectricityUserVO;

/**
 * <p>
 * 每日用电量及电费汇总表 服务类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-27
 */
public interface IEbUsageSummaryService extends IService<EbUsageSummary> {

    ElectricityUserVO getElectricityRecords();
}
