package com.electricitybill.entity.vo.bill;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BillUserDetailVO {
    private String billPeriod;

    private String userType;

    private String meterId;;

    private BigDecimal usage;

    private BigDecimal amount;

    private String status;

    private LocalDateTime billDate;

    private LocalDateTime paymentDate;

    private String paymentMethod;

    private LocalDateTime dueDate;

    private BigDecimal startReading; //开始读数

    private BigDecimal endReading;  // 结束读数

    private List<JSONObject> billDetails;
}
