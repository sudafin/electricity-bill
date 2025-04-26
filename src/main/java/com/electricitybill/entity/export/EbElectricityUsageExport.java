package com.electricitybill.entity.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EbElectricityUsageExport {
    
    @ExcelProperty("用户ID")
    private Long userId;
    
    @ExcelProperty("电表ID")
    private String meterId;
    
    @ExcelProperty("尖峰用电量(kWh)")
    private BigDecimal peakUsage;
    
    @ExcelProperty("平段用电量(kWh)")
    private BigDecimal flatUsage;
    
    @ExcelProperty("谷段用电量(kWh)")
    private BigDecimal valleyUsage;
    
    @ExcelProperty("总用电量(kWh)")
    private BigDecimal totalUsage;
    
    @ExcelProperty("周期类型")
    private String periodType;
    
    @ExcelProperty("开始时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime startTime;
    
    @ExcelProperty("结束时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime endTime;
    
    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime createdAt;
}