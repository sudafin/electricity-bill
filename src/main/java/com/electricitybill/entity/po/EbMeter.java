package com.electricitybill.entity.po;

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
 * 
 * </p>
 *
 * @author huangdada
 * @since 2025-03-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_meter")
@ApiModel(value="EbMeter对象", description="")
public class EbMeter implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "电表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "电表型号")
    private String model;

    @ApiModelProperty(value = "安装日期")
    private LocalDate installDate;

    @ApiModelProperty(value = "状态: 正常/故障/停用")
    private String status;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "最近读数时间")
    private LocalDateTime lastMeterReadingDate;

}
