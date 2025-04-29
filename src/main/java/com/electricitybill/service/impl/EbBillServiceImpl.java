package com.electricitybill.service.impl;


import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.easysdk.factory.Factory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.config.AliPayConfig;
import com.electricitybill.config.AlipayClientConfig;
import com.electricitybill.config.WebSocketHandler;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.dto.AliPay;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.bill.BillPageQuery;
import com.electricitybill.entity.po.*;
import com.electricitybill.entity.vo.bill.BillAdminDetailVO;
import com.electricitybill.entity.vo.bill.BillPageAdminVO;
import com.electricitybill.entity.vo.bill.BillPageVO;
import com.electricitybill.entity.vo.bill.BillUserDetailVO;
import com.electricitybill.entity.vo.payment.PaymentDetailVO;
import com.electricitybill.enums.BillType;
import com.electricitybill.enums.PaymentType;
import com.electricitybill.enums.PeriodType;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.*;
import com.electricitybill.service.IEbBillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.BeanUtils;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-17
 */
@Service
@Slf4j

public class EbBillServiceImpl extends ServiceImpl<EbBillMapper, EbBill> implements IEbBillService {
    @Resource
    private EbUserMapper userMapper;
    @Resource
    private EbMeterMapper meterMapper;
    @Resource
    private EbPaymentMapper paymentMapper;
    @Resource
    private EbReconciliationMapper ebReconciliationMapper;
    @Resource
    private EbUsageSummaryMapper ebUsageSummaryMapper;
    @Resource
    private AlipayClient client;

    @Override
    public List<BillUserDetailVO> queryUserBill(Long userId) {
        EbUser ebUser = userMapper.selectById(userId);
        if (ObjectUtils.isEmpty(ebUser)) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        EbMeter ebMeter = meterMapper.selectById(ebUser.getMeterId());
        if (ObjectUtils.isEmpty(ebMeter)) {
            throw new DbException(Constant.METER_NOT_EXIST);
        }
        List<EbBill> ebBills = this.list(new LambdaQueryWrapper<EbBill>().eq(EbBill::getUserId, userId));
        if (CollUtils.isEmpty(ebBills)) {
            throw new BizIllegalException(Constant.BILL_NOT_EXIST);
        }
        List<BillUserDetailVO> billUserDetailVOS = BeanUtils.copyList(ebBills, BillUserDetailVO.class);
        billUserDetailVOS.forEach(billUserDetailVO -> {
            billUserDetailVO.setUserType(ebUser.getUserType());
            billUserDetailVO.setMeterId(ebUser.getMeterId());
        });
        return billUserDetailVOS;
    }

    @Override
    public PageDTO<BillPageVO> query(JSONObject jsonObject) {
        Page<EbBill> page = new Page<>(jsonObject.getInt("pageNo"), jsonObject.getInt("pageSize"));
        Page<EbBill> ebBillPage = lambdaQuery()
                .orderByDesc(EbBill::getCreatedAt)
                .page(page);
        if (ebBillPage.getTotal() == 0) {
            return PageDTO.empty(page);
        }
        List<EbBill> records = ebBillPage.getRecords();
        List<BillPageVO> billPageVOS = List.of();
        try {
            billPageVOS = records.stream().map(
                    ebBill -> {
                        BillPageVO billPageVO = new BillPageVO();
                        billPageVO.setBillId(ebBill.getId());
                        billPageVO.setUsage(ebBill.getUsageAmount());
                        billPageVO.setAmount(ebBill.getTotalAmount());
                        billPageVO.setPaymentDate(ebBill.getPaymentTime());
                        billPageVO.setDueDate(ebBill.getDueDate());
                        billPageVO.setStatus(ebBill.getStatus());
                        return billPageVO;
                    }).
                    collect(Collectors.toList());
        } catch (Exception e) {
            log.info("转换异常");
        }
        return PageDTO.of(ebBillPage, billPageVOS);
    }

