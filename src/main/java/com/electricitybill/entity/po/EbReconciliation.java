package com.electricitybill.entity.po;

import java.math.BigDecimal;
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
 * @since 2025-03-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_reconciliation")
@ApiModel(value="EbReconciliation对象", description="")
public class EbReconciliation implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "对账单ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "开始日期")
    private LocalDateTime startDate;

    @ApiModelProperty(value = "结束日期")
    private LocalDateTime endDate;

    @ApiModelProperty(value = "总用电量")
    private BigDecimal totalUsage;

    @ApiModelProperty(value = "总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "状态: pending（待处理）/completed（已完成）")
    private String status;

    @ApiModelProperty(value = "支付状态: unpaid（未支付）/paid（已支付）")
    private String paymentStatus;

    @ApiModelProperty(value = "审批人ID")
    private Long approverId;

    @ApiModelProperty(value = "审批时间")
    private LocalDateTime approvalTime;

    @ApiModelProperty(value = "审批意见")
    private String comment;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "支付记录ID")
    private Long paymentId;


}
