package com.electricitybill.controller.dashboard;

import com.electricitybill.entity.vo.dashboard.DashboardVO;
import com.electricitybill.service.IEbUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RequestMapping("/dashboard")
@RestController
public class DashboardController {
    @Resource
    private IEbUserService eBUserService;

    @GetMapping
    public DashboardVO getDashboardInfo(){
        return eBUserService.getDashboardInfo();
    }
}
