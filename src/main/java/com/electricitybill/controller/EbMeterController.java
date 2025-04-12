package com.electricitybill.controller;


import com.electricitybill.entity.vo.meter.MeterPageVO;
import com.electricitybill.service.IEbMeterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.electricitybill.entity.dto.PageDTO;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author huangdada
 * @since 2025-04-10
 */
@RestController
@RequestMapping("/meter")
public class EbMeterController {
    @Resource
    private IEbMeterService ebMeterService;


    @GetMapping("/page")
    public PageDTO<MeterPageVO> queryMeterPage() {
        return ebMeterService.queryMeterPage();
    }
}
