package com.electricitybill.entity.vo.user;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDetailVO extends UserPageVO{
    List<UserPaymentRecordVO> userPaymentRecordVOList;
}
