package com.electricitybill.controller.user;


import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.user.UserPageQuery;
import com.electricitybill.entity.vo.user.UserPageVO;
import com.electricitybill.service.IEbUserService;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/page")
    public PageDTO<UserPageVO> queryUserPage(UserPageQuery userPageQuery){
        return ebUserService.queryUserPage(userPageQuery);
    }
}
