package com.electricitybill;

import com.electricitybill.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

class ElectricityBillApplicationTests {

    @Test
    void contextLoads() {
        String a1 = "a";
        String a2 = null;
        String a3 = null;
        System.out.println(!StringUtils.isAllBlank(a1, a2, a3));
    }

}
