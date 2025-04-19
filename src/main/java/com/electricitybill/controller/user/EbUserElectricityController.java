package com.electricitybill.controller.user;

import com.electricitybill.entity.vo.electricity.ElectricityUserVO;
import com.electricitybill.service.IEbElectricityUsageService;
import com.electricitybill.service.IEbUsageSummaryService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RequestMapping("/user/electricity")
@RestController
@Api(tags = "用户端电费管理")
public class EbUserElectricityController {
    @Resource
    private IEbUsageSummaryService ebUsageSummaryService;

    //首页去获取当前数据
    @GetMapping
    public ElectricityUserVO getElectricityRecords() {
        return ebUsageSummaryService.getElectricityRecords();
    }

}
