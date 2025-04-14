package com.electricitybill.controller.admin;

import com.electricitybill.entity.vo.dashboard.DashboardVO;
import com.electricitybill.service.IEbAdminService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RequestMapping("/admin/dashboard")
@RestController
@Api(tags = "管理端仪表盘管理")
public class EbAdminDashboardController {
    @Resource
    private IEbAdminService ebAdminService;


    @GetMapping
    public DashboardVO getAdminDashboardInfo(){
        return ebAdminService.getAdminDashboardInfo();
    }
}
