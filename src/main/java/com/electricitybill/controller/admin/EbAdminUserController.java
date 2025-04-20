package com.electricitybill.controller.admin;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.user.UserCreateDTOGroup;
import com.electricitybill.entity.dto.user.UserCreateDTO;
import com.electricitybill.entity.dto.user.UserPageQuery;
import com.electricitybill.entity.dto.usertype.UserTypeCreateDTO;
import com.electricitybill.entity.vo.user.UserDetailVO;
import com.electricitybill.entity.vo.user.UserPageVO;
import com.electricitybill.service.IEbUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/admin/user")
@Api(tags = "管理端用户管理")
public class EbAdminUserController {
    @Resource
    private IEbUserService ebUserService;

    @ApiOperation("管理端分页查询用户")
    @GetMapping("page")
    public PageDTO<UserPageVO> queryUserPage(@Validated UserPageQuery userPageQuery){
        return ebUserService.queryUserPage(userPageQuery);
    }

    @ApiOperation("管理端查询用户详情")
    @GetMapping("detail/{userId}")
    public UserDetailVO queryUserDetail(@PathVariable @Validated @NotNull Long userId){
        return ebUserService.queryUserDetail(userId);
    }

    @ApiOperation("管理端创建用户")
    @PostMapping("create")
    // @Validated(value = UserCreateDTOGroup.class)用来指定哪个标识，注意这个接口是无意义的只是标记用，然后把DTO中需要做鉴别的字段做这个标记分组就行
    public R insertUser(@RequestBody @Validated(value = UserCreateDTOGroup.class) UserCreateDTO userCreateDTO){
        return ebUserService.insertUser(userCreateDTO);
    }
    @ApiOperation("管理端删除用户")
    @DeleteMapping("delete")
    public R deleteUser(@RequestParam("userIds")  List<Long> userIds){
        return ebUserService.deleteUser(userIds);
    }

    @ApiOperation("管理端更新用户信息")
    @PutMapping("edit/{userId}")
    //不需要指定验证分组，字段的验证规则失效
    public R updateUser(@PathVariable Long userId,@RequestBody @Validated UserCreateDTO userCreateDTO){
        return ebUserService.updateUser(userCreateDTO);

    }

    @ApiOperation("管理端获取用户类型")
    @GetMapping("userType")
    public List<String> getUserTypeList(){
        return ebUserService.getUserTypeList();
    }

    @ApiOperation("管理端新增用户类型")
    @PostMapping("addUserType")
    public R addUserType(@RequestBody @Validated UserTypeCreateDTO typeCreateDTO){
        return ebUserService.addUserType(typeCreateDTO);
    }

}
