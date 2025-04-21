package com.electricitybill.controller.user;


import com.alipay.api.domain.UserDTO;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.user.UserCreateDTO;
import com.electricitybill.entity.dto.user.UserEditDTO;
import com.electricitybill.entity.vo.user.UserInfoVO;
import com.electricitybill.service.IEbUserService;
import io.swagger.annotations.Api;
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
@RequestMapping("/user")
@Api(tags = "用户端用户管理")
public class EbUserController {
    @Resource
    private IEbUserService ebUserService;

    //用户端修改信息
    @PostMapping("edit")
    public R userEditInfo(@RequestBody UserEditDTO userEditDTO) {
        return ebUserService.userEditInfo(userEditDTO);
    }

    @GetMapping("detail")
    public UserInfoVO getUserInfo() {
        return ebUserService.getUserInfo();
    }

    /**
     * 修改密码
     */


}
