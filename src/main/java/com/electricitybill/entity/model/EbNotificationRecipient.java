package com.electricitybill.entity.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("eb_notification_recipient")
public class EbNotificationRecipient implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 通知ID
     */
    private Long notificationId;

    /**
     * 接收对象类型:user/admin
     */
    private String recipientType;

    /**
     * 接收者ID
     */
    private Long recipientId;

    /**
     * 阅读状态:0未读/1已读
     */
    private Integer readStatus;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
