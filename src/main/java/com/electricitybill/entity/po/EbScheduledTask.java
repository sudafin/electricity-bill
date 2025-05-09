package com.electricitybill.entity.po;

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
 * @since 2025-03-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_scheduled_task")
@ApiModel(value="EbScheduledTask对象", description="")
public class EbScheduledTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户id")
    @TableField("user_ids")
    private String userIds;

    @ApiModelProperty(value = "任务名称")
    @TableField("task_name")
    private String taskName;

    @ApiModelProperty(value = "任务类型:用户任务，系统任务")
    @TableField("task_type")
    private String taskType;


    @ApiModelProperty(value = "任务描述")
    @TableField("task_desc")
    private String taskDesc;

    @ApiModelProperty(value = "Cron表达式")
    @TableField("cron_expression")
    private String cronExpression;

    @ApiModelProperty(value = "状态: 0停用/1启用")
    @TableField("task_status")
    private Integer taskStatus;

    @ApiModelProperty(value = "创建人ID")
    @TableField("created_by")
    private Long createdBy;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;


}
