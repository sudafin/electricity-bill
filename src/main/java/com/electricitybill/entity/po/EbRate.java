package com.electricitybill.entity.po;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 电价费率表
 * </p>
 *
 * @author huangdada
 * @since 2025-03-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_rate")
@ApiModel(value="EbRate对象", description="电价费率表")
public class EbRate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "费率ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户类型")
    @TableField("user_type")
    private String userType;

    @ApiModelProperty(value = "峰时电价(元/度)")
    @TableField("peak_price")
    private BigDecimal peakPrice;

    @ApiModelProperty(value = "平时电价(元/度)")
    @TableField("flat_price")
    private BigDecimal flatPrice;

    @ApiModelProperty(value = "谷时电价(元/度)")
    @TableField("valley_price")
    private BigDecimal valleyPrice;

    @ApiModelProperty(value = "夏季尖峰电价(元/度)")
    @TableField("summer_peak_price")
    private BigDecimal summerPeakPrice;

    @ApiModelProperty(value = "峰时段开始时间")
    @TableField("peak_start")
    private LocalTime peakStart;

    @ApiModelProperty(value = "峰时段结束时间")
    @TableField("peak_end")
    private LocalTime peakEnd;

    @ApiModelProperty(value = "谷时段开始时间")
    @TableField("valley_start")
    private LocalTime valleyStart;

    @ApiModelProperty(value = "谷时段结束时间")
    @TableField("valley_end")
    private LocalTime valleyEnd;

    @ApiModelProperty(value = "夏季时段(如'06-01至08-31')")
    @TableField("summer_period")
    private String summerPeriod;

    @ApiModelProperty(value = "状态: 0禁用/1启用")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "生效日期")
    @TableField("effective_date")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "失效日期")
    @TableField("expire_date")
    private LocalDate expireDate;

    @ApiModelProperty(value = "创建人")
    @TableField("created_by")
    private String createdBy;

    @ApiModelProperty(value = "更新人")
    @TableField("updated_by")
    private String updatedBy;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "优惠率")
    private BigDecimal discount;

}
