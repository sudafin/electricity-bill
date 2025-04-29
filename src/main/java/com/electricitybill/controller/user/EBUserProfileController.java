package com.electricitybill.controller.user;

import cn.hutool.json.JSONObject;
import com.electricitybill.entity.R;
import com.electricitybill.entity.vo.user.UserProfileVO;
import com.electricitybill.service.UserProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/user/profile")
@Api(tags = "用户端 - 个人信息接口")
public class EBUserProfileController {

    @Resource
    private UserProfileService userProfileService;

    @GetMapping("")
    @ApiOperation("获取用户个人信息")
    public R<UserProfileVO> getUserProfile() {
        log.info("获取用户个人信息");
        UserProfileVO profile = userProfileService.getUserProfile();
        return R.ok(profile);
    }

    @PutMapping("/update")
    @ApiOperation("更新用户个人信息")
    public R<Boolean> updateUserProfile(@RequestBody JSONObject jsonObject) {
        log.info("更新用户个人信息: {}", jsonObject);
        boolean result = userProfileService.updateUserProfile(jsonObject);
        return R.ok(result);
    }

    @PutMapping("/change-password")
    @ApiOperation("修改用户密码")
    public R<Boolean> changePassword(@Validated @RequestBody JSONObject jsonObject) {
        log.info("修改用户密码请求");
        String currentPassword = jsonObject.getStr("currentPassword");
        String newPassword = jsonObject.getStr("newPassword");
        String confirmPassword = jsonObject.getStr("confirmPassword");
        
        boolean result = userProfileService.changePassword(currentPassword, newPassword, confirmPassword);
        return R.ok(result);
    }
}