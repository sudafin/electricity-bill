package com.electricitybill.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.paymennt.PaymentPageQuery;
import com.electricitybill.entity.po.EbPayment;
import com.electricitybill.entity.po.EbReconciliation;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.vo.payment.PaymentDetailVO;
import com.electricitybill.entity.vo.payment.PaymentPageVO;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.EbPaymentMapper;
import com.electricitybill.mapper.EbReconciliationMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbPaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.StringUtils;
import com.electricitybill.utils.TTLGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
public class EbPaymentServiceImpl extends ServiceImpl<EbPaymentMapper, EbPayment> implements IEbPaymentService {
    @Resource
    private EbReconciliationMapper reconciliationMapper;
    @Resource
    private EbUserMapper ebUserMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public PageDTO<PaymentPageVO> queryPage(PaymentPageQuery paymentPageQuery) {
        Page<EbPayment> ebPaymentPage = new Page<>(paymentPageQuery.getPageNo(), paymentPageQuery.getPageSize());
        Page<EbPayment> paymentPage = lambdaQuery()
                .eq(StringUtils.isNotBlank(paymentPageQuery.getPaymentMethod()), EbPayment::getPaymentMethod, paymentPageQuery.getPaymentMethod())
                .eq(StringUtils.isNotBlank(paymentPageQuery.getStatus()), EbPayment::getStatus, paymentPageQuery.getStatus())
                .eq(paymentPageQuery.getPaymentId() != null, EbPayment::getId, paymentPageQuery.getPaymentId())
                .ge(paymentPageQuery.getStartDate() != null, EbPayment::getPaymentTime, paymentPageQuery.getStartDate())
                .le(paymentPageQuery.getEndDate() != null, EbPayment::getPaymentTime, paymentPageQuery.getEndDate())
                .page(ebPaymentPage);
        List<EbPayment> records = paymentPage.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(paymentPage);
        }
        ArrayList<PaymentPageVO> pageVOArrayList = new ArrayList<>();
        records.forEach(ebPayment -> {
            PaymentPageVO paymentPageVO = new PaymentPageVO();
            paymentPageVO.setPaymentMethod(ebPayment.getPaymentMethod());
            paymentPageVO.setPaymentTime(ebPayment.getPaymentTime());
            paymentPageVO.setPaymentId(ebPayment.getId());
            paymentPageVO.setStatus(ebPayment.getStatus());
            EbUser ebUser = ebUserMapper.selectById(ebPayment.getUserId());
            paymentPageVO.setUsername(ebUser.getUsername());
            paymentPageVO.setBalance(ebUser.getBalance());
            pageVOArrayList.add(paymentPageVO);
        });
        return PageDTO.of(paymentPage, pageVOArrayList);
    }

    @Override
    public PaymentDetailVO queryUserPayment(Long paymentId) {
        String paymentDetailJson = (String) stringRedisTemplate.opsForHash().get(Constant.PAYMENT_DETAIL_KEY, paymentId.toString());
        if(paymentDetailJson != null && !paymentDetailJson.isEmpty()){
            return JSONUtil.toBean(paymentDetailJson, PaymentDetailVO.class);
        }
        EbPayment ebPayment = baseMapper.selectById(paymentId);
        if(ObjectUtils.isEmpty(ebPayment)){
            throw new DbException(Constant.PAYMENT_NOT_EXIST);
        }
        PaymentDetailVO paymentDetailVO = new PaymentDetailVO();
        paymentDetailVO.setPaymentId(ebPayment.getId());
        EbUser ebUser = ebUserMapper.selectById(ebPayment.getUserId());
        paymentDetailVO.setUsername(ebUser.getUsername());
        paymentDetailVO.setUserStatus(ebUser.getAccountStatus());
        paymentDetailVO.setBalance(ebUser.getBalance());
        paymentDetailVO.setPaymentMethod(ebPayment.getPaymentMethod());
        paymentDetailVO.setStatus(ebPayment.getStatus());
        paymentDetailVO.setPaymentTime(ebPayment.getPaymentTime());
        if(ebPayment.getReconciliationId() != null) {
            EbReconciliation ebReconciliation = reconciliationMapper.selectOne(new LambdaQueryWrapper<EbReconciliation>().eq(EbReconciliation::getReconciliationNo, ebPayment.getReconciliationId()));
            paymentDetailVO.setReconciliationId(ebPayment.getId());
            paymentDetailVO.setReconciliationRemark(ebReconciliation.getComment());
            paymentDetailVO.setReconciliationStatus(ebReconciliation.getStatus());
        }
        paymentDetailVO.setIsReconciliate(ebPayment.getReconciliationId() != null);
        //设置缓存
        stringRedisTemplate.opsForHash().put(Constant.PAYMENT_DETAIL_KEY, paymentId.toString(), JSONUtil.toJsonStr(paymentDetailVO));
        stringRedisTemplate.expire(Constant.PAYMENT_DETAIL_KEY, TTLGenerator.generateDefaultRandomTTL(), TimeUnit.SECONDS);
        return paymentDetailVO;
    }

    @Override
    public R deletePayment(List<Long> ids) {
        int deleteBatchIds = baseMapper.deleteBatchIds(ids);
        if (deleteBatchIds != ids.size()) {
            throw new DbException(Constant.DB_DELETE_FAILURE);
        }
        //删除缓存
        stringRedisTemplate.delete(Constant.PAYMENT_DETAIL_KEY);
        return R.ok();
    }

    @Override
    public R refundPayment(Long paymentId) {
        EbPayment ebPayment = baseMapper.selectById(paymentId);
        if(ObjectUtils.isEmpty(ebPayment)){
            throw new DbException(Constant.PAYMENT_NOT_EXIST);
        }
        EbReconciliation ebReconciliation = reconciliationMapper.
                selectOne(new LambdaQueryWrapper<EbReconciliation>()
                        .eq(EbReconciliation::getReconciliationNo, ebPayment.getReconciliationId()));
        if(ebReconciliation.getStatus().equals("通过") || ebReconciliation.getPaymentStatus().equals("暂缓")){
            throw new BadRequestException("审批状态不是退回或者是拒绝状态, 无法执行退款操作");
        }
        ebPayment.setStatus("退款");
        ebPayment.setRefundAmount(ebPayment.getAmount());
        ebPayment.setRefundTime(LocalDateTime.now());
        int updateById = baseMapper.updateById(ebPayment);
        if (updateById != 1) {
            throw new DbException(Constant.DB_UPDATE_FAILURE);
        }
        return R.ok();
    }

    @Override
    @Async("generateReportExecutor")
    public Future<String> export() throws IOException, IOException {
        // 支付详情字段名
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add("支付单号");
        fieldNames.add("用户名");
        fieldNames.add("用户状态");
        fieldNames.add("余额");
        fieldNames.add("支付方式");
        fieldNames.add("支付时间");
        fieldNames.add("状态");
        fieldNames.add("对账单号");
        fieldNames.add("对账单状态");
        fieldNames.add("对账单备注");
        fieldNames.add("是否已对账");

        // 获取所有支付详情
        List<PaymentDetailVO> allPaymentDetails = getAllPaymentDetails();

        // 获取表的行数
        int row = allPaymentDetails.size();

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
        Map<Integer, Function<PaymentDetailVO, Object>> columnMap = new HashMap<>();
        columnMap.put(0, PaymentDetailVO::getPaymentId);
        columnMap.put(1, PaymentDetailVO::getUsername);
        columnMap.put(2, PaymentDetailVO::getUserStatus);
        columnMap.put(3, PaymentDetailVO::getBalance);
        columnMap.put(4, PaymentDetailVO::getPaymentMethod);
        columnMap.put(5, PaymentDetailVO::getPaymentTime);
        columnMap.put(6, PaymentDetailVO::getStatus);
        columnMap.put(7, PaymentDetailVO::getReconciliationId);
        columnMap.put(8, PaymentDetailVO::getReconciliationStatus);
        columnMap.put(9, PaymentDetailVO::getReconciliationRemark);
        columnMap.put(10, PaymentDetailVO::getIsReconciliate);

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
            PaymentDetailVO paymentDetail = allPaymentDetails.get(rowNum - 1);
            for (int columnNum = 0; columnNum < fieldNames.size(); columnNum++) {
                Object value = columnMap.getOrDefault(columnNum, vo -> "").apply(paymentDetail);
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

    public List<PaymentDetailVO> getAllPaymentDetails() {
        List<EbPayment> ebPayments = list(); // 查询所有支付记录
        List<PaymentDetailVO> paymentDetailVOList = new ArrayList<>();
        for (EbPayment ebPayment : ebPayments) {
            PaymentDetailVO paymentDetailVO = new PaymentDetailVO();
            paymentDetailVO.setPaymentId(ebPayment.getId());

            EbUser ebUser = ebUserMapper.selectById(ebPayment.getUserId());
            paymentDetailVO.setUsername(ebUser.getUsername());
            paymentDetailVO.setUserStatus(ebUser.getAccountStatus());
            paymentDetailVO.setBalance(ebUser.getBalance());

            paymentDetailVO.setPaymentMethod(ebPayment.getPaymentMethod());
            paymentDetailVO.setStatus(ebPayment.getStatus());
            paymentDetailVO.setPaymentTime(ebPayment.getPaymentTime());

            if (ebPayment.getReconciliationId() != null) {
                EbReconciliation ebReconciliation = reconciliationMapper.selectOne(
                        new LambdaQueryWrapper<EbReconciliation>()
                                .eq(EbReconciliation::getReconciliationNo, ebPayment.getReconciliationId())
                );
                paymentDetailVO.setReconciliationId(ebPayment.getReconciliationId()); // 修正这里应该是 ebPayment.getReconciliationId()
                paymentDetailVO.setReconciliationRemark(ebReconciliation.getComment());
                paymentDetailVO.setReconciliationStatus(ebReconciliation.getStatus());
            }

            paymentDetailVO.setIsReconciliate(ebPayment.getReconciliationId() != null);
            paymentDetailVOList.add(paymentDetailVO);
        }

        return paymentDetailVOList;
    }
}
