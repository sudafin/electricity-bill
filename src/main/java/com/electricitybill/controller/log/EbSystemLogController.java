package com.electricitybill.controller.log;


import com.electricitybill.service.IEbSystemLogService;
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
@RequestMapping("/log")
public class EbSystemLogController {
    @Resource
    private IEbSystemLogService ebSystemLogService;

}
