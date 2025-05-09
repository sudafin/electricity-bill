package com.electricitybill.entity.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EbUserExport {
    
    @ExcelProperty("账号")
    private String account;
    
    @ExcelProperty("用户名")
    private String username;
    
    @ExcelProperty("用户类型")
    private String userType;
    
    @ExcelProperty("邮箱")
    private String email;
    
    @ExcelProperty("电话")
    private String phone;
    
    @ExcelProperty("地址")
    @ColumnWidth(50)
    private String address;
    
    @ExcelProperty("账号状态")
    private String accountStatus;
    
    @ExcelProperty("最后登录时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime lastLoginTime;
    
    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime createdAt;
}