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
        String encode1 = passwordEncoder.encode("user001");
        System.out.println(encode1);
        String encode2 = passwordEncoder.encode("user001");
        System.out.println(passwordEncoder.matches(encode2,encode1));
        System.out.println(passwordEncoder.matches("user001",encode1));
    }


}
