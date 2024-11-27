package com.electricitybill.entity.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalTime;
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
@TableName("eb_rate")
public class EbRate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 费率名称
     */
    private String rateName;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 每度电费价格
     */
    private BigDecimal price;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 峰时价格
     */
    private BigDecimal peakPrice;

    /**
     * 平时价格
     */
    private BigDecimal flatPrice;

    /**
     * 谷时价格
     */
    private BigDecimal valleyPrice;

    /**
     * 状态:0禁用/1启用
     */
    private Integer status;

    /**
     * 生效日期
     */
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    private LocalDate expireDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
