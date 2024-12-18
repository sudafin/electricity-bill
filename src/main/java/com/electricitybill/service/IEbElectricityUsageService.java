package com.electricitybill.service;

import com.electricitybill.entity.dto.report.ReportDTO;
import com.electricitybill.entity.po.EbElectricityUsage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.report.ReportDataVO;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

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

    Future<String> export(ReportDTO reportDTO) throws IOException;
}
