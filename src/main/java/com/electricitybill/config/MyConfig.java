package com.electricitybill.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "eb")  //这是一级前缀
//获取不需要验证的网址
public class MyConfig {

    private String[] noAuthPaths; //二级前缀获得三级数据

}
