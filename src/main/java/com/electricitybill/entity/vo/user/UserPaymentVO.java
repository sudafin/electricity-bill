package com.electricitybill.entity.vo.user;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserPaymentVO {
    private String username;
    private String userType;
    private String meterNo;
    private BigDecimal unpaidAmount;
    private BigDecimal balance;

}
