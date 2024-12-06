package com.electricitybill.entity.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("eb_payment")
public class EbPayment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    /**
     * 操作员ID
     */
    private Long operatorId;
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单号
     */
    private Long reconciliationId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付流水号
     */
    private String transactionNo;

    /**
     * 支付状态:pending/success/failed
     */
    private String status;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 退款状态:none/processing/success/failed
     */
    private String refundStatus;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    /**
     * 备注
     */
    private String remark;


}
