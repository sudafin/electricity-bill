package com.electricitybill.entity.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EbBillExport {
    
    @ExcelProperty("用户ID")
    private Long userId;
    
    @ExcelProperty("电表ID")
    private String meterId;
    
    @ExcelProperty("账单周期")
    private String billingPeriod;
    
    @ExcelProperty("账单日期")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime billingDate;
    
    @ExcelProperty("总金额(元)")
    private BigDecimal totalAmount;
    
    @ExcelProperty("尖峰电费(元)")
    private BigDecimal peakAmount;
    
    @ExcelProperty("平段电费(元)")
    private BigDecimal flatAmount;
    
    @ExcelProperty("谷段电费(元)")
    private BigDecimal valleyAmount;
    
    @ExcelProperty("账单状态")
    private String status;
    
    @ExcelProperty("截止日期")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime dueDate;
    
    @ExcelProperty("支付日期")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime paidDate;
    
    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime createdAt;
}