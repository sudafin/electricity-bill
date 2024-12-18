package com.electricitybill.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.log.LogDTO;
import com.electricitybill.entity.dto.log.LogPageQuery;
import com.electricitybill.entity.po.EbAdmin;
import com.electricitybill.entity.po.EbSystemLog;
import com.electricitybill.entity.vo.log.LogDetailVO;
import com.electricitybill.entity.vo.log.LogPageVO;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.mapper.EbAdminMapper;
import com.electricitybill.mapper.EbSystemLogMapper;
import com.electricitybill.service.IEbSystemLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.StringUtils;
import com.electricitybill.utils.UserContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
@Slf4j
public class EbSystemLogServiceImpl extends ServiceImpl<EbSystemLogMapper, EbSystemLog> implements IEbSystemLogService {
    @Resource
    private EbAdminMapper ebAdminMapper;
    @Override
    public PageDTO<LogPageVO> queryPage(LogPageQuery logPageQuery) {
        Page<EbSystemLog> ebSystemLogPage = new Page<>(logPageQuery.getPageNo(), logPageQuery.getPageSize());
        Page<EbSystemLog> systemLogPage = lambdaQuery()
                .eq(StringUtils.isNotBlank(logPageQuery.getOperationType()), EbSystemLog::getOperationType, logPageQuery.getOperationType())
                .eq(StringUtils.isNotBlank(logPageQuery.getModule()), EbSystemLog::getModule, logPageQuery.getModule())
                .like(StringUtils.isNotBlank(logPageQuery.getOperatorName()), EbSystemLog::getDescription, logPageQuery.getOperatorName())
                .ge(logPageQuery.getStartDate() != null, EbSystemLog::getCreatedAt, logPageQuery.getStartDate())
                .le(logPageQuery.getEndDate() != null, EbSystemLog::getCreatedAt, logPageQuery.getEndDate())
                .page(ebSystemLogPage);
        List<EbSystemLog> records = systemLogPage.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(systemLogPage);
        }
        ArrayList<LogPageVO> pageVOArrayList = new ArrayList<>();
        records.forEach(ebSystemLog -> {
            LogPageVO logPageVO = new LogPageVO();
            logPageVO.setId(ebSystemLog.getId());
            logPageVO.setOperatorName(ebSystemLog.getOperatorName());
            logPageVO.setOperationType(ebSystemLog.getOperationType());
            logPageVO.setModule(ebSystemLog.getModule());
            logPageVO.setDescription(ebSystemLog.getDescription());
            logPageVO.setIp(ebSystemLog.getIp());
            logPageVO.setStatus(ebSystemLog.getStatus());
            logPageVO.setCreateTime(ebSystemLog.getCreatedAt());
            pageVOArrayList.add(logPageVO);
        });
        return PageDTO.of(systemLogPage, pageVOArrayList);
    }

    @Override
    public LogDetailVO queryDetail(Long id) {
        EbSystemLog ebSystemLog = getById(id);
        if(ObjectUtils.isEmpty(ebSystemLog)){
            throw new BadRequestException(Constant.LOG_NOT_EXIST);
        }
        LogDetailVO logDetailVO = new LogDetailVO();
        logDetailVO.setOperatorName(ebSystemLog.getOperatorName());
        logDetailVO.setOperationType(ebSystemLog.getOperationType());
        logDetailVO.setModule(ebSystemLog.getModule());
        logDetailVO.setDescription(ebSystemLog.getDescription());
        logDetailVO.setIp(ebSystemLog.getIp());
        logDetailVO.setStatus(ebSystemLog.getStatus());
        logDetailVO.setRequestParams(JSONUtil.escape(ebSystemLog.getRequestParams()));
        logDetailVO.setResponseData(ebSystemLog.getResponseData());
        logDetailVO.setErrorMsg(ebSystemLog.getErrorMsg());
        logDetailVO.setRequestBody(ebSystemLog.getRequestBody());
        logDetailVO.setCreateTime(ebSystemLog.getCreatedAt());
        return logDetailVO;
    }

    @Override
    @Async("generateReportExecutor")
    public Future<String> export() throws IOException {
        List<EbSystemLog> list = list();
        // 获取表的行数
        int row = list.size();
        // 获取表的各个列名
        String[] columnName = {"Id", "操作人", "操作类型", "模块", "描述", "IP", "状态", "请求参数","请求数据", "返回数据", "用户设备", "错误信息", "创建时间"};

        // 创建工作簿和表格
        Workbook excel = new XSSFWorkbook();
        Sheet sheet = excel.createSheet();

        // 设置样式：字体、颜色、对齐方式等
        CellStyle headerStyle = excel.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER); // 设置水平居中
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 设置垂直居中
        Font headerFont = excel.createFont();
        headerFont.setBold(true); // 设置字体加粗
        headerStyle.setFont(headerFont);

        // 为每列设置自适应宽度
        for (int i = 0; i < columnName.length; i++) {
            sheet.setColumnWidth(i, 15 * 256); // 设置列宽（调整15为合适的列宽值）
        }

        // 创建表头
        Row headerRow = sheet.createRow(0); // 创建一行
        for (int columnNum = 0; columnNum < columnName.length; columnNum++) {
            Cell cell = headerRow.createCell(columnNum);
            cell.setCellValue(columnName[columnNum]); // 设置列名
            cell.setCellStyle(headerStyle); // 应用样式
        }

        // 创建列映射
        Map<Integer, Function<EbSystemLog, Object>> columnMap = new HashMap<>();
        columnMap.put(0, EbSystemLog::getId);
        columnMap.put(1, EbSystemLog::getOperatorName);
        columnMap.put(2, EbSystemLog::getOperationType);
        columnMap.put(3, EbSystemLog::getModule);
        columnMap.put(4, EbSystemLog::getDescription);
        columnMap.put(5, EbSystemLog::getIp);
        columnMap.put(6, EbSystemLog::getStatus);
        columnMap.put(7, EbSystemLog::getRequestParams);
        columnMap.put(9, EbSystemLog::getRequestBody);
        columnMap.put(8, EbSystemLog::getResponseData);
        columnMap.put(9, EbSystemLog::getUserAgent);
        columnMap.put(10, EbSystemLog::getErrorMsg);
        columnMap.put(11, log -> String.valueOf(log.getCreatedAt()));

        // 设置数据行的样式
        CellStyle dataStyle = excel.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        dataStyle.setBorderTop(BorderStyle.THIN); // 设置边框
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);

        // 按列插入数据
        for (int rowNum = 1; rowNum <= row; rowNum++) {
            Row sheetRow = sheet.createRow(rowNum);
            EbSystemLog ebSystemLog = list.get(rowNum - 1);
            for (int columnNum = 0; columnNum < columnName.length; columnNum++) {
                Object value = columnMap.getOrDefault(columnNum, log -> "").apply(ebSystemLog);
                if (value == null) {
                    value = ""; // 或者其他默认值
                }
                Cell cell = sheetRow.createCell(columnNum);
                cell.setCellValue(value.toString());
                cell.setCellStyle(dataStyle); // 应用数据行样式
            }
        }

        //把execl保存到临时文件中
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = "report_" + System.currentTimeMillis() + ".xlsx";
        String filePath = tempDir + File.separator + fileName;
        FileOutputStream outputStream = new FileOutputStream(filePath);
        excel.write(outputStream);
        excel.close();
        outputStream.close();
        log.info("文件路径：{}", filePath);
        return new AsyncResult<>(filePath);
    }

    @Override
    public void saveLog(LogDTO logDTO) {
        EbSystemLog ebSystemLog = new EbSystemLog();
        Long user = UserContextUtils.getUser();
        EbAdmin ebAdmin = ebAdminMapper.selectById(user);
        ebSystemLog.setOperatorId(ebAdmin.getId());
        ebSystemLog.setOperatorName(ebAdmin.getAccount());
        ebSystemLog.setRequestBody(logDTO.getRequestBody());
        ebSystemLog.setRequestParams(logDTO.getRequestParamMap());
        ebSystemLog.setResponseData(logDTO.getResponseBody());
        ebSystemLog.setIp(logDTO.getIp());
        ebSystemLog.setUserAgent(logDTO.getUserAgent());
        ebSystemLog.setStatus("success");
        ebSystemLog.setErrorMsg(logDTO.getErrorMsg());
        //eg: /user/page ,拿到第一个字符串
        String[] split = logDTO.getPath().split("/");
        String module = split[1];
        ebSystemLog.setModule(module);
        String method = logDTO.getMethod();
        switch (method) {
            case "GET":
                ebSystemLog.setOperationType("查询");
                ebSystemLog.setDescription(ebAdmin.getAccount() +"查询" + module);
            break;
            case "POST": ebSystemLog.setOperationType("新增");
            ebSystemLog.setDescription(ebAdmin.getAccount() +"新增" + module);
            break;
            case "PUT": ebSystemLog.setOperationType("修改");
            ebSystemLog.setDescription(ebAdmin.getAccount() +"修改" + module);
            break;
            case "DELETE": ebSystemLog.setOperationType("删除");
            ebSystemLog.setDescription(ebAdmin.getAccount() +"删除" + module);
            break;
            default: ebSystemLog.setOperationType("未知");
            ebSystemLog.setDescription("未知");
        }
        save(ebSystemLog);
    }

}
