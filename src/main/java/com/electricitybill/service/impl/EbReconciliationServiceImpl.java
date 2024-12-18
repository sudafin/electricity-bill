package com.electricitybill.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.reconciliation.ApprovalDTO;
import com.electricitybill.entity.po.EbAdmin;
import com.electricitybill.entity.vo.reconciliation.ApprovalDetailVO;
import com.electricitybill.entity.vo.reconciliation.ApprovalRecordVO;
import com.electricitybill.entity.vo.user.UserPaymentRecordVO;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.mapper.EbAdminMapper;
import com.electricitybill.utils.UserContextUtils;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.reconciliation.ReconciliationPageQuery;
import com.electricitybill.entity.po.EbPayment;
import com.electricitybill.entity.po.EbReconciliation;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.vo.reconciliation.ReconciliationDetailVO;
import com.electricitybill.entity.vo.reconciliation.ReconciliationPageVO;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.EbPaymentMapper;
import com.electricitybill.mapper.EbReconciliationMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbReconciliationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
@Slf4j
public class EbReconciliationServiceImpl extends ServiceImpl<EbReconciliationMapper, EbReconciliation> implements IEbReconciliationService {
    @Resource
    private EbUserMapper ebUserMapper;
    @Resource
    private EbPaymentMapper ebPaymentMapper;
    @Resource
    private EbAdminMapper adminMapper;

