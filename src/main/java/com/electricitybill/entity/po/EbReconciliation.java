package com.electricitybill.entity.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_reconciliation")
public class EbReconciliation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 对账单号
     */
    private Long reconciliationNo;

    private Long paymentId;
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 总用电量
     */
    private BigDecimal totalUsage;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态:pending/completed
     */
    private String status;

    /**
     * 支付状态:unpaid/paid
     */
    private String paymentStatus;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    private String comment;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
