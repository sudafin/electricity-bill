package com.electricitybill.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.entity.export.*;
import com.electricitybill.entity.po.*;
import com.electricitybill.mapper.*;
import com.electricitybill.service.ReportExportService;
import com.electricitybill.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportExportServiceImpl implements ReportExportService {

    @Autowired
    private EbElectricityUsageMapper electricityUsageMapper;

    @Autowired
    private  EbUsageSummaryMapper ebUsageSummaryMapper;
    @Autowired
    private EbBillMapper billMapper;
    
    @Autowired
    private EbUserFeedbackMapper feedbackMapper;
    
    @Autowired
    private EbReconciliationMapper reconciliationMapper;
    
    @Autowired
    private EbUserMapper userMapper;
    
    @Autowired
    private EbPaymentMapper paymentMapper;

    // 日期格式化
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exportElectricityReport(String startDate, String endDate, String granularity, 
                                       String format, HttpServletResponse response) throws IOException {
        log.info("开始导出电量统计报表, 时间范围: {} - {}, 粒度: {}", startDate, endDate, granularity);
        
        // 解析日期
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        
        // 查询数据
        LambdaQueryWrapper<EbUsageSummary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(EbUsageSummary::getSummaryDateStart, start, end)
                   .orderByAsc(EbUsageSummary::getSummaryDateStart);
        
        List<EbUsageSummary> usageList = ebUsageSummaryMapper.selectList(queryWrapper);
        
        // 转换为导出实体
        List<EbElectricityUsageExport> exportList = usageList.stream()
                .map(usage -> {
                    EbElectricityUsageExport export = new EbElectricityUsageExport();
                    export.setUserId(usage.getUserId());
                    export.setMeterId(usage.getMeterId());
                    export.setPeakUsage(usage.getPeakUsage());
                    export.setFlatUsage(usage.getFlatUsage());
                    export.setValleyUsage(usage.getValleyUsage());
                    export.setTotalUsage(usage.getTotalUsage());
                    export.setPeriodType(usage.getDateType());
                    export.setStartTime(usage.getSummaryDateStart());
                    export.setEndTime(usage.getSummaryDateEnd());
                    export.setCreatedAt(usage.getCreatedAt());
                    return export;
                })
                .collect(Collectors.toList());
        
        // 设置Excel响应头
        setExcelResponseHeader(response, "电量统计报表");
        
        // 导出Excel
        EasyExcel.write(response.getOutputStream(), EbElectricityUsageExport.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("电量统计")
                .doWrite(exportList);
    }
    
    @Override
    public void exportFeeReport(String startDate, String endDate, String granularity, 
                               String format, HttpServletResponse response) throws IOException {
        log.info("开始导出电费统计报表, 时间范围: {} - {}, 粒度: {}", startDate, endDate, granularity);
        
        // 解析日期
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        
        // 查询数据
        LambdaQueryWrapper<EbBill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(EbBill::getCreatedAt, start, end)
                   .orderByAsc(EbBill::getCreatedAt);
        
        List<EbBill> billList = billMapper.selectList(queryWrapper);
        
        // 转换为导出实体
        List<EbBillExport> exportList = billList.stream()
                .map(bill -> {
                    EbBillExport export = new EbBillExport();
                    export.setUserId(bill.getUserId());
                    export.setMeterId(bill.getMeterId());
                    EbUsageSummary ebUsageSummary = ebUsageSummaryMapper.selectOne(new LambdaQueryWrapper<EbUsageSummary>().eq(EbUsageSummary::getBillId, bill.getId()));
                    export.setBillingPeriod(ebUsageSummary.getDateType());
                    export.setBillingDate(bill.getCreatedAt());
                    export.setTotalAmount(bill.getTotalAmount());
                    export.setPeakAmount(ebUsageSummary.getPeakCost());
                    export.setFlatAmount(ebUsageSummary.getFlatCost());
                    export.setValleyAmount(ebUsageSummary.getValleyCost());
                    export.setStatus(bill.getStatus());
                    export.setDueDate(bill.getDueDate());
                    export.setPaidDate(bill.getPaymentTime());
                    export.setCreatedAt(bill.getCreatedAt());
                    return export;
                })
                .collect(Collectors.toList());
        
        // 设置Excel响应头
        setExcelResponseHeader(response, "电费统计报表");
        
        // 导出Excel
        EasyExcel.write(response.getOutputStream(), EbBillExport.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("电费统计")
                .doWrite(exportList);
    }
    
    @Override
    public void exportFeedbackReport(String startDate, String endDate, String granularity, 
                                    String format, HttpServletResponse response) throws IOException {
        log.info("开始导出反馈统计报表, 时间范围: {} - {}, 粒度: {}", startDate, endDate, granularity);
        
        // 解析日期
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        
        // 查询数据
        LambdaQueryWrapper<EbUserFeedback> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(EbUserFeedback::getSubmitTime, start, end)
                   .orderByDesc(EbUserFeedback::getSubmitTime);
        
        List<EbUserFeedback> feedbackList = feedbackMapper.selectList(queryWrapper);
        
        // 转换为导出实体
        List<EbUserFeedbackExport> exportList = feedbackList.stream()
                .map(feedback -> {
                    EbUserFeedbackExport export = new EbUserFeedbackExport();
                    export.setUserId(feedback.getUserId());
                    export.setFeedbackType(feedback.getFeedbackType());
                    export.setContent(feedback.getContent());
                    export.setFeedbackStatus(feedback.getFeedbackStatus());
                    export.setSubmitTime(feedback.getSubmitTime());
                    export.setProcessorId(feedback.getProcessorId());
                    export.setProcessTime(feedback.getProcessTime());
                    export.setReply(feedback.getResponse());
                    export.setCreatedAt(feedback.getCreatedAt());
                    return export;
                })
                .collect(Collectors.toList());
        
        // 设置Excel响应头
        setExcelResponseHeader(response, "反馈统计报表");
        
        // 导出Excel
        EasyExcel.write(response.getOutputStream(), EbUserFeedbackExport.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("反馈统计")
                .doWrite(exportList);
    }
    
    @Override
    public void exportReconciliationReport(String startDate, String endDate, String granularity, 
                                          String format, HttpServletResponse response) throws IOException {
        log.info("开始导出对账统计报表, 时间范围: {} - {}, 粒度: {}", startDate, endDate, granularity);
        
        // 解析日期
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        
        // 查询数据
        LambdaQueryWrapper<EbReconciliation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(EbReconciliation::getStartDate, start, end)
                   .orderByDesc(EbReconciliation::getCreatedAt);
        
        List<EbReconciliation> reconciliationList = reconciliationMapper.selectList(queryWrapper);
        
        // 转换为导出实体
        List<EbReconciliationExport> exportList = reconciliationList.stream()
                .map(reconciliation -> {
                    EbReconciliationExport export = new EbReconciliationExport();
                    export.setUserId(reconciliation.getUserId());
                    export.setStartDate(reconciliation.getStartDate());
                    export.setEndDate(reconciliation.getEndDate());
                    export.setTotalAmount(reconciliation.getTotalAmount());
                    export.setStatus(reconciliation.getStatus());
                    export.setApproverId(reconciliation.getApproverId());
                    export.setApprovalTime(reconciliation.getApprovalTime());
                    export.setPaymentStatus(reconciliation.getPaymentStatus());
                    export.setCreatedAt(reconciliation.getCreatedAt());
                    return export;
                })
                .collect(Collectors.toList());
        
        // 设置Excel响应头
        setExcelResponseHeader(response, "对账统计报表");
        
        // 导出Excel
        EasyExcel.write(response.getOutputStream(), EbReconciliationExport.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("对账统计")
                .doWrite(exportList);
    }
    
    @Override
    public void exportUserTypeReport(String startDate, String endDate, String granularity, 
                                     String format, HttpServletResponse response) throws IOException {
        log.info("开始导出用户类型统计报表, 时间范围: {} - {}, 粒度: {}", startDate, endDate, granularity);
        
        // 解析日期
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        
        // 查询数据 - 由于用户注册时间可能不在范围内，这里需要特殊处理
        LambdaQueryWrapper<EbUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(EbUser::getCreatedAt, end) // 创建时间早于结束时间
                  .orderByAsc(EbUser::getUserType);
        
        List<EbUser> userList = userMapper.selectList(queryWrapper);
        
        // 转换为导出实体
        List<EbUserExport> exportList = userList.stream()
                .map(user -> {
                    EbUserExport export = new EbUserExport();
                    export.setAccount(user.getAccount());
                    export.setUsername(user.getUsername());
                    export.setUserType(user.getUserType());
                    export.setPhone(user.getPhone());
                    export.setAddress(user.getAddress());
                    export.setAccountStatus(user.getAccountStatus());
                    export.setCreatedAt(user.getCreatedAt());
                    return export;
                })
                .collect(Collectors.toList());
        
        // 设置Excel响应头
        setExcelResponseHeader(response, "用户类型统计报表");
        
        // 导出Excel
        EasyExcel.write(response.getOutputStream(), EbUserExport.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("用户类型统计")
                .doWrite(exportList);
    }
    
    /**
     * 设置Excel响应头
     */
    private void setExcelResponseHeader(HttpServletResponse response, String fileName) throws IOException {
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }
}