package com.electricitybill.controller;

import com.electricitybill.entity.vo.dashboard.DashboardVO;
import com.electricitybill.service.IEbAdminService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RequestMapping("/dashboard")
@RestController
@Api(tags = "仪表盘管理")
public class EbDashboardController {
    @Resource
    private IEbAdminService ebAdminService;


    @GetMapping("admin")
    public DashboardVO getAdminDashboardInfo(){
        return ebAdminService.getAdminDashboardInfo();
    }
}
