package com.electricitybill.entity.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("用户个人信息VO")
public class UserProfileVO {


    
    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("用户类型")
    private String userType;

    @ApiModelProperty("电话号码")
    private String phone;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("注册时间")
    private LocalDateTime registerTime;
    //账单提醒
    private Boolean billReminder;
    //缴费提醒
    private Boolean paymentReminder;
}