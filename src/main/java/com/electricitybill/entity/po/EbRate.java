package com.electricitybill.entity.po;

import java.math.BigDecimal;
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
 * 
 * </p>
 *
 * @author huangdada
 * @since 2025-03-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_rate")
@ApiModel(value="EbRate对象", description="")
public class EbRate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "费率ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "费率名称")
    private String rateName;

    @ApiModelProperty(value = "用户类型: 居民用户/商业用户")
    private String userType;

    @ApiModelProperty(value = "每度电费价格")
    private BigDecimal price;

    @ApiModelProperty(value = "开始时间")
    private LocalTime startTime;

    @ApiModelProperty(value = "结束时间")
    private LocalTime endTime;

    @ApiModelProperty(value = "峰时价格")
    private BigDecimal peakPrice;

    @ApiModelProperty(value = "平时价格")
    private BigDecimal flatPrice;

    @ApiModelProperty(value = "谷时价格")
    private BigDecimal valleyPrice;

    @ApiModelProperty(value = "状态: 0禁用/1启用")
    private Integer status;

    @ApiModelProperty(value = "生效日期")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "失效日期")
    private LocalDate expireDate;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedAt;


}
