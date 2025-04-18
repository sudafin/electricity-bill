package com.electricitybill.service.impl;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.config.AliPayConfig;
import com.electricitybill.config.WebSocketHandler;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.dto.AliPay;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.bill.BillPageQuery;
import com.electricitybill.entity.po.EbBill;
import com.electricitybill.entity.po.EbMeter;
import com.electricitybill.entity.po.EbPayment;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.vo.bill.BillPageVO;
import com.electricitybill.entity.vo.user.UserBillVO;
import com.electricitybill.enums.BillType;
import com.electricitybill.enums.PaymentType;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.EbBillMapper;
import com.electricitybill.mapper.EbMeterMapper;
import com.electricitybill.mapper.EbPaymentMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbBillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.BeanUtils;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "utf-8";
    private static final String SIGN_TYPE = "RSA2";
    @Resource
    private AliPayConfig aliPayConfig;
    @Resource
    private EbUserMapper userMapper;
    @Resource
    private EbMeterMapper meterMapper;
    @Resource
    private EbPaymentMapper paymentMapper;

    @Override
    public List<UserBillVO> queryUserBill(Long userId) {
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
        List<UserBillVO> userBillVOS = BeanUtils.copyList(ebBills, UserBillVO.class);
        userBillVOS.forEach(userBillVO -> {
            userBillVO.setUserType(ebUser.getUserType());
            userBillVO.setMeterId(ebUser.getMeterId());
            userBillVO.setUsername(ebUser.getUsername());
        });
        return userBillVOS;
    }

    @Override
    public PageDTO<BillPageVO> query(BillPageQuery billPageQuery) {
        Page<EbBill> page = new Page<>(billPageQuery.getPageNo(), billPageQuery.getPageSize());
        Page<EbBill> ebBillPage = lambdaQuery()
                .eq(StringUtils.isNotBlank(billPageQuery.getStatus()), EbBill::getStatus, billPageQuery.getStatus())
                .like(billPageQuery.getBillId() != null, EbBill::getId, billPageQuery.getBillId())
                .between(billPageQuery.getStartDate() != null && billPageQuery.getEndDate() != null, EbBill::getCreatedAt, billPageQuery.getStartDate(), billPageQuery.getEndDate())
                .page(page);
        if (ebBillPage.getTotal() == 0) {
            return PageDTO.empty(page);
        }
        List<EbBill> records = ebBillPage.getRecords();
        List<BillPageVO> billPageVOS = List.of();
        try {
            billPageVOS = records.stream().map(ebBill -> BeanUtils.copyBean(ebBill, BillPageVO.class)).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("转换异常");
        }
        return PageDTO.of(ebBillPage, billPageVOS);
    }

    @Override
    public UserBillVO detailBill(Long billId) {
        EbBill ebBill = getById(billId);
        if (ObjectUtils.isEmpty(ebBill)) {
            throw new DbException(Constant.BILL_NOT_EXIST);
        }
        UserBillVO userBillVO = BeanUtils.copyBean(ebBill, UserBillVO.class);
        EbUser ebUser = userMapper.selectById(ebBill.getUserId());
        userBillVO.setUsername(ebUser.getUsername());
        userBillVO.setUserType(ebUser.getUserType());
        userBillVO.setMeterId(ebUser.getMeterId());
        return userBillVO;
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
            // 确保 MyBatis 配置 useGeneratedKeys=true, keyProperty="id"
            paymentMapper.insert(ebPayment);
            // 主键回写到账单
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
        AlipayClient client = new DefaultAlipayClient(
                GATEWAY_URL,
                aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(),
                FORMAT,
                CHARSET,
                aliPayConfig.getAlipayPublicKey(),
                SIGN_TYPE
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);

        try {
            // 5. 查询支付宝流水状态
            String status = checkOrderStatus(client, billId);

            switch (status) {
                case "TRADE_CLOSED":
                case "NOT_EXIST":
                    // 6. 生成新二维码
                    AlipayTradePrecreateResponse qr = generateQRCode(client, ebBill);
                    if (qr.isSuccess()) {
                        resp.put("success", true);
                        resp.put("orderNo", billId.toString());
                        resp.put("qrCode", qr.getQrCode());
                        resp.put("expireTime", System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));
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
            ebPayment.setStatus(PaymentType.FAILED.getDesc());
            log.error(err, ex);
        }

        // 8. 持久化支付记录
        paymentMapper.updateById(ebPayment);

        return resp;
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
        String billIdStr = params.get("out_order_no");
        String transactionId = params.get("trade_no");

        // 3. 校验核心参数
        if (billIdStr == null || transactionId == null) {
            log.error("支付宝回调参数缺失: out_order_no={}, trade_no={}", billIdStr, transactionId);
            return "success";
        }
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
                boolean verifyResult = com.alipay.easysdk.factory.Factory.Payment.Common()
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

    /**
     * 查询支付宝订单状态
     */
    private String checkOrderStatus(AlipayClient client, Long billId) throws AlipayApiException {
        AlipayTradeQueryRequest req = new AlipayTradeQueryRequest();
        req.setBizContent("{\"out_trade_no\":\"" + billId + "\"}");
        AlipayTradeQueryResponse rsp = client.execute(req);
        return rsp.isSuccess() ? rsp.getTradeStatus() : "QUERY_FAILED";
    }

    /**
     * 生成支付宝二维码
     */
    private AlipayTradePrecreateResponse generateQRCode(AlipayClient client, EbBill ebBill) throws AlipayApiException {
        AlipayTradePrecreateRequest req = new AlipayTradePrecreateRequest();
        req.setNotifyUrl(aliPayConfig.getNotifyUrl());
        req.setBizContent("{" +
                "\"out_trade_no\":\"" + ebBill.getId() + "\"," +
                "\"total_amount\":\"" + ebBill.getTotalAmount() + "\"," +
                "\"subject\":\"电费支付\"," +
                "\"product_code\":\"FACE_TO_FACE_PAYMENT\"," +
                "\"timeout_express\":\"30m\"," +
                "\"qr_code_timeout_express\":\"5m\"" +
                "}");
        return client.execute(req);
    }
}