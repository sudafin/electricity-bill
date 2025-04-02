package com.electricitybill.entity.vo.user;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserBillVO {

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
}
