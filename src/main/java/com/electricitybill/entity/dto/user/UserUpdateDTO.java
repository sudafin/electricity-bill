package com.electricitybill.entity.dto.user;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserUpdateDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;
    /**
     * 地址
     */

    private String address;



}
