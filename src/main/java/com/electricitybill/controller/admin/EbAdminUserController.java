package com.electricitybill.controller.admin;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.user.UserDTO;
import com.electricitybill.entity.dto.user.UserPageQuery;
import com.electricitybill.entity.vo.user.UserDetailVO;
import com.electricitybill.entity.vo.user.UserPageVO;
import com.electricitybill.service.IEbUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@RestController
@RequestMapping("/admin/user")
@Api(tags = "管理端用户管理")
public class EbAdminUserController {
    private IEbUserService ebUserService;

    @ApiOperation("管理端分页查询用户")
    @GetMapping("page")
    public PageDTO<UserPageVO> queryUserPage( UserPageQuery userPageQuery){
        return ebUserService.queryUserPage(userPageQuery);
    }

    @ApiOperation("管理端查询用户详情")
    @GetMapping("detail/{userId}")
    public UserDetailVO queryUserDetail(@PathVariable @NotNull Long userId){
        return ebUserService.queryUserDetail(userId);
    }

    @ApiOperation("管理端创建用户")
    @PostMapping("create")
    public R insertUser(@RequestBody @NotNull UserDTO userDTO){
        return ebUserService.insertUser(userDTO);
    }
    @ApiOperation("管理端删除用户")
    @DeleteMapping("delete")
    public R deleteUser(@RequestParam("userIds")  List<Long> userIds){
        return ebUserService.deleteUser(userIds);
    }
    @ApiOperation("管理端更新用户信息")
    @PutMapping("edit")
    public R updateUser(@RequestBody @NotNull UserDTO userDTO){
        return ebUserService.updateUser(userDTO);

    }


}
