package com.electricitybill.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

@SpringBootTest
class IEbAdminServiceTest {
    @Resource
     private PasswordEncoder passwordEncoder;
    @Test
    void login() {
        System.out.println(passwordEncoder.matches("admin123", "$2a$10$4FUHuxcpYOIomnc3CIJfcOnCYk0P0corhysvagSvIqy234vm3hj9u"));
    }
}