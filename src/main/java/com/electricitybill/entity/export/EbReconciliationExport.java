package com.electricitybill.entity.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EbReconciliationExport {
    
    @ExcelProperty("用户ID")
    private Long userId;
    
    @ExcelProperty("开始日期")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime startDate;
    
    @ExcelProperty("结束日期")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime endDate;
    
    @ExcelProperty("总金额(元)")
    private BigDecimal totalAmount;
    
    @ExcelProperty("审批状态")
    private String status;
    
    @ExcelProperty("审批人ID")
    private Long approverId;
    
    @ExcelProperty("审批时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime approvalTime;
    
    @ExcelProperty("支付状态")
    private String paymentStatus;
    
    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime createdAt;
}