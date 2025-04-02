package com.electricitybill.entity.po;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("eb_electricity_usage")
@ApiModel(value="EbElectricityUsage对象", description="")
public class EbElectricityUsage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用电记录ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "电表编号")
    private String meterId;

    @ApiModelProperty(value = "用电量（度）")
    private BigDecimal usageAmount;

    @ApiModelProperty(value = "费率ID")
    private Long rateId;

    @ApiModelProperty(value = "用电开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "用电结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "用电时段: peak（峰）/flat（平）/valley（谷）")
    @TableField("period_type")
    private String periodType;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;


}
