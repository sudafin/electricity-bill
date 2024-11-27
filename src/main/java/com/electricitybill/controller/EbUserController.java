package com.electricitybill.controller;


import com.electricitybill.service.IEbUserService;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/user")
public class EbUserController {
    @Resource
    private IEbUserService ebUserService;

}
