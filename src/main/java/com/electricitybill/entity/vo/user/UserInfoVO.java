package com.electricitybill.entity.vo.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserInfoVO {
    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "地址")
    private String address;

    @ApiModelProperty(value = "电表模型")
    private String meterModel;

    @ApiModelProperty(value = "用户类型: 居民用户/商业用户")
    private String userType;

    @ApiModelProperty(value = "账号状态: 正常/欠费/停用")
    private String accountStatus;

    @ApiModelProperty(value = "身份证号")
    private String idCardNo;

    //账单提醒
    private Boolean billReminder;
    //缴费提醒
    private Boolean paymentReminder;

}
