package com.electricitybill.controller.admin;


import cn.hutool.json.JSONObject;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.user.UserCreateDTOGroup;
import com.electricitybill.entity.dto.user.UserCreateDTO;
import com.electricitybill.entity.dto.user.UserPageQuery;
import com.electricitybill.entity.dto.usertype.UserTypeCreateDTO;
import com.electricitybill.entity.vo.user.UserDetailVO;
import com.electricitybill.entity.vo.user.UserPageVO;
import com.electricitybill.service.IEbUserService;
import com.google.gson.JsonObject;
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
    @PutMapping("edit")
    //不需要指定验证分组，字段的验证规则失效
    public R updateUser(@RequestBody @Validated UserCreateDTO userCreateDTO){
        return ebUserService.adminUpdateUser(userCreateDTO);

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

    /**
     *
     * @param idCardNo 身份证
     * @return id
     *         username
     *         phone
     */
    @ApiOperation("管理端查询个人用户")
    @GetMapping("getUserInfoByIdCard/{id}")
    public JSONObject getUserByCarId(@PathVariable("id") String idCardNo){
        return ebUserService.getUserInfoByIdCard(idCardNo);
    }

    /**
     *
     * @param jsonObject meterId 电表id usrerId 用户id status 状态 1表示绑定 2表示解绑
     * @return R
     */

    @ApiOperation("管理端绑定电表")
    @PostMapping("bindMeter")
    public R bindMeter(@RequestBody JSONObject jsonObject){
        return ebUserService.bindMeter(jsonObject);
    }
}
