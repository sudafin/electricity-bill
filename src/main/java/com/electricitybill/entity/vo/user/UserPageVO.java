package com.electricitybill.entity.vo.user;

import io.swagger.annotations.ApiModelProperty;
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
    private String meterId;
    /**
     * 用户状态
     */
    private String accountStatus;
    /**
     * 用户类型
     */
    private String userType;
    /**
     * 上次缴费时间
     */
    private LocalDateTime lastPaymentDate;
    /**
     * 身份证号
     */
    private String idCardNo;


}
