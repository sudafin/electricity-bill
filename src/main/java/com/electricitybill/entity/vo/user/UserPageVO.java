package com.electricitybill.entity.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserPageVO {
    /**
     * 用户id
     */
    private Long id;
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
     * 用电量
     */
    private BigDecimal electricityUsage;
    /**
     * 用户类型
     */
    private String userType;
    /**
     * 电费余额
     */
    private BigDecimal balance;
    /**
     * 上次缴费时间
     */
    private LocalDateTime lastPaymentDate;
}
