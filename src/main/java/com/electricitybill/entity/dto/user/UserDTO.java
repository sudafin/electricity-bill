package com.electricitybill.entity.dto.user;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class UserDTO {
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
    /**
     * 电表编号
     */
    private String meterNo;
    /**
     * 用户状态
     */
    private String accountStatus;

    /**
     * 用户类型
     */
    private String userType;
    /**
     * 电费余额
     */
    private BigDecimal balance;

}
