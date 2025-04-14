package com.electricitybill.controller.admin;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.meter.MeterCreateDTO;
import com.electricitybill.entity.dto.meter.MeterEditDTO;
import com.electricitybill.entity.dto.meter.MeterInspectionDTO;
import com.electricitybill.entity.dto.meter.MeterPageQuery;
import com.electricitybill.entity.vo.meter.MeterDetailVO;
import com.electricitybill.entity.vo.meter.MeterPageVO;
import com.electricitybill.service.IEbMeterService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import com.electricitybill.entity.dto.PageDTO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author huangdada
 * @since 2025-04-10
 */
@RestController
@RequestMapping("/admin/meter")
@Api(tags = "电表管理")
public class EbMeterController {
    @Resource
    private IEbMeterService ebMeterService;


    @GetMapping("/page")
    public PageDTO<MeterPageVO> queryMeterPage(MeterPageQuery meterPageQuery) {
        return ebMeterService.queryMeterPage(meterPageQuery);
    }

    @GetMapping("/model")
    public List<String> getMeterModel() {
        return ebMeterService.getMeterModel();
    }

    @PostMapping("/create")
    public R<Object> createMeter(MeterCreateDTO meterCreateDTO) {
        return ebMeterService.createMeter(meterCreateDTO);
    }

    //绑定用户
    @PostMapping("/edit")
    public R<Object> editMeter(@Valid MeterEditDTO meterBindDTO) {
        return ebMeterService.editMeter(meterBindDTO);
    }

    @PutMapping("delete/{meterId}")
    public R<Object> deleteMeter(@PathVariable @NotNull String meterId) {
        return ebMeterService.deleteMeter(meterId);
    }


    @GetMapping("/{meterId}")
    public MeterDetailVO getMeterDetail(@PathVariable @NotNull String meterId) {
        return ebMeterService.getMeterDetail(meterId);
    }

    @PostMapping("/inspection/{meterId}")
    public R<Object> inspectionCreate(@Valid @NotNull MeterInspectionDTO meterInspectionDTO) {
        return ebMeterService.inspectionCreate(meterInspectionDTO);
    }

    @GetMapping("/inspection/type")
    public Map<String,List<String>> getInspectionType() {
        return ebMeterService.getInspectionType();
    }
}

