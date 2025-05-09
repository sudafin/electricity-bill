package com.electricitybill.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class AlipayClientConfig { 
    
    @Resource
    private AliPayConfig aliPayConfig;
    
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
            "https://openapi-sandbox.dl.alipaydev.com/gateway.do", // 沙箱地址
            aliPayConfig.getAppId(),
            aliPayConfig.getAppPrivateKey(),
            "JSON",
            "utf-8",
            aliPayConfig.getAlipayPublicKey(),
            "RSA2"
        );
    }
}
