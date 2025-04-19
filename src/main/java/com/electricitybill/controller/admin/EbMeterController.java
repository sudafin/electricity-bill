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

    /**
     * 分页查询电表信息
     *
     * @param meterPageQuery 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public PageDTO<MeterPageVO> queryMeterPage(MeterPageQuery meterPageQuery) {
        return ebMeterService.queryMeterPage(meterPageQuery);
    }

    /**
     * 获取所有电表型号
     *
     * @return 电表型号列表
     */
    @GetMapping("/model")
    public List<String> getMeterModel() {
        return ebMeterService.getMeterModel();
    }

    /**
     * 创建新的电表
     *
     * @param meterCreateDTO 新电表信息
     * @return 操作结果
     */
    @PostMapping("/create")
    public R<Object> createMeter(@RequestBody MeterCreateDTO meterCreateDTO) {
        return ebMeterService.createMeter(meterCreateDTO);
    }

    /**
     * 编辑电表信息
     *
     * @param meterBindDTO 电表编辑信息
     * @return 操作结果
     */
    @PostMapping("/edit")
    public R<Object> editMeter(@RequestBody @Valid MeterEditDTO meterBindDTO) {
        return ebMeterService.editMeter(meterBindDTO);
    }

    /**
     * 删除指定电表
     *
     * @param meterId 电表ID
     * @return 操作结果
     */
    @PutMapping("delete/{meterId}")
    public R<Object> deleteMeter(@PathVariable @NotNull String meterId) {
        return ebMeterService.deleteMeter(meterId);
    }

    /**
     * 获取指定电表的详细信息
     *
     * @param meterId 电表ID
     * @return 电表详细信息
     */
    @GetMapping("/{meterId}")
    public MeterDetailVO getMeterDetail(@PathVariable @NotNull String meterId) {
        return ebMeterService.getMeterDetail(meterId);
    }

    /**
     * 创建电表巡检记录
     *
     * @param meterInspectionDTO 巡检信息
     * @return 操作结果
     */
    @PostMapping("/inspection")
    public R<Object> inspectionCreate(@RequestBody MeterInspectionDTO meterInspectionDTO) {
        return ebMeterService.inspectionCreate(meterInspectionDTO);
    }
}