    @Override
    public BillUserDetailVO detailBill(Long billId) {
        EbBill ebBill = getById(billId);
        if (ObjectUtils.isEmpty(ebBill)) {
            throw new DbException(Constant.BILL_NOT_EXIST);
        }
        EbUsageSummary ebUsageSummary = ebUsageSummaryMapper.selectOne(new LambdaQueryWrapper<EbUsageSummary>().eq(EbUsageSummary::getBillId, billId));
        BillUserDetailVO billUserDetailVO = new BillUserDetailVO();
        EbUser ebUser = userMapper.selectById(ebBill.getUserId());
        billUserDetailVO.setUserType(ebUser.getUserType());
        billUserDetailVO.setMeterId(ebUser.getMeterId());
        billUserDetailVO.setBillPeriod(ebUsageSummary.getDateType());
        billUserDetailVO.setUsage(ebBill.getUsageAmount());
        billUserDetailVO.setAmount(ebBill.getTotalAmount());
        billUserDetailVO.setStatus(ebBill.getStatus());
        billUserDetailVO.setBillDate(ebBill.getCreatedAt());
        billUserDetailVO.setPaymentDate(ebBill.getPaymentTime());
        billUserDetailVO.setPaymentMethod(ebBill.getPaymentMethod());
        billUserDetailVO.setDueDate(ebBill.getDueDate());
        billUserDetailVO.setStartReading(ebBill.getStartReading());
        billUserDetailVO.setEndReading(ebBill.getEndingReading());
        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
        for (PeriodType periodType : PeriodType.values()) {
            JSONObject jsonObject = new JSONObject();
            if (PeriodType.PEAK.getDesc().equals(periodType.getDesc())) {
                jsonObject.set("name", periodType.getDesc());
                jsonObject.set("value", ebUsageSummary.getPeakCost());
            }else if (PeriodType.FLAT.getDesc().equals(periodType.getDesc())) {
                jsonObject.set("name", periodType.getDesc());
                jsonObject.set("value", ebUsageSummary.getFlatCost());
            }else if (PeriodType.VALLEY.getDesc().equals(periodType.getDesc())) {
                jsonObject.set("name", periodType.getDesc());
                jsonObject.set("value", ebUsageSummary.getValleyCost());
            }
            jsonObjectArrayList.add(jsonObject);
        }
        billUserDetailVO.setBillDetails(jsonObjectArrayList);
        return billUserDetailVO;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> pay(AliPay aliPay) {
        Long billId = aliPay.getBillId();
        // 1. 校验账单
        EbBill ebBill = this.getById(billId);
        if (ebBill == null) {
            throw new BizIllegalException(Constant.BILL_NOT_EXIST);
        }
        if (!BillType.UNPAID.getDesc().equals(ebBill.getStatus())) {
            throw new BizIllegalException(Constant.BILL_PAID_OVERDUE);
        }

        // 2. 获取或新建支付记录
        EbPayment ebPayment;
        if (ebBill.getPaymentId() == null) {
            ebPayment = new EbPayment();
            ebPayment.setAmount(ebBill.getTotalAmount());
            ebPayment.setPaymentMethod("支付宝");
            ebPayment.setStatus(PaymentType.UNPAID.getDesc());
            ebPayment.setUserId(ebBill.getUserId());
            ebPayment.setOperatorId(0L);
            int insert = paymentMapper.insert(ebPayment);
            // 主键回写到账单
            if (insert != 1) {
                throw new DbException(Constant.DB_INSERT_FAILURE);
            }
            ebBill.setPaymentId(ebPayment.getId());
            this.updateById(ebBill);
        } else {
            ebPayment = paymentMapper.selectById(ebBill.getPaymentId());
        }

        // 3. 已支付校验
        if (PaymentType.PAID.getDesc().equals(ebPayment.getStatus())) {
            throw new BizIllegalException(Constant.PAYMENT_PAID);
        }

        // 4. 构造 AlipayClient


        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);

        try {
            // 5. 查询支付宝流水状态
            String status = checkOrderStatus(client, String.valueOf(billId));

            switch (status) {
                case "TRADE_CLOSED":
                case "NOT_EXIST":
                    // 6. 生成新二维码
                    cancelOrder(client, String.valueOf(billId));
                    String timeoutExpress = "120m";  // 设置二维码有效期为2小时
                    Map<String, String> bizContent = new HashMap<>();
                    bizContent.put("out_trade_no", String.valueOf(billId));
                    bizContent.put("total_amount", String.valueOf(ebBill.getTotalAmount()));
                    bizContent.put("subject", "电费支付");
                    bizContent.put("product_code", "FACE_TO_FACE_PAYMENT");
                    bizContent.put("timeout_express", timeoutExpress);  // 设置超时时间为2小时

                    AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
                    request.setBizContent(JSON.toJSONString(bizContent));

                    AlipayTradePrecreateResponse qr = client.execute(request);
                    if (qr.isSuccess()) {
                        resp.put("success", true);
                        resp.put("orderNo", billId.toString());
                        resp.put("qrCode", qr.getQrCode());
                        resp.put("expireTime", System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(120)); // 设置二维码过期时间为2小时
                        log.info("生成支付宝二维码成功, 订单={}，二维码={}", billId, qr.getQrCode());
                    } else {
                        String msg = "二维码生成失败：" + qr.getSubMsg();
                        resp.put("error", msg);
                        log.error(msg);
                    }
                    break;
                case "WAIT_BUYER_PAY":
                    // 7. 继续使用旧二维码
                    resp.put("success", true);
                    resp.put("orderNo", billId.toString());
                    resp.put("message", "订单等待支付，请使用原二维码");
                    resp.put("orderStatus", status);
                    break;
                default:
                    String msg = "当前状态无法重置二维码：" + status;
                    resp.put("error", msg);
                    log.warn(msg);
            }
        } catch (AlipayApiException ex) {
            String err = "支付宝接口异常：" + ex.getErrMsg();
            resp.put("error", err);
            log.error(err, ex);
        } finally {
            if (ebPayment.getStatus().equals(PaymentType.FAILED.getDesc())) {
                ebPayment.setStatus(PaymentType.UNPAID.getDesc());
                paymentMapper.updateById(ebPayment);
            }
        }

        return resp;
    }
    private void cancelOrder(AlipayClient client, String outTradeNo) {
        try {
            AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
            request.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\"}");

            AlipayTradeCancelResponse response = client.execute(request);

            if (response.isSuccess()) {
                log.info("订单撤销成功，订单号={}", outTradeNo);
            } else {
                log.error("订单撤销失败，订单号={}", outTradeNo);
            }
        } catch (AlipayApiException e) {
            log.error("撤销订单异常", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String payNotify(HttpServletRequest request) {
        // 1. 参数收集
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((name, values) -> {
            String val = request.getParameter(name);
            if (val != null && !val.isEmpty()) {
                params.put(name, val);
            }
        });

        // 2. 预置变量
        String tradeStatus = params.get("trade_status");
        String billIdStr = params.get("out_trade_no");
        String transactionId = params.get("trade_no");

        // 3. 校验核心参数
        if (billIdStr == null || transactionId == null) {
            log.error("支付宝回调参数缺失: out_order_no={}, trade_no={}", billIdStr, transactionId);
            return "success";
        }
        //转为Long,在这里转是因为转了上面不能判断位空,那么为空时会直接报错
        Long billId = Long.valueOf(billIdStr);

        // 4. 获取账单和支付记录
        EbBill ebBill = getById(billId);
        EbPayment ebPayment = paymentMapper.selectById(ebBill.getPaymentId());

        try {
            // 5. 非成功状态处理
            if (!"TRADE_SUCCESS".equals(tradeStatus)) {
                ebPayment.setStatus(PaymentType.FAILED.getDesc());
                log.warn("支付宝回调状态非成功: {}", tradeStatus);
            } else {
                // 6. 验签
                boolean verifyResult = Factory.Payment.Common()
                        .verifyNotify(params);
                if (!verifyResult) {
                    ebPayment.setStatus(PaymentType.FAILED.getDesc());
                    log.error("支付宝验签失败: out_order_no={}", billId);
                } else {
                    // 7. 支付成功
                    ebBill.setStatus(BillType.PAID.getDesc());
                    ebPayment.setStatus(PaymentType.PAID.getDesc());
                    log.info("支付确认成功: 订单号={}，交易号={}", billId, transactionId);
                    WebSocketHandler.sendMessageToAll("支付成功，订单号：" + billId);
                }
            }
        } catch (Exception e) {
            // 8. 异常统一处理
            ebPayment.setStatus(PaymentType.FAILED.getDesc());
            log.error("处理支付宝回调异常", e);
        } finally {
            // 9. 持久化更新
            updateById(ebBill);
            paymentMapper.updateById(ebPayment);
        }

        return "success";
    }

    @Override
    public PageDTO<BillPageAdminVO> queryAdmin(BillPageQuery billPageQuery) {
        Page<EbBill> page = new Page<>(billPageQuery.getPageNo(), billPageQuery.getPageSize());
        Page<EbBill> ebBillPage = lambdaQuery()
                .eq(billPageQuery.getBillId() != null, EbBill::getId, billPageQuery.getBillId())
                .eq(StringUtils.isNotBlank(billPageQuery.getStatus()), EbBill::getStatus, billPageQuery.getStatus())
                .between(billPageQuery.getStartDate() != null && billPageQuery.getEndDate() != null, EbBill::getUpdatedAt, billPageQuery.getStartDate(), billPageQuery.getEndDate())
                .orderByDesc(EbBill::getUpdatedAt)
                .page(page);
        if (ebBillPage.getTotal()== 0) {
            return PageDTO.empty(page);
        }
        Stream<BillPageAdminVO> billPageAdminVOStream = ebBillPage.getRecords().stream().map(ebBill -> {
            BillPageAdminVO billPageAdminVO = new BillPageAdminVO();
            billPageAdminVO.setBillId(ebBill.getId());
            billPageAdminVO.setUserId(ebBill.getUserId());
            billPageAdminVO.setStatus(ebBill.getStatus());
            EbUser ebUser = userMapper.selectById(ebBill.getUserId());
            billPageAdminVO.setUsername(ebUser.getUsername());
            billPageAdminVO.setUserType(ebUser.getUserType());
            billPageAdminVO.setUsageAmount(ebBill.getUsageAmount());
            billPageAdminVO.setPaymentAmount(ebBill.getTotalAmount());

            if (ebBill.getPaymentId() != null) {
                billPageAdminVO.setPaymentId(ebBill.getPaymentId());
                billPageAdminVO.setPaymentTime(paymentMapper.selectById(ebBill.getPaymentId()).getPaymentTime());
            }
            return billPageAdminVO;
        });
        return PageDTO.of(ebBillPage, billPageAdminVOStream.collect(Collectors.toList()));
    }

    @Override
    public BillAdminDetailVO queryUserBillByAdmin(Long billId) {
        EbBill ebBill = getById(billId);
        if (ObjectUtils.isEmpty(ebBill)) {
            throw new DbException(Constant.BILL_NOT_EXIST);
        }
        BillAdminDetailVO billAdminDetailVO = BeanUtils.copyBean(ebBill, BillAdminDetailVO.class);
        EbUser ebUser = userMapper.selectById(ebBill.getUserId());
        billAdminDetailVO.setUsername(ebUser.getUsername());
        billAdminDetailVO.setUserType(ebUser.getUserType());
        billAdminDetailVO.setMeterId(ebUser.getMeterId());
        List<EbPayment> ebPayments = paymentMapper.selectList(new LambdaQueryWrapper<EbPayment>()
                .eq(EbPayment::getId, ebBill.getPaymentId())
        );
        if(!ebPayments.isEmpty()) {
            ArrayList<PaymentDetailVO> paymentDetailVOS = new ArrayList<>();
            ebPayments.forEach(ebPayment -> {
                PaymentDetailVO paymentDetailVO = new PaymentDetailVO();
                paymentDetailVO.setPaymentId(ebPayment.getId());
                paymentDetailVO.setUsername(ebUser.getUsername());
                paymentDetailVO.setPaymentMethod(ebPayment.getPaymentMethod());
                paymentDetailVO.setStatus(ebPayment.getStatus());
                paymentDetailVO.setPaymentTime(ebPayment.getPaymentTime());
                EbReconciliation ebReconciliation = ebReconciliationMapper.selectById(ebPayment.getReconciliationId());
                if(ebReconciliation != null) {
                    paymentDetailVO.setIsReconciliation(true);
                    paymentDetailVO.setReconciliationId(ebPayment.getReconciliationId());
                    paymentDetailVO.setReconciliationStatus(ebReconciliation.getStatus());
                    paymentDetailVO.setReconciliationComment(ebReconciliation.getComment());
                }else {
                    paymentDetailVO.setIsReconciliation(false);
                }
                paymentDetailVOS.add(paymentDetailVO);
            });
            billAdminDetailVO.setPaymentDetailVOList(paymentDetailVOS);
        }
        return billAdminDetailVO;
    }

    @Override
    public JSONObject overview() {
        List<EbBill> ebBills = lambdaQuery().eq(EbBill::getStatus, BillType.PAID.getDesc()).list();
        int unpaidBills = lambdaQuery().eq(EbBill::getStatus, BillType.UNPAID.getDesc()).list().size();
        BigDecimal totalAmount = ebBills.stream().map(EbBill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("totalPayment", totalAmount);
        jsonObject.set("totalBills", ebBills.size());
        jsonObject.set("unpaidBills", unpaidBills);
        jsonObject.set("avgMonthlyPayment", totalAmount.divide(new BigDecimal(ebBills.size()), 2, RoundingMode.HALF_UP));
        return jsonObject;
    }

    @Override
    public String queryStatus(Long billId) throws AlipayApiException {
        // 根据 billId 查询出对应的 out_trade_no
        EbBill ebBill = getById(billId);
        if (ebBill == null) {
            throw new BizIllegalException("账单不存在");
        }

        // 调用支付宝接口查询订单状态
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"" + billId + "\"}");

        AlipayTradeQueryResponse response =client.execute(request);

        if (response.isSuccess()) {
            EbPayment ebPayment = paymentMapper.selectById(ebBill.getPaymentId());
            ebBill.setStatus(BillType.PAID.getDesc());
            ebPayment.setStatus(BillType.PAID.getDesc());
            ebPayment.setTransactionNo(response.getTradeNo());
            updateById(ebBill);
            paymentMapper.updateById(ebPayment);
            return response.getTradeStatus(); // 返回交易状态（例如 WAIT_BUYER_PAY, TRADE_SUCCESS 等）
        } else if ("ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
            return "NOT_EXIST";  // 交易不存在
        } else {
            return "QUERY_FAILED";  // 查询失败
        }
    }


    /**
     * 查询支付宝订单状态
     */
    private String checkOrderStatus(AlipayClient client, String billId) throws AlipayApiException {
        // 构造请求参数
        AlipayTradeQueryRequest req = new AlipayTradeQueryRequest();
        req.setBizContent("{\"out_trade_no\":\"" + billId + "\"}");

        // 执行请求
        AlipayTradeQueryResponse rsp = client.execute(req);

        // 请求成功的处理
        if (rsp != null && rsp.isSuccess()) {
            return rsp.getTradeStatus(); // 返回订单状态，如 WAIT_BUYER_PAY, TRADE_SUCCESS 等
        } else {
            // 请求失败时的处理
            if (rsp != null && "ACQ.TRADE_NOT_EXIST".equals(rsp.getSubCode())) {
                // 订单不存在
                return "NOT_EXIST";
            } else if (rsp != null && "QUERY_FAILED".equals(rsp.getSubCode())) {
                // 查询失败
                return "QUERY_FAILED";
            } else {
                // 默认错误处理
                return "ERROR";
            }
        }
    }



}