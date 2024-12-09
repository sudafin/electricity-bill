package com.electricitybill.service;

import com.electricitybill.entity.dto.report.ReportDTO;
import com.electricitybill.entity.po.EbElectricityUsage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.report.ReportDataVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbElectricityUsageService extends IService<EbElectricityUsage> {

    List<ReportDataVO> getReportData(ReportDTO reportDTO);
}
