package com.electricitybill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//如果需要借用外部的类作为bean来注入，需要使用@Configuration注解,如果只是使用其他中的方法则使用util进行包装就行
@Configuration
public class BCryptConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
