package com.electricitybill.config;

import com.electricitybill.config.properties.TTLProperties;
import com.electricitybill.utils.TTLGenerator;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
//配置工具类加载ttProperties
public class TTLConfiguration {

    private final TTLProperties ttlProperties;

    public TTLConfiguration(TTLProperties ttlProperties) {
        this.ttlProperties = ttlProperties;
    }

    @PostConstruct
    public void init() {
        TTLGenerator.initProperties(ttlProperties);
    }
}