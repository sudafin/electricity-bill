package com.electricitybill.entity.vo.bill;

import com.electricitybill.entity.vo.payment.PaymentDetailVO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BillAdminDetailVO {

    private String username;

    private String userType;

    private String meterId;;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal usageAmount;

    private BigDecimal totalAmount;

    private String status;

    private Long paymentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String paymentMethod;

    private BigDecimal baseElectricityUsage;

    private BigDecimal hotElectricityUsage;

    private LocalDateTime dueDate;

    private BigDecimal startReading; //开始读数

    private BigDecimal endReading;  // 结束读数

    List<PaymentDetailVO> paymentDetailVOList;
}
