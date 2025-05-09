package com.electricitybill.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ReportExportService {

    /**
     * 导出电量统计报表
     */
    void exportElectricityReport(String startDate, String endDate, String granularity, 
                                String format, HttpServletResponse response) throws IOException;

    /**
     * 导出电费统计报表
     */
    void exportFeeReport(String startDate, String endDate, String granularity, 
                        String format, HttpServletResponse response) throws IOException;

    /**
     * 导出反馈统计报表
     */
    void exportFeedbackReport(String startDate, String endDate, String granularity, 
                             String format, HttpServletResponse response) throws IOException;

    /**
     * 导出对账统计报表
     */
    void exportReconciliationReport(String startDate, String endDate, String granularity, 
                                   String format, HttpServletResponse response) throws IOException;

    /**
     * 导出用户类型统计报表
     */
    void exportUserTypeReport(String startDate, String endDate, String granularity, 
                             String format, HttpServletResponse response) throws IOException;
}