package com.electricitybill.controller;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.user.UserDTO;
import com.electricitybill.entity.dto.user.UserPageQuery;
import com.electricitybill.entity.vo.user.UserDetailVO;
import com.electricitybill.entity.vo.user.UserPageVO;
import com.electricitybill.entity.vo.user.UserBillVO;
import com.electricitybill.service.IEbUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
@RequestMapping("/user")
@Api(tags = "用户管理")
public class EbUserController {
    @Resource
    private IEbUserService ebUserService;

    @ApiOperation("管理端分页查询用户")
    @GetMapping("admin/page")
    public PageDTO<UserPageVO> queryUserPage( UserPageQuery userPageQuery){
        return ebUserService.queryUserPage(userPageQuery);
    }

    @ApiOperation("管理端查询用户详情")
    @GetMapping("admin/detail/{userId}")
    public UserDetailVO queryUserDetail(@PathVariable @NotNull Long userId){
        return ebUserService.queryUserDetail(userId);
    }

    @ApiOperation("管理端创建用户")
    @PostMapping("admin/create")
    public R insertUser(@RequestBody @NotNull UserDTO userDTO){
        return ebUserService.insertUser(userDTO);
    }
    @ApiOperation("管理端删除用户")
    @DeleteMapping("admin/delete")
    public R deleteUser(@RequestParam("userIds")  List<Long> userIds){
        return ebUserService.deleteUser(userIds);
    }
    @ApiOperation("管理端更新用户信息")
    @PutMapping("admin/edit")
    public R updateUser(@RequestBody @NotNull UserDTO userDTO){
        return ebUserService.updateUser(userDTO);
    }


}
