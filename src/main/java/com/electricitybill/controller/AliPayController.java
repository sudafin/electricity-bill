package com.electricitybill.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.electricitybill.config.AliPayConfig;
import com.electricitybill.config.WebSocketHandler;
import com.electricitybill.entity.dto.AliPay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("alipay")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class AliPayController {

    @Resource
    private AliPayConfig aliPayConfig;

    private static final String GATEWAY_URL ="https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT ="JSON";
    private static final String CHARSET ="utf-8";
    private static final String SIGN_TYPE ="RSA2";

    @GetMapping("/pay")
    public Map<String, Object> pay(AliPay aliPay) {
        Map<String, Object> responseMap = new HashMap<>();
        AlipayClient alipayClient = new DefaultAlipayClient(
                GATEWAY_URL,
                aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(),
                FORMAT,
                CHARSET,
                aliPayConfig.getAlipayPublicKey(),
                SIGN_TYPE
        );

        try {
            // 1. 先查询订单状态（防止重复生成）
            String orderStatus = checkOrderStatus(alipayClient, aliPay.getTraceNo());

            // 2. 根据订单状态决定是否生成新二维码
            if ("TRADE_CLOSED".equals(orderStatus) || "NOT_EXIST".equals(orderStatus)) {
                // 订单已关闭或不存在，可以生成新二维码
                AlipayTradePrecreateResponse response = generateQRCode(alipayClient, aliPay);

                if (response.isSuccess()) {
                    responseMap.put("qrCode", response.getQrCode());
                    responseMap.put("expireTime", System.currentTimeMillis() + 5 * 60 * 1000); // 5分钟后过期
                    responseMap.put("orderNo", aliPay.getTraceNo());
                    log.info("支付宝二维码生成成功 - 订单号: {}, 二维码: {}", aliPay.getTraceNo(), response.getQrCode());
                } else {
                    handleError(responseMap, "支付宝二维码生成失败: " + response.getSubMsg());
                }
            } else if ("WAIT_BUYER_PAY".equals(orderStatus)) {
                // 订单仍在等待支付，返回提示信息
                responseMap.put("message", "订单仍在等待支付，请继续使用原二维码");
                responseMap.put("orderStatus", orderStatus);
            } else {
                handleError(responseMap, "当前订单状态不支持重新生成二维码: " + orderStatus);
            }
        } catch (AlipayApiException e) {
            handleError(responseMap, "支付宝接口调用异常: " + e.getMessage());
            log.error("支付宝接口调用异常 - 订单号: {}", aliPay.getTraceNo(), e);
        }

        return responseMap;
    }

    // 查询订单状态
    private String checkOrderStatus(AlipayClient alipayClient, String outTradeNo) throws AlipayApiException {
        AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
        queryRequest.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\"}");

        AlipayTradeQueryResponse response = alipayClient.execute(queryRequest);
        return response.isSuccess() ? response.getTradeStatus() : "QUERY_FAILED";
    }

    // 生成二维码
    private AlipayTradePrecreateResponse generateQRCode(AlipayClient alipayClient, AliPay aliPay) throws AlipayApiException {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + aliPay.getTraceNo() + "\"," +
                "\"total_amount\":\"" + aliPay.getTotalAmount() + "\"," +
                "\"subject\":\"" + aliPay.getSubject() + "\"," +
                "\"product_code\":\"FACE_TO_FACE_PAYMENT\"," +
                "\"timeout_express\":\"30m\"," +
                "\"qr_code_timeout_express\":\"5m\"" +
                "}");
        return alipayClient.execute(request);
    }

    // 错误处理
    private void handleError(Map<String, Object> responseMap, String errorMsg) {
        responseMap.put("success", false);
        responseMap.put("error", errorMsg);
        log.error(errorMsg);
    }
    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
        try {
            String tradeStatus = request.getParameter("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus)) {
                log.warn("支付宝回调状态非成功: {}", tradeStatus);
                return "success";
            }

            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                String value = request.getParameter(name);
                if (value != null && !value.isEmpty()) {
                    params.put(name, value);
                }
            }

            String tradeNo = params.get("out_trade_no");
            String alipayTradeNo = params.get("trade_no");
            if (tradeNo == null || alipayTradeNo == null) {
                log.error("支付宝回调参数缺失: out_trade_no={}, trade_no={}", tradeNo, alipayTradeNo);
                return "success";
            }

            boolean verifyResult = com.alipay.easysdk.factory.Factory.Payment.Common().verifyNotify(params);
            if (!verifyResult) {
                log.error("支付宝验签失败: tradeNo={}", tradeNo);
                return "success";
            }

            System.out.println("交易名称: " + params.get("subject"));
            System.out.println("交易状态: " + params.get("trade_status"));
            System.out.println("支付宝交易凭证号: " + params.get("trade_no"));
            System.out.println("商户订单号: " + params.get("out_trade_no"));
            System.out.println("交易金额: " + params.get("total_amount"));
            System.out.println("买家在支付宝唯一id: " + params.get("buyer_id"));
            System.out.println("买家付款时间: " + params.get("gmt_payment"));
            System.out.println("买家付款金额: " + params.get("buyer_pay_amount"));
            // 调用 WebSocket 推送支付成功消息给前端
            WebSocketHandler.sendMessageToAll("支付成功，订单号：" + tradeNo);
            return "success";
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
            return "success";
        }
    }
}