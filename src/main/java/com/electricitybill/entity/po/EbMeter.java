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

/**
 * <p>
 * 
 * </p>
 *
 * @author huangdada
 * @since 2025-03-26
 */
@TableName("eb_meter")
@ApiModel(value="EbMeter对象", description="")
@Data
public class EbMeter implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "电表ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @ApiModelProperty(value = "电表型号")
    @TableField("model")
    private String model;

    @ApiModelProperty(value = "安装时间")
    @TableField("install_date")
    private LocalDateTime installDate;

    @ApiModelProperty(value = "状态: 正常/故障/停用")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "周期最后一次抄表时间")
    @TableField("last_meter_reading_date")
    private LocalDateTime lastMeterReadingDate;

    @ApiModelProperty(value = "周期内开始读表的时间")
    @TableField("start_meter_reading_date")
    private LocalDateTime startMeterReadingDate;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "一个周期内读表开始的度数，周期通常为为一个月")
    @TableField("start_reading")
    private BigDecimal startReading;

    @ApiModelProperty(value = "一个周期内读表现在的度数")
    @TableField("ending_reading")
    private BigDecimal endingReading;

    @ApiModelProperty(value = "检查电表的id")
    @TableField("inspection_id")
    private Long inspectionId;


    @ApiModelProperty(value = "安装地点")
    @TableField("install_place")
    private String installPlace;

    @ApiModelProperty(value = "是否有效")
    private Integer validType;
}
