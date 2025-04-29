package com.electricitybill;

import com.electricitybill.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

@SpringBootTest
class ElectricityBillApplicationTests {
    @Resource
    private PasswordEncoder passwordEncoder;
    @Test
    void contextLoads() {
        String encode = passwordEncoder.encode("user001");
        System.out.println(encode);
    }


}
