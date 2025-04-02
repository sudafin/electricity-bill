package com.electricitybill.entity.po;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author huangdada
 * @since 2025-03-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_bill")
@ApiModel(value="EbBill对象", description="")
public class EbBill implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "账单ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "账单开始日期")
    @TableField("start_date")
    private LocalDate startDate;

    @ApiModelProperty(value = "账单结束日期")
    @TableField("end_date")
    private LocalDate endDate;

    @ApiModelProperty(value = "用电量（度）")
    @TableField("usage_amount")
    private BigDecimal usageAmount;

    @ApiModelProperty(value = "总金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "状态: 未支付/已支付/逾期")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "支付记录ID")
    @TableField("payment_id")
    private Long paymentId;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "支付方式")
    @TableField("payment_method")
    private String paymentMethod;

    @ApiModelProperty(value = "基础电费")
    @TableField("base_electricity_usage")
    private BigDecimal baseElectricityUsage;

    @ApiModelProperty(value = "高峰电费")
    @TableField("hot_electricity_usage")
    private BigDecimal hotElectricityUsage;

    @ApiModelProperty(value = "最晚支付时间")
    @TableField("due_date")
    private LocalDateTime dueDate;

    @ApiModelProperty(value = "一个周期内读表开始的度数")
    @TableField("start_reading")
    private BigDecimal startReading;

    @ApiModelProperty(value = "一个周期内读表现在的度数")
    @TableField("ending_reading")
    private BigDecimal endingReading;

}
