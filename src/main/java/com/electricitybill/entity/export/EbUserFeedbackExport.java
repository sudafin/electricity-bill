package com.electricitybill.entity.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EbUserFeedbackExport {
    
    @ExcelProperty("用户ID")
    private Long userId;
    
    @ExcelProperty("反馈类型")
    private String feedbackType;
    
    @ExcelProperty("反馈内容")
    @ColumnWidth(50)
    private String content;
    
    @ExcelProperty("处理状态")
    private String feedbackStatus;
    
    @ExcelProperty("提交时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime submitTime;
    
    @ExcelProperty("处理人ID")
    private Long processorId;
    
    @ExcelProperty("处理时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime processTime;
    
    @ExcelProperty("回复内容")
    @ColumnWidth(50)
    private String reply;
    
    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(20)
    private LocalDateTime createdAt;
}