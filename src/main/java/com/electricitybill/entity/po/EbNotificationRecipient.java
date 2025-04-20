package com.electricitybill.entity.po;

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
@TableName("eb_notification_recipient")
@ApiModel(value="EbNotificationRecipient对象", description="")
public class EbNotificationRecipient implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "接收记录ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "通知ID")
    private Long notificationId;

    @ApiModelProperty(value = "接收者类型: user（用户）/admin（管理员）")
    private String recipientType;

    @ApiModelProperty(value = "接收者ID")
    private Long recipientId;


    @ApiModelProperty(value = "阅读状态: 0未读/1已读")
    private Integer readStatus;

    @ApiModelProperty(value = "阅读时间")
    private LocalDateTime readTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedAt;


}
