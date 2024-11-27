package com.electricitybill.controller;


import com.electricitybill.entity.dto.AdminFormDTO;
import com.electricitybill.entity.vo.LoginVO;
import com.electricitybill.service.IEbAdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@RestController
@RequestMapping("/admin")
public class EbAdminController {
    @Resource
    private IEbAdminService ebAdminService;
    @PostMapping("/login")
    public LoginVO login(@RequestBody @Validated AdminFormDTO adminFormDTO){
        return ebAdminService.login(adminFormDTO);
    }
}
