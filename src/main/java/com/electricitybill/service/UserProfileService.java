package com.electricitybill.service;


import cn.hutool.json.JSONObject;
import com.electricitybill.entity.vo.user.UserProfileVO;

public interface UserProfileService {
    
    /**
     * 获取用户个人信息
     * @return 用户个人信息VO
     */
    UserProfileVO getUserProfile();
    
    /**
     * 更新用户个人信息
     *
     * @return 更新是否成功
     */
    boolean updateUserProfile(JSONObject jsonObject);
    
    /**
     * 修改用户密码
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @return 修改是否成功
     */
    boolean changePassword(String currentPassword, String newPassword, String confirmPassword);
}