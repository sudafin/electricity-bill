package com.electricitybill.entity.model;

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
@TableName("eb_electricity_usage")
public class EbElectricityUsage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 电表编号
     */
    private String meterNo;

    /**
     * 用电量
     */
    private BigDecimal usageAmount;

    /**
     * 费率ID
     */
    private Long rateId;

    /**
     * 电费金额
     */
    private BigDecimal feeAmount;

    /**
     * 用电开始时间
     */
    private LocalDateTime startTime;

    /**
     * 用电结束时间
     */
    private LocalDateTime endTime;

    /**
     * 用电时段:peak/flat/valley
     */
    private String timeSegment;

    private LocalDateTime createdAt;


}
