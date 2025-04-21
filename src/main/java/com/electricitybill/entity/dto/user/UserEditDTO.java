package com.electricitybill.entity.dto.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserEditDTO {
    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "地址")
    private String address;


    //账单提醒
    private Boolean billReminder;
    //缴费提醒
    private Boolean paymentReminder;



}
