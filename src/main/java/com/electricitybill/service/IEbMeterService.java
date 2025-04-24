package com.electricitybill.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.meter.MeterCreateDTO;
import com.electricitybill.entity.dto.meter.MeterEditDTO;
import com.electricitybill.entity.dto.meter.MeterInspectionDTO;
import com.electricitybill.entity.dto.meter.MeterPageQuery;
import com.electricitybill.entity.po.EbMeter;
import com.electricitybill.entity.vo.meter.MeterDetailVO;
import com.electricitybill.entity.vo.meter.MeterInspectionVO;
import com.electricitybill.entity.vo.meter.MeterPageVO;
import com.electricitybill.entity.dto.PageDTO;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-25
 */
public interface IEbMeterService extends IService<EbMeter> {

    PageDTO<MeterPageVO> queryMeterPage(MeterPageQuery meterPageQuery);

    R<Object> createMeter(MeterCreateDTO meterCreateDTO);

    List<String> getMeterModel();

    R<Object> editMeter(MeterEditDTO meterBindDTO);

    R<Object> deleteMeter(@NotNull String id);

    MeterDetailVO getMeterDetail(@NotNull String meterId);

    R<Object> inspectionCreate(MeterInspectionDTO meterInspectionDTO);

    Map<String, List<String>> getInspectionType();

    List<MeterInspectionVO> getInspectionById(String meterId);


}
