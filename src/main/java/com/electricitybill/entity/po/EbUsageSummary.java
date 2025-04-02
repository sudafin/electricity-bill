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
 * 每日用电量及电费汇总表
 * </p>
 *
 * @author huangdada
 * @since 2025-03-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_daily_usage_summary")
@ApiModel(value="EbDailyUsageSummary对象", description="每日用电量及电费汇总表")
public class EbUsageSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "汇总ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "电表编号")
    @TableField("meter_id")
    private String meterId;



    @ApiModelProperty(value = "峰时段用电量(度)")
    @TableField("peak_usage")
    private BigDecimal peakUsage;

    @ApiModelProperty(value = "平时段用电量(度)")
    @TableField("flat_usage")
    private BigDecimal flatUsage;

    @ApiModelProperty(value = "谷时段用电量(度)")
    @TableField("valley_usage")
    private BigDecimal valleyUsage;

    @ApiModelProperty(value = "总用电量(度)")
    @TableField("total_usage")
    private BigDecimal totalUsage;

    @ApiModelProperty(value = "峰时段电费(元)")
    @TableField("peak_cost")
    private BigDecimal peakCost;

    @ApiModelProperty(value = "平时段电费(元)")
    @TableField("flat_cost")
    private BigDecimal flatCost;

    @ApiModelProperty(value = "谷时段电费(元)")
    @TableField("valley_cost")
    private BigDecimal valleyCost;

    @ApiModelProperty(value = "总电费(元)")
    @TableField("total_cost")
    private BigDecimal totalCost;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "汇总日期始")
    @TableField("summary_date_start")
    private LocalDateTime summaryDateStart;

    @ApiModelProperty(value = "汇总日期末")
    @TableField("summary_date_end")
    private LocalDateTime summaryDateEnd;

    @ApiModelProperty(value = "汇总日期类型")
    @TableField("date_type")
    private String dateType;

}
