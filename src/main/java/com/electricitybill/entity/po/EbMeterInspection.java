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

/**
 * <p>
 * 
 * </p>
 *
 * @author huangdada
 * @since 2025-03-26
 */
@TableName("eb_meter_inspection")
@ApiModel(value="EbMeterInspection对象", description="")
@Data
public class EbMeterInspection implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "检测记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "电表编号")
    @TableField("meter_id")
    private String meterId;

    @ApiModelProperty(value = "检测类型: routine（常规检查）/fault（故障检修）/calibration（校准）")
    @TableField("inspection_type")
    private String inspectionType;

    @ApiModelProperty(value = "检测结果: normal（正常）/fault（故障）/fixed（已修复）")
    @TableField("inspection_result")
    private String inspectionResult;

    @ApiModelProperty(value = "故障描述")
    @TableField("fault_description")
    private String faultDescription;

    @ApiModelProperty(value = "解决方案")
    @TableField("solution")
    private String solution;

    @ApiModelProperty(value = "检测人员姓名")
    @TableField("inspector_name")
    private String inspectorName;

    @ApiModelProperty(value = "检测时间")
    @TableField("inspection_time")
    private LocalDateTime inspectionTime;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty(value = "状态: pending（待处理）/processing（处理中）/completed（已完成）")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