    @Override
    public PageDTO<ReconciliationPageVO> queryPage(ReconciliationPageQuery reconciliationPageQuery) {
        log.debug("queryPage:{}", reconciliationPageQuery);
        Page<EbReconciliation> ebReconciliationPage = new Page<>(reconciliationPageQuery.getPageNo(), reconciliationPageQuery.getPageSize());
        //reconciliation只有userId如果想要通过用户名查找需要先查询用户的信息
        List<EbUser> ebUserList = ebUserMapper.selectList(new LambdaQueryWrapper<EbUser>().eq(StringUtils.isNotBlank(reconciliationPageQuery.getMeterNo()), EbUser::getMeterNo, reconciliationPageQuery.getMeterNo()).eq(StringUtils.isNotBlank(reconciliationPageQuery.getUsername()), EbUser::getUsername, reconciliationPageQuery.getUsername()).eq(StringUtils.isNotBlank(reconciliationPageQuery.getUserType()), EbUser::getUserType, reconciliationPageQuery.getUserType()));
        if (CollUtils.isEmpty(ebUserList) && StringUtils.isNotBlank(reconciliationPageQuery.getUsername())) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        //收集id
        Set<Long> ids = ebUserList.stream().mapToLong(EbUser::getId).boxed().collect(Collectors.toSet());
        //正式查询
        Page<EbReconciliation> reconciliationPage = lambdaQuery()
                //假如其中有不为空的字段就走这个查询, 如果都为空就不走这个查询
                .in(!StringUtils.isAllBlank(reconciliationPageQuery.getMeterNo(), reconciliationPageQuery.getUsername(), reconciliationPageQuery.getUserType()), EbReconciliation::getUserId, ids).eq(StringUtils.isNotBlank(reconciliationPageQuery.getReconciliationNo()), EbReconciliation::getReconciliationNo, reconciliationPageQuery.getReconciliationNo()).eq(StringUtils.isNotBlank(reconciliationPageQuery.getReconciliationStatus()), EbReconciliation::getStatus, reconciliationPageQuery.getReconciliationStatus()).ge(reconciliationPageQuery.getStartDate() != null, EbReconciliation::getStartDate, reconciliationPageQuery.getStartDate()).le(reconciliationPageQuery.getEndDate() != null, EbReconciliation::getStartDate, reconciliationPageQuery.getEndDate()).page(ebReconciliationPage);
        List<EbReconciliation> records = reconciliationPage.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(ebReconciliationPage);
        }
        List<ReconciliationPageVO> reconciliationPageVOList = records.stream().map(reconciliation -> {
            ReconciliationPageVO reconciliationPageVO = new ReconciliationPageVO();
            reconciliationPageVO.setReconciliationNo(reconciliation.getReconciliationNo());
            reconciliationPageVO.setReconciliationStatus(reconciliation.getStatus());
            reconciliationPageVO.setReconciliationTime(reconciliation.getStartDate());
            reconciliationPageVO.setBalance(reconciliation.getTotalAmount());
            ebUserList.stream().filter(ebUser -> ebUser.getId().equals(reconciliation.getUserId())).findFirst().ifPresent(ebUser -> {
                reconciliationPageVO.setUsername(ebUser.getUsername());
                reconciliationPageVO.setMeterNo(ebUser.getMeterNo());
                reconciliationPageVO.setUserType(ebUser.getUserType());
            });
            return reconciliationPageVO;
        }).collect(Collectors.toList());
        return new PageDTO<>(reconciliationPage.getTotal(), reconciliationPage.getPages(), reconciliationPageVOList);
    }

    @Override
    public ReconciliationDetailVO queryReconciliationDetail(Long reconciliationId) {
        ReconciliationDetailVO reconciliationDetailVO = new ReconciliationDetailVO();
        EbReconciliation ebReconciliation = lambdaQuery().eq(EbReconciliation::getReconciliationNo, reconciliationId).one();
        if (ObjectUtils.isEmpty(ebReconciliation)) {
            throw new DbException(Constant.RECONCILIATION_NOT_EXIST);
        }
        EbUser ebUser = ebUserMapper.selectById(ebReconciliation.getUserId());
        List<EbPayment> ebPaymentList = ebPaymentMapper.selectList(new LambdaQueryWrapper<EbPayment>().eq(EbPayment::getUserId, ebReconciliation.getUserId()));
        ArrayList<UserPaymentRecordVO> recordVOArrayList = new ArrayList<>();
        if (CollUtils.isEmpty(ebPaymentList)) {
            reconciliationDetailVO.setUserPaymentRecordVOList(CollUtils.emptyList());
        } else {
            ebPaymentList.forEach(ebPayment -> {
                UserPaymentRecordVO userPaymentRecordVO = new UserPaymentRecordVO();
                userPaymentRecordVO.setPaymentAmount(ebPayment.getAmount());
                userPaymentRecordVO.setPaymentStatus(ebPayment.getStatus());
                EbAdmin ebAdmin = adminMapper.selectById(ebPayment.getOperatorId());
                if (ObjectUtils.isEmpty(ebAdmin)) {
                    throw new DbException(Constant.DATA_QUERY_EMPTY);
                } else {
                    userPaymentRecordVO.setOperator(ebAdmin.getAccount());
                    userPaymentRecordVO.setRemark(ebPayment.getRemark());
                    userPaymentRecordVO.setPaymentTime(ebPayment.getPaymentTime());
                    userPaymentRecordVO.setPaymentMethod(ebPayment.getPaymentMethod());
                    recordVOArrayList.add(userPaymentRecordVO);
                }
                log.debug("userPaymentRecordVO的数据:{}", userPaymentRecordVO);
            });
        }
        reconciliationDetailVO.setReconciliationNo(ebReconciliation.getReconciliationNo());
        reconciliationDetailVO.setUsername(ebUser.getUsername());
        reconciliationDetailVO.setUserType(ebUser.getUserType());
        reconciliationDetailVO.setReconciliationStatus(ebReconciliation.getStatus());
        reconciliationDetailVO.setMeterNo(ebUser.getMeterNo());
        reconciliationDetailVO.setCreateTime(ebReconciliation.getCreatedAt());
        reconciliationDetailVO.setBalance(ebReconciliation.getTotalAmount());
        reconciliationDetailVO.setApprovalTime(ebReconciliation.getApprovalTime());
        if (ebReconciliation.getApproverId() != null) {
            EbAdmin ebAdmin = adminMapper.selectById(ebReconciliation.getApproverId());
            reconciliationDetailVO.setApprovalOperator(ebAdmin.getAccount());
        } else reconciliationDetailVO.setApprovalOperator(null);
        reconciliationDetailVO.setApprovalComment(ebReconciliation.getComment());
        reconciliationDetailVO.setUserPaymentRecordVOList(recordVOArrayList);
        reconciliationDetailVO.setPaymentStatus(ebReconciliation.getPaymentStatus());
        return reconciliationDetailVO;
    }

    @Override
    public R approveReconciliation(Long reconciliationId, ApprovalDTO approvalDTO) {
        EbReconciliation ebReconciliation = lambdaQuery().eq(EbReconciliation::getReconciliationNo, reconciliationId).one();
        if (ObjectUtils.isEmpty(ebReconciliation)) {
            throw new DbException(Constant.RECONCILIATION_NOT_EXIST);
        }
        if (ebReconciliation.getStatus().equals(approvalDTO.getStatus())) {
            throw new BadRequestException(Constant.RECONCILIATION_STATUS_NOT_CHANGE);
        }
        ebReconciliation.setStatus(approvalDTO.getStatus());
        ebReconciliation.setComment(approvalDTO.getComment());
        ebReconciliation.setApprovalTime(LocalDateTime.now());
        ebReconciliation.setApproverId(UserContextUtils.getUser());
        int res = baseMapper.updateById(ebReconciliation);
        if (res <= 0) {

            throw new DbException(Constant.DATA_UPDATE_FAILURE);
        }
        return R.ok();
    }

    @Override
    public ApprovalDetailVO queryApprovalReconciliationDetail(Long reconciliationId) {
        ApprovalDetailVO approvalDetailVO = new ApprovalDetailVO();
        EbReconciliation ebReconciliation = lambdaQuery().eq(EbReconciliation::getReconciliationNo, reconciliationId).one();
        if (ObjectUtils.isEmpty(ebReconciliation)) {
            throw new DbException(Constant.RECONCILIATION_NOT_EXIST);
        }
        EbUser ebUser = ebUserMapper.selectById(ebReconciliation.getUserId());
        approvalDetailVO.setReconciliationNo(ebReconciliation.getReconciliationNo());
        approvalDetailVO.setUsername(ebUser.getUsername());
        approvalDetailVO.setBalance(ebReconciliation.getTotalAmount());
        approvalDetailVO.setComment(ebReconciliation.getComment());
        approvalDetailVO.setIsApproved(!ebReconciliation.getStatus().equals("待审批") && !ebReconciliation.getStatus().equals("暂缓"));
        approvalDetailVO.setStatus(ebReconciliation.getStatus());
        List<EbReconciliation> list = lambdaQuery().eq(EbReconciliation::getUserId, ebReconciliation.getUserId()).list();
        //把当前的审批记录排除
        list.remove(ebReconciliation);
        if (CollUtils.isEmpty(list)) {
            approvalDetailVO.setApprovalRecordList(CollUtils.emptyList());
        } else {
            List<ApprovalRecordVO> approvalRecordVOList = list.stream().map(reconciliation -> {
                ApprovalRecordVO approvalRecordVO = new ApprovalRecordVO();
                //审批人id需要判断存在, 不存在说明未审批
                if (reconciliation.getApproverId() != null) {
                    approvalRecordVO.setReconciliationNo(reconciliation.getReconciliationNo());
                    approvalRecordVO.setApprovalStatus(reconciliation.getStatus());
                    approvalRecordVO.setApprovalTime(reconciliation.getApprovalTime());
                    approvalRecordVO.setApprovalOperator(adminMapper.selectById(reconciliation.getApproverId()).getAccount());
                    approvalRecordVO.setComment(reconciliation.getComment());
                }
                return approvalRecordVO;
            }).collect(Collectors.toList());
            approvalDetailVO.setApprovalRecordList(approvalRecordVOList);
        }
        return approvalDetailVO;
    }

    @Override
    @Async("generateReportExecutor")
    public Future<String> export() throws IOException {
        // 对账单字段名
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add("对账单号");
        fieldNames.add("用户名");
        fieldNames.add("用户类型");
        fieldNames.add("对账单状态");
        fieldNames.add("电表号");
        fieldNames.add("创建时间");
        fieldNames.add("余额");
        fieldNames.add("支付状态");
        fieldNames.add("审批时间");
        fieldNames.add("审批操作员");
        fieldNames.add("审批备注");

        // 获取所有对账单数据
        List<ReconciliationDetailVO> allReconciliationDetailsWithoutUserPayments = getAllReconciliationDetailsWithoutUserPayments();

        // 获取表的行数
        int row = allReconciliationDetailsWithoutUserPayments.size();

        // 创建工作簿和表格
        Workbook excel = new XSSFWorkbook();
        Sheet sheet = excel.createSheet();

        // 设置表头样式
        CellStyle headerStyle = excel.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER); // 设置水平居中
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 设置垂直居中
        Font headerFont = excel.createFont();
        headerFont.setBold(true); // 设置字体加粗
        headerStyle.setFont(headerFont);

        // 为每列设置自适应宽度
        for (int i = 0; i < fieldNames.size(); i++) {
            sheet.setColumnWidth(i, 15 * 256); // 设置列宽
        }

        // 创建表头
        Row headerRow = sheet.createRow(0); // 创建一行
        for (int columnNum = 0; columnNum < fieldNames.size(); columnNum++) {
            Cell cell = headerRow.createCell(columnNum);
            cell.setCellValue(fieldNames.get(columnNum)); // 设置列名
            cell.setCellStyle(headerStyle); // 应用样式
        }

        // 创建列映射，映射字段与数据对象中的属性
        Map<Integer, Function<ReconciliationDetailVO, Object>> columnMap = new HashMap<>();
        columnMap.put(0, ReconciliationDetailVO::getReconciliationNo);
        columnMap.put(1, ReconciliationDetailVO::getUsername);
        columnMap.put(2, ReconciliationDetailVO::getUserType);
        columnMap.put(3, ReconciliationDetailVO::getReconciliationStatus);
        columnMap.put(4, ReconciliationDetailVO::getMeterNo);
        columnMap.put(5, ReconciliationDetailVO::getCreateTime);
        columnMap.put(6, ReconciliationDetailVO::getBalance);
        columnMap.put(7, ReconciliationDetailVO::getPaymentStatus);
        columnMap.put(8, ReconciliationDetailVO::getApprovalTime);
        columnMap.put(9, ReconciliationDetailVO::getApprovalOperator);
        columnMap.put(10, ReconciliationDetailVO::getApprovalComment);
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
            ReconciliationDetailVO reconciliationDetail = allReconciliationDetailsWithoutUserPayments.get(rowNum - 1);
            for (int columnNum = 0; columnNum < fieldNames.size(); columnNum++) {
                Object value = columnMap.getOrDefault(columnNum, vo -> "").apply(reconciliationDetail);
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


    public List<ReconciliationDetailVO> getAllReconciliationDetailsWithoutUserPayments() {
        List<EbReconciliation> allReconciliations = list(); // 获取所有的对账记录
        List<ReconciliationDetailVO> reconciliationDetailVOList = new ArrayList<>();

        for (EbReconciliation ebReconciliation : allReconciliations) {
            ReconciliationDetailVO reconciliationDetailVO = new ReconciliationDetailVO();

            EbUser ebUser = ebUserMapper.selectById(ebReconciliation.getUserId());
            if (ObjectUtils.isEmpty(ebUser)) {
                throw new DbException(Constant.USER_NOT_EXIST);
            }

            reconciliationDetailVO.setReconciliationNo(ebReconciliation.getReconciliationNo());
            reconciliationDetailVO.setUsername(ebUser.getUsername());
            reconciliationDetailVO.setUserType(ebUser.getUserType());
            reconciliationDetailVO.setReconciliationStatus(ebReconciliation.getStatus());
            reconciliationDetailVO.setMeterNo(ebUser.getMeterNo());
            reconciliationDetailVO.setCreateTime(ebReconciliation.getCreatedAt());
            reconciliationDetailVO.setBalance(ebReconciliation.getTotalAmount());
            reconciliationDetailVO.setApprovalTime(ebReconciliation.getApprovalTime());
            if (ebReconciliation.getApproverId() != null) {
                EbAdmin ebAdmin = adminMapper.selectById(ebReconciliation.getApproverId());
                reconciliationDetailVO.setApprovalOperator(ebAdmin.getAccount());
            } else {
                reconciliationDetailVO.setApprovalOperator(null);
            }
            reconciliationDetailVO.setApprovalComment(ebReconciliation.getComment());
            reconciliationDetailVO.setPaymentStatus(ebReconciliation.getPaymentStatus());
            reconciliationDetailVOList.add(reconciliationDetailVO);
        }

        return reconciliationDetailVOList;
    }

}
