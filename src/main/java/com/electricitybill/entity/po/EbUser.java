package com.electricitybill.entity.po;

import java.math.BigDecimal;
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
@TableName("eb_user")
public class EbUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 地址
     */
    private String address;

    /**
     * 电表编号
     */
    private String meterNo;

    /**
     * 用户类型:居民用户/商业用户
     */
    private String userType;

    /**
     * 账号状态:正常/欠费/停用
     */
    private String accountStatus;

    /**
     * 电费余额
     */
    private BigDecimal balance;

    /**
     * 用电量
     */
    private BigDecimal electricityUsage;

    /**
     * 最近缴费时间
     */
    private LocalDateTime lastPaymentDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
