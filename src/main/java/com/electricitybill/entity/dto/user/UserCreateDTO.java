package com.electricitybill.entity.dto.user;

import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class UserCreateDTO {
    /**
     * 身份证号
     */
    @NotNull(groups = {UserCreateDTOGroup.class})
    public String idCardNo;
    /**
     * 用户名
     */
    @NotNull(groups = {UserCreateDTOGroup.class})
    private String username;

    /**
     * 手机号
     */
    @NotNull(groups = {UserCreateDTOGroup.class})
    private String phone;
    /**
     * 地址
     */
    @NotNull(groups = {UserCreateDTOGroup.class})
    private String address;
    /**
     * 电表编号
     */
    private String meterNo;

    /**
     * 用户类型
     */
    @NotNull(groups = {UserCreateDTOGroup.class})
    private String userType;



}
