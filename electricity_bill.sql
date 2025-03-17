# 删除数据库
DROP DATABASE IF EXISTS `electricity_bill`;
CREATE DATABASE `electricity_bill`;
USE `electricity_bill`;
-- 管理员表
CREATE TABLE `eb_admin`
(
    `id`              bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `account`         varchar(50)  NOT NULL COMMENT '账号',
    `password`        varchar(100) NOT NULL COMMENT '密码',
    `email`           varchar(100)          DEFAULT NULL COMMENT '邮箱',
    `phone`           varchar(20)           DEFAULT NULL COMMENT '电话',
    `role_id`         bigint(20)   NOT NULL COMMENT '角色ID',
    `status`          tinyint(4)            DEFAULT '1' COMMENT '状态: 0禁用/1启用',
    `last_login_time` datetime              DEFAULT NULL COMMENT '最后登录时间',
    `created_at`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `role_id` (`role_id`),
    KEY `idx_account` (`account`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1865685594955948035
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_admin` (`account`, `password`, `email`, `phone`, `role_id`, `status`, `last_login_time`, `created_at`,
                        `updated_at`)
VALUES ('admin1', 'hashed_pass1', 'admin1@example.com', '13812345678', 1, 1, '2025-03-14 10:00:00',
        '2025-03-01 09:00:00', '2025-03-14 10:00:00'),
       ('admin2', 'hashed_pass2', 'admin2@example.com', '13987654321', 2, 1, '2025-03-15 08:30:00',
        '2025-03-01 10:00:00', '2025-03-15 08:30:00'),
       ('admin3', 'hashed_pass3', 'admin3@example.com', '13711112222', 1, 1, '2025-03-13 09:00:00',
        '2025-03-02 09:00:00', '2025-03-13 09:00:00'),
       ('admin4', 'hashed_pass4', 'admin4@example.com', '13633334444', 2, 1, '2025-03-14 15:00:00',
        '2025-03-02 10:00:00', '2025-03-14 15:00:00'),
       ('admin5', 'hashed_pass5', 'admin5@example.com', '13555556666', 1, 0, '2025-03-12 14:00:00',
        '2025-03-03 09:00:00', '2025-03-12 14:00:00'),
       ('admin6', 'hashed_pass6', 'admin6@example.com', '13477778888', 2, 1, '2025-03-15 10:00:00',
        '2025-03-03 10:00:00', '2025-03-15 10:00:00'),
       ('admin7', 'hashed_pass7', 'admin7@example.com', '13399990000', 1, 1, '2025-03-14 11:00:00',
        '2025-03-04 09:00:00', '2025-03-14 11:00:00'),
       ('admin8', 'hashed_pass8', 'admin8@example.com', '13222223333', 2, 1, '2025-03-13 13:00:00',
        '2025-03-04 10:00:00', '2025-03-13 13:00:00'),
       ('admin9', 'hashed_pass9', 'admin9@example.com', '13144445555', 1, 1, '2025-03-15 09:00:00',
        '2025-03-05 09:00:00', '2025-03-15 09:00:00'),
       ('admin10', 'hashed_pass10', 'admin10@example.com', '13066667777', 2, 1, '2025-03-14 12:00:00',
        '2025-03-05 10:00:00', '2025-03-14 12:00:00');

-- 用电记录表
CREATE TABLE `eb_electricity_usage`
(
    `id`           bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '用电记录ID',
    `user_id`      bigint(20)     NOT NULL COMMENT '用户ID',
    `meter_no`     varchar(50)    NOT NULL COMMENT '电表编号',
    `usage_amount` decimal(10, 2) NOT NULL COMMENT '用电量（度）',
    `rate_id`      bigint(20)     NOT NULL COMMENT '费率ID',
    `fee_amount`   decimal(10, 2) NOT NULL COMMENT '电费金额',
    `start_time`   datetime       NOT NULL COMMENT '用电开始时间',
    `end_time`     datetime       NOT NULL COMMENT '用电结束时间',
    `time_segment` varchar(10)    NOT NULL COMMENT '用电时段: peak（峰）/flat（平）/valley（谷）',
    `created_at`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `rate_id` (`rate_id`),
    KEY `idx_user_usage` (`user_id`, `start_time`),
    KEY `idx_meter_no` (`meter_no`),
    KEY `idx_time_segment` (`time_segment`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 21
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_electricity_usage` (`user_id`, `meter_no`, `usage_amount`, `rate_id`, `fee_amount`, `start_time`,
                                    `end_time`, `time_segment`, `created_at`)
VALUES (1, 'M001', 100.50, 1, 60.30, '2025-03-01 08:00:00', '2025-03-01 22:00:00', 'peak', '2025-03-02 00:00:00'),
       (2, 'M002', 75.30, 2, 60.24, '2025-03-01 00:00:00', '2025-03-01 23:59:59', 'flat', '2025-03-02 00:00:00'),
       (1, 'M001', 60.00, 1, 24.00, '2025-03-02 00:00:00', '2025-03-02 07:59:59', 'valley', '2025-03-03 00:00:00'),
       (3, 'M003', 120.75, 1, 72.45, '2025-03-02 08:00:00', '2025-03-02 22:00:00', 'peak', '2025-03-03 00:00:00'),
       (4, 'M004', 90.20, 2, 72.16, '2025-03-03 00:00:00', '2025-03-03 23:59:59', 'flat', '2025-03-04 00:00:00'),
       (5, 'M005', 50.00, 1, 20.00, '2025-03-03 00:00:00', '2025-03-03 07:59:59', 'valley', '2025-03-04 00:00:00'),
       (6, 'M006', 110.30, 1, 66.18, '2025-03-04 08:00:00', '2025-03-04 22:00:00', 'peak', '2025-03-05 00:00:00'),
       (7, 'M007', 85.60, 2, 68.48, '2025-03-04 00:00:00', '2025-03-04 23:59:59', 'flat', '2025-03-05 00:00:00'),
       (8, 'M008', 70.40, 1, 28.16, '2025-03-05 00:00:00', '2025-03-05 07:59:59', 'valley', '2025-03-06 00:00:00'),
       (9, 'M009', 130.90, 1, 78.54, '2025-03-05 08:00:00', '2025-03-05 22:00:00', 'peak', '2025-03-06 00:00:00');

-- 电表信息表
CREATE TABLE `eb_meter`
(
    `id`           bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '电表ID',
    `meter_no`     varchar(50) NOT NULL COMMENT '电表编号',
    `model`        varchar(50)          DEFAULT NULL COMMENT '电表型号',
    `install_date` date                 DEFAULT NULL COMMENT '安装日期',
    `status`       varchar(20) NOT NULL DEFAULT '正常' COMMENT '状态: 正常/故障/停用',
    `user_id`      bigint(20)           DEFAULT NULL COMMENT '用户ID',
    `created_at`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `meter_no` (`meter_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
            
INSERT INTO `eb_meter` (`meter_no`, `model`, `install_date`, `status`, `user_id`, `created_at`, `updated_at`)
VALUES ('M001', 'SmartMeterX1', '2024-12-01', '正常', 1, '2024-12-01 10:00:00', '2024-12-01 10:00:00'),
       ('M002', 'SmartMeterX2', '2025-01-15', '正常', 2, '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
       ('M003', 'SmartMeterX1', '2025-02-01', '正常', 3, '2025-02-01 11:00:00', '2025-02-01 11:00:00'),
       ('M004', 'SmartMeterX2', '2025-02-15', '正常', 4, '2025-02-15 10:00:00', '2025-02-15 10:00:00'),
       ('M005', 'SmartMeterX1', '2025-03-01', '正常', 5, '2025-03-01 09:00:00', '2025-03-01 09:00:00'),
       ('M006', 'SmartMeterX2', '2025-03-02', '正常', 6, '2025-03-02 10:00:00', '2025-03-02 10:00:00'),
       ('M007', 'SmartMeterX1', '2025-03-03', '正常', 7, '2025-03-03 11:00:00', '2025-03-03 11:00:00'),
       ('M008', 'SmartMeterX2', '2025-03-04', '正常', 8, '2025-03-04 09:00:00', '2025-03-04 09:00:00'),
       ('M009', 'SmartMeterX1', '2025-03-05', '正常', 9, '2025-03-05 10:00:00', '2025-03-05 10:00:00'),
       ('M010', 'SmartMeterX2', '2025-03-06', '故障', NULL, '2025-03-06 11:00:00', '2025-03-15 14:00:00');

-- 通知表
CREATE TABLE `eb_notification`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `title`       varchar(100) NOT NULL COMMENT '标题',
    `content`     text         NOT NULL COMMENT '内容',
    `type`        varchar(20)  NOT NULL COMMENT '类型: feedback（反馈）/billing（账单）/internal（内部）',
    `sender_type` varchar(20)  NOT NULL COMMENT '发送者类型: system（系统）/admin（管理员）',
    `sender_id`   bigint(20)            DEFAULT NULL COMMENT '发送人ID',
    `created_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `sender_id` (`sender_id`),
    KEY `idx_type` (`type`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1898746378252201985
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_notification` (`title`, `content`, `type`, `sender_type`, `sender_id`, `created_at`, `updated_at`)
VALUES ('新反馈待处理', '用户1提交了反馈，请处理。', 'feedback', 'system', NULL, '2025-03-10 14:00:00',
        '2025-03-10 14:00:00'),
       ('账单提醒', '用户2的3月账单已生成。', 'billing', 'system', NULL, '2025-03-05 09:00:00', '2025-03-05 09:00:00'),
       ('任务分配', '请管理员2审核用户1的对账单。', 'internal', 'admin', 1, '2025-03-12 10:00:00',
        '2025-03-12 10:00:00'),
       ('账单提醒', '用户3的3月账单已生成。', 'billing', 'system', NULL, '2025-03-06 09:00:00', '2025-03-06 09:00:00'),
       ('新反馈待处理', '用户4提交了反馈，请处理。', 'feedback', 'system', NULL, '2025-03-11 15:00:00',
        '2025-03-11 15:00:00'),
       ('内部通知', '请管理员3处理用户5的支付问题。', 'internal', 'admin', 2, '2025-03-13 11:00:00',
        '2025-03-13 11:00:00'),
       ('账单提醒', '用户6的3月账单已生成。', 'billing', 'system', NULL, '2025-03-07 09:00:00', '2025-03-07 09:00:00'),
       ('新反馈待处理', '用户7提交了反馈，请处理。', 'feedback', 'system', NULL, '2025-03-14 16:00:00',
        '2025-03-14 16:00:00'),
       ('任务分配', '请管理员4审核用户8的对账单。', 'internal', 'admin', 1, '2025-03-15 10:00:00',
        '2025-03-15 10:00:00'),
       ('账单提醒', '用户9的3月账单已生成。', 'billing', 'system', NULL, '2025-03-08 09:00:00', '2025-03-08 09:00:00');

-- 通知接收表
CREATE TABLE `eb_notification_recipient`
(
    `id`              bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '接收记录ID',
    `notification_id` bigint(20)  NOT NULL COMMENT '通知ID',
    `recipient_type`  varchar(20) NOT NULL COMMENT '接收者类型: user（用户）/admin（管理员）',
    `recipient_id`    bigint(20)  NOT NULL COMMENT '接收者ID',
    `priority`        tinyint(4)  NOT NULL DEFAULT '1' COMMENT '优先级: 1普通/2重要/3紧急',
    `read_status`     tinyint(4)  NOT NULL DEFAULT '0' COMMENT '阅读状态: 0未读/1已读',
    `read_time`       datetime             DEFAULT NULL COMMENT '阅读时间',
    `created_at`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_notification_id` (`notification_id`),
    KEY `idx_recipient` (`recipient_type`, `recipient_id`),
    KEY `idx_read_status_time` (`read_status`, `read_time`),
    KEY `idx_priority` (`priority`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 21
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_notification_recipient` (`notification_id`, `recipient_type`, `recipient_id`, `priority`, `read_status`,
                                         `read_time`, `created_at`, `updated_at`)
VALUES (1, 'admin', 1, 2, 0, NULL, '2025-03-10 14:00:00', '2025-03-10 14:00:00'),
       (2, 'user', 2, 1, 1, '2025-03-05 10:00:00', '2025-03-05 09:00:00', '2025-03-05 10:00:00'),
       (3, 'admin', 2, 2, 0, NULL, '2025-03-12 10:00:00', '2025-03-12 10:00:00'),
       (4, 'user', 3, 1, 1, '2025-03-06 10:00:00', '2025-03-06 09:00:00', '2025-03-06 10:00:00'),
       (5, 'admin', 3, 2, 0, NULL, '2025-03-11 15:00:00', '2025-03-11 15:00:00'),
       (6, 'admin', 3, 2, 1, '2025-03-13 12:00:00', '2025-03-13 11:00:00', '2025-03-13 12:00:00'),
       (7, 'user', 6, 1, 0, NULL, '2025-03-07 09:00:00', '2025-03-07 09:00:00'),
       (8, 'admin', 4, 2, 0, NULL, '2025-03-14 16:00:00', '2025-03-14 16:00:00'),
       (9, 'admin', 4, 2, 0, NULL, '2025-03-15 10:00:00', '2025-03-15 10:00:00'),
       (10, 'user', 9, 1, 1, '2025-03-08 10:00:00', '2025-03-08 09:00:00', '2025-03-08 10:00:00');

-- 支付记录表
CREATE TABLE `eb_payment`
(
    `id`                bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
    `user_id`           bigint(20)     NOT NULL COMMENT '用户ID',
    `amount`            decimal(10, 2) NOT NULL COMMENT '支付金额',
    `payment_method`    varchar(20)    NOT NULL COMMENT '支付方式',
    `transaction_no`    varchar(100)            DEFAULT NULL COMMENT '支付流水号',
    `status`            varchar(20)    NOT NULL COMMENT '支付状态: pending（待支付）/success（成功）/failed（失败）',
    `failure_reason`    varchar(255)            DEFAULT NULL COMMENT '失败原因',
    `payment_time`      datetime                DEFAULT NULL COMMENT '支付时间',
    `refund_status`     varchar(20)             DEFAULT 'none' COMMENT '退款状态: none（无）/processing（处理中）/success（成功）/failed（失败）',
    `refund_amount`     decimal(10, 2)          DEFAULT NULL COMMENT '退款金额',
    `refund_time`       datetime                DEFAULT NULL COMMENT '退款时间',
    `created_at`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`            varchar(255)            DEFAULT NULL COMMENT '备注',
    `operator_id`       bigint(20)     NOT NULL COMMENT '操作人ID',
    `reconciliation_id` bigint(20)              DEFAULT NULL COMMENT '对账单ID',
    PRIMARY KEY (`id`),
    KEY `idx_user_payment` (`user_id`, `payment_time`),
    KEY `idx_status` (`status`),
    KEY `idx_refund_status` (`refund_status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1898745682408779778
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_payment` (`user_id`, `amount`, `payment_method`, `transaction_no`, `status`, `failure_reason`,
                          `payment_time`, `refund_status`, `refund_amount`, `refund_time`, `created_at`, `updated_at`,
                          `remark`, `operator_id`, `reconciliation_id`)
VALUES (1, 60.30, '微信', 'TXN001', 'success', NULL, '2025-03-06 15:00:00', 'none', NULL, NULL, '2025-03-06 14:00:00',
        '2025-03-06 15:00:00', '3月电费', 1, 1),
       (2, 60.24, '支付宝', 'TXN002', 'success', NULL, '2025-03-07 09:30:00', 'none', NULL, NULL, '2025-03-07 09:00:00',
        '2025-03-07 09:30:00', '3月电费', 2, 2),
       (3, 72.45, '银行卡', 'TXN003', 'success', NULL, '2025-03-08 10:00:00', 'none', NULL, NULL, '2025-03-08 09:00:00',
        '2025-03-08 10:00:00', '3月电费', 3, 3),
       (4, 72.16, '微信', 'TXN004', 'success', NULL, '2025-03-09 11:00:00', 'none', NULL, NULL, '2025-03-09 10:00:00',
        '2025-03-09 11:00:00', '3月电费', 4, 4),
       (5, 20.00, '支付宝', 'TXN005', 'pending', NULL, NULL, 'none', NULL, NULL, '2025-03-10 09:00:00',
        '2025-03-10 09:00:00', '待支付', 5, NULL),
       (6, 66.18, '银行卡', 'TXN006', 'success', NULL, '2025-03-11 12:00:00', 'none', NULL, NULL, '2025-03-11 11:00:00',
        '2025-03-11 12:00:00', '3月电费', 6, 6),
       (7, 68.48, '微信', 'TXN007', 'success', NULL, '2025-03-12 13:00:00', 'none', NULL, NULL, '2025-03-12 12:00:00',
        '2025-03-12 13:00:00', '3月电费', 7, 7),
       (8, 28.16, '支付宝', 'TXN008', 'pending', NULL, NULL, 'none', NULL, NULL, '2025-03-13 09:00:00',
        '2025-03-13 09:00:00', '待支付', 8, NULL),
       (9, 78.54, '银行卡', 'TXN009', 'success', NULL, '2025-03-14 14:00:00', 'none', NULL, NULL, '2025-03-14 13:00:00',
        '2025-03-14 14:00:00', '3月电费', 9, 9),
       (1, 24.00, '微信', 'TXN010', 'success', NULL, '2025-03-15 15:00:00', 'none', NULL, NULL, '2025-03-15 14:00:00',
        '2025-03-15 15:00:00', '补充支付', 1, 1);

-- 权限表
CREATE TABLE `eb_permission`
(
    `id`              bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `permission_name` varchar(50) NOT NULL COMMENT '权限名称',
    `permission_code` varchar(50) NOT NULL COMMENT '权限编码',
    `permission_type` varchar(20) NOT NULL COMMENT '权限类型: menu（菜单）/action（操作）',
    `parent_id`       bigint(20)           DEFAULT NULL COMMENT '父权限ID',
    `created_at`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_permission_code` (`permission_code`),
    KEY `idx_permission_type` (`permission_type`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 40
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_permission` (`permission_name`, `permission_code`, `permission_type`, `parent_id`, `created_at`,
                             `updated_at`)
VALUES ('用户管理', 'user:manage', 'menu', NULL, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('查看用户', 'user:view', 'action', 1, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('编辑用户', 'user:edit', 'action', 1, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('账单管理', 'bill:manage', 'menu', NULL, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('查看账单', 'bill:view', 'action', 4, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('支付管理', 'payment:manage', 'menu', NULL, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('审核支付', 'payment:audit', 'action', 6, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('通知管理', 'notification:manage', 'menu', NULL, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('发送通知', 'notification:send', 'action', 8, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('日志查看', 'log:view', 'menu', NULL, '2025-03-01 08:00:00', '2025-03-01 08:00:00');

-- 费率表
CREATE TABLE `eb_rate`
(
    `id`             bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '费率ID',
    `rate_name`      varchar(50)    NOT NULL COMMENT '费率名称',
    `user_type`      varchar(20)    NOT NULL COMMENT '用户类型: 居民用户/商业用户',
    `price`          decimal(10, 2) NOT NULL COMMENT '每度电费价格',
    `start_time`     time           NOT NULL COMMENT '开始时间',
    `end_time`       time           NOT NULL COMMENT '结束时间',
    `peak_price`     decimal(10, 2)          DEFAULT NULL COMMENT '峰时价格',
    `flat_price`     decimal(10, 2)          DEFAULT NULL COMMENT '平时价格',
    `valley_price`   decimal(10, 2)          DEFAULT NULL COMMENT '谷时价格',
    `status`         tinyint(4)              DEFAULT '1' COMMENT '状态: 0禁用/1启用',
    `effective_date` date           NOT NULL COMMENT '生效日期',
    `expire_date`    date                    DEFAULT NULL COMMENT '失效日期',
    `created_at`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_type` (`user_type`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_rate` (`rate_name`, `user_type`, `price`, `start_time`, `end_time`, `peak_price`, `flat_price`,
                       `valley_price`, `status`, `effective_date`, `expire_date`, `created_at`, `updated_at`)
VALUES ('居民峰谷费率', '居民用户', 0.50, '00:00:00', '23:59:59', 0.60, 0.50, 0.40, 1, '2025-01-01', NULL,
        '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
       ('商业标准费率', '商业用户', 0.80, '00:00:00', '23:59:59', NULL, NULL, NULL, 1, '2025-01-01', NULL,
        '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
       ('居民夏季费率', '居民用户', 0.55, '00:00:00', '23:59:59', 0.65, 0.55, 0.45, 0, '2025-06-01', '2025-09-30',
        '2025-03-01 00:00:00', '2025-03-01 00:00:00'),
       ('商业高峰费率', '商业用户', 0.90, '08:00:00', '22:00:00', 0.90, 0.80, 0.70, 1, '2025-01-01', NULL,
        '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
       ('居民冬季费率', '居民用户', 0.45, '00:00:00', '23:59:59', 0.55, 0.45, 0.35, 0, '2025-12-01', '2026-02-28',
        '2025-03-01 00:00:00', '2025-03-01 00:00:00'),
       ('商业低谷费率', '商业用户', 0.70, '00:00:00', '07:59:59', NULL, NULL, NULL, 1, '2025-01-01', NULL,
        '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
       ('居民标准费率', '居民用户', 0.50, '00:00:00', '23:59:59', NULL, NULL, NULL, 1, '2025-01-01', NULL,
        '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
       ('商业临时费率', '商业用户', 0.85, '00:00:00', '23:59:59', NULL, NULL, NULL, 0, '2025-03-01', '2025-03-31',
        '2025-03-01 00:00:00', '2025-03-01 00:00:00'),
       ('居民夜间费率', '居民用户', 0.40, '22:00:00', '07:59:59', NULL, NULL, NULL, 1, '2025-01-01', NULL,
        '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
       ('商业全天费率', '商业用户', 0.80, '00:00:00', '23:59:59', NULL, NULL, NULL, 1, '2025-01-01', NULL,
        '2025-01-01 00:00:00', '2025-01-01 00:00:00');

-- 对账表
CREATE TABLE `eb_reconciliation`
(
    `id`             bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '对账单ID',
    `user_id`        bigint(20)     NOT NULL COMMENT '用户ID',
    `start_date`     date           NOT NULL COMMENT '开始日期',
    `end_date`       date           NOT NULL COMMENT '结束日期',
    `total_usage`    decimal(10, 2) NOT NULL COMMENT '总用电量',
    `total_amount`   decimal(10, 2) NOT NULL COMMENT '总金额',
    `status`         varchar(20)    NOT NULL COMMENT '状态: pending（待处理）/completed（已完成）',
    `payment_status` varchar(20)    NOT NULL COMMENT '支付状态: unpaid（未支付）/paid（已支付）',
    `approver_id`    bigint(20)              DEFAULT NULL COMMENT '审批人ID',
    `approval_time`  datetime                DEFAULT NULL COMMENT '审批时间',
    `comment`        text COMMENT '审批意见',
    `created_at`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `payment_id`     bigint(20)              DEFAULT NULL COMMENT '支付记录ID',
    PRIMARY KEY (`id`),
    KEY `approver_id` (`approver_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_payment_status` (`payment_status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 29
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_reconciliation` (`user_id`, `start_date`, `end_date`, `total_usage`, `total_amount`, `status`,
                                 `payment_status`, `approver_id`, `approval_time`, `comment`, `created_at`,
                                 `updated_at`, `payment_id`)
VALUES (1, '2025-03-01', '2025-03-05', 160.50, 84.30, 'completed', 'paid', 1, '2025-03-07 10:00:00', '已核对',
        '2025-03-06 09:00:00', '2025-03-07 10:00:00', 1),
       (2, '2025-03-01', '2025-03-05', 75.30, 60.24, 'completed', 'paid', 2, '2025-03-08 11:00:00', '无误',
        '2025-03-07 09:00:00', '2025-03-08 11:00:00', 2),
       (3, '2025-03-01', '2025-03-05', 120.75, 72.45, 'completed', 'paid', 3, '2025-03-09 12:00:00', '已核对',
        '2025-03-08 09:00:00', '2025-03-09 12:00:00', 3),
       (4, '2025-03-01', '2025-03-05', 90.20, 72.16, 'completed', 'paid', 4, '2025-03-10 13:00:00', '无误',
        '2025-03-09 09:00:00', '2025-03-10 13:00:00', 4),
       (5, '2025-03-01', '2025-03-05', 50.00, 20.00, 'pending', 'unpaid', NULL, NULL, NULL, '2025-03-10 09:00:00',
        '2025-03-10 09:00:00', NULL),
       (6, '2025-03-01', '2025-03-05', 110.30, 66.18, 'completed', 'paid', 6, '2025-03-12 14:00:00', '已核对',
        '2025-03-11 09:00:00', '2025-03-12 14:00:00', 6),
       (7, '2025-03-01', '2025-03-05', 85.60, 68.48, 'completed', 'paid', 7, '2025-03-13 15:00:00', '无误',
        '2025-03-12 09:00:00', '2025-03-13 15:00:00', 7),
       (8, '2025-03-01', '2025-03-05', 70.40, 28.16, 'pending', 'unpaid', NULL, NULL, NULL, '2025-03-13 09:00:00',
        '2025-03-13 09:00:00', NULL),
       (9, '2025-03-01', '2025-03-05', 130.90, 78.54, 'completed', 'paid', 9, '2025-03-15 16:00:00', '已核对',
        '2025-03-14 09:00:00', '2025-03-15 16:00:00', 9),
       (1, '2025-03-06', '2025-03-10', 60.00, 24.00, 'completed', 'paid', 1, '2025-03-15 17:00:00', '补充核对',
        '2025-03-15 09:00:00', '2025-03-15 17:00:00', 10);

-- 角色表
CREATE TABLE `eb_role`
(
    `id`         bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`  varchar(50) NOT NULL COMMENT '角色名称',
    `role_desc`  varchar(255)         DEFAULT NULL COMMENT '角色描述',
    `status`     tinyint(4)           DEFAULT '1' COMMENT '状态: 0禁用/1启用',
    `created_at` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_role_name` (`role_name`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1866859201658155009
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_role` (`role_name`, `role_desc`, `status`, `created_at`, `updated_at`)
VALUES ('超级管理员', '所有权限', 1, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('普通管理员', '部分权限', 1, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('财务管理员', '账单和支付管理', 1, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('客服管理员', '用户反馈和通知管理', 1, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('系统管理员', '日志和权限管理', 1, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('临时管理员', '临时权限', 0, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('审核管理员', '对账和支付审核', 1, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('电表管理员', '电表管理', 1, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('通知管理员', '通知管理', 1, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
       ('数据管理员', '数据查看和统计', 1, '2025-03-01 08:00:00', '2025-03-01 08:00:00');

-- 角色权限关联表
CREATE TABLE `eb_role_permission`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色权限ID',
    `role_id`       bigint(20) NOT NULL COMMENT '角色ID',
    `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
    `created_at`    datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `permission_id` (`permission_id`),
    KEY `idx_role_permission` (`role_id`, `permission_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 89
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_role_permission` (`role_id`, `permission_id`, `created_at`)
VALUES (1, 1, '2025-03-01 08:00:00'),
       (1, 2, '2025-03-01 08:00:00'),
       (1, 3, '2025-03-01 08:00:00'),
       (2, 4, '2025-03-01 08:00:00'),
       (2, 5, '2025-03-01 08:00:00'),
       (3, 6, '2025-03-01 08:00:00'),
       (3, 7, '2025-03-01 08:00:00'),
       (4, 8, '2025-03-01 08:00:00'),
       (4, 9, '2025-03-01 08:00:00'),
       (5, 10, '2025-03-01 08:00:00');

-- 系统日志表
CREATE TABLE `eb_system_log`
(
    `id`             bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `operator_id`    bigint(20)  NOT NULL COMMENT '操作人ID',
    `operator_name`  varchar(50) NOT NULL COMMENT '操作人姓名',
    `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
    `module`         varchar(50) NOT NULL COMMENT '模块名称',
    `description`    text        NOT NULL COMMENT '操作描述',
    `request_params` text COMMENT '请求参数',
    `response_data`  text COMMENT '响应数据',
    `ip`             varchar(50) NOT NULL COMMENT 'IP地址',
    `user_agent`     varchar(255)         DEFAULT NULL COMMENT '用户代理',
    `status`         varchar(20) NOT NULL COMMENT '操作状态: success（成功）/fail（失败）',
    `error_msg`      text COMMENT '错误信息',
    `created_at`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `request_body`   varchar(4000)        DEFAULT NULL COMMENT '请求体',
    PRIMARY KEY (`id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1898747109711646723
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_system_log` (`operator_id`, `operator_name`, `operation_type`, `module`, `description`,
                             `request_params`, `response_data`, `ip`, `user_agent`, `status`, `error_msg`, `created_at`,
                             `request_body`)
VALUES (1, 'admin1', '登录', 'auth', '管理员登录成功', '{"account":"admin1"}', '{"status":"success"}', '192.168.1.1',
        'Mozilla/5.0', 'success', NULL, '2025-03-14 10:00:00', NULL),
       (2, 'admin2', '支付审核', 'payment', '审核用户2的支付', '{"payment_id":2}', '{"status":"success"}',
        '192.168.1.2', 'Mozilla/5.0', 'success', NULL, '2025-03-07 09:30:00', NULL),
       (3, 'admin3', '对账审核', 'reconciliation', '审核用户3的对账单', '{"reconciliation_id":3}',
        '{"status":"success"}', '192.168.1.3', 'Mozilla/5.0', 'success', NULL, '2025-03-09 12:00:00', NULL),
       (4, 'admin4', '发送通知', 'notification', '发送账单提醒给用户4', '{"notification_id":4}', '{"status":"success"}',
        '192.168.1.4', 'Mozilla/5.0', 'success', NULL, '2025-03-06 09:00:00', NULL),
       (5, 'admin5', '登录', 'auth', '管理员登录失败', '{"account":"admin5"}', '{"status":"fail"}', '192.168.1.5',
        'Mozilla/5.0', 'fail', '密码错误', '2025-03-12 14:00:00', NULL),
       (6, 'admin6', '支付审核', 'payment', '审核用户6的支付', '{"payment_id":6}', '{"status":"success"}',
        '192.168.1.6', 'Mozilla/5.0', 'success', NULL, '2025-03-11 12:00:00', NULL),
       (7, 'admin7', '对账审核', 'reconciliation', '审核用户7的对账单', '{"reconciliation_id":7}',
        '{"status":"success"}', '192.168.1.7', 'Mozilla/5.0', 'success', NULL, '2025-03-13 15:00:00', NULL),
       (8, 'admin8', '查看日志', 'log', '查看系统日志', '{"date":"2025-03-13"}', '{"status":"success"}', '192.168.1.8',
        'Mozilla/5.0', 'success', NULL, '2025-03-13 09:00:00', NULL),
       (9, 'admin9', '发送通知', 'notification', '发送反馈处理通知给用户9', '{"notification_id":8}',
        '{"status":"success"}', '192.168.1.9', 'Mozilla/5.0', 'success', NULL, '2025-03-14 16:00:00', NULL),
       (10, 'admin10', '用户管理', 'user', '编辑用户1信息', '{"user_id":1}', '{"status":"success"}', '192.168.1.10',
        'Mozilla/5.0', 'success', NULL, '2025-03-15 10:00:00', NULL);

-- 用户信息表
CREATE TABLE `eb_user`
(
    `id`                bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `account`           varchar(50)  NOT NULL COMMENT '账号',
    `password`          varchar(100) NOT NULL COMMENT '密码',
    `username`          varchar(50)  NOT NULL COMMENT '用户名',
    `phone`             varchar(20)  NOT NULL COMMENT '电话',
    `address`           varchar(255) NOT NULL COMMENT '地址',
    `meter_no`          varchar(50)  NOT NULL COMMENT '电表编号',
    `user_type`         varchar(20)  NOT NULL COMMENT '用户类型: 居民用户/商业用户',
    `account_status`    varchar(20)  NOT NULL DEFAULT '正常' COMMENT '账号状态: 正常/欠费/停用',
    `id_card_no`        varchar(50)           DEFAULT NULL COMMENT '身份证号',
    `last_payment_date` datetime              DEFAULT NULL COMMENT '最近缴费时间',
    `created_at`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `account` (`account`),
    UNIQUE KEY `meter_no` (`meter_no`),
    KEY `idx_phone` (`phone`),
    KEY `idx_meter_no` (`meter_no`),
    KEY `idx_user_type` (`user_type`),
    KEY `idx_account_status` (`account_status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 35
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_user` (`account`, `password`, `username`, `phone`, `address`, `meter_no`, `user_type`, `account_status`,
                       `id_card_no`, `last_payment_date`, `created_at`, `updated_at`)
VALUES ('user1', 'hashed_pass1', '张三', '13812345678', '北京市朝阳区1号', 'M001', '居民用户', '正常',
        '110101199001011234', '2025-03-15 15:00:00', '2025-03-01 09:00:00', '2025-03-15 15:00:00'),
       ('user2', 'hashed_pass2', '李四', '13987654321', '上海市浦东新区2号', 'M002', '商业用户', '正常',
        '310101199002021234', '2025-03-07 09:30:00', '2025-03-01 10:00:00', '2025-03-07 09:30:00'),
       ('user3', 'hashed_pass3', '王五', '13711112222', '广州市天河区3号', 'M003', '居民用户', '正常',
        '440101199003031234', '2025-03-08 10:00:00', '2025-03-02 09:00:00', '2025-03-08 10:00:00'),
       ('user4', 'hashed_pass4', '赵六', '13633334444', '深圳市南山区4号', 'M004', '商业用户', '正常',
        '440301199004041234', '2025-03-09 11:00:00', '2025-03-02 10:00:00', '2025-03-09 11:00:00'),
       ('user5', 'hashed_pass5', '孙七', '13555556666', '成都市锦江区5号', 'M005', '居民用户', '欠费',
        '510101199005051234', NULL, '2025-03-03 09:00:00', '2025-03-03 09:00:00'),
       ('user6', 'hashed_pass6', '周八', '13477778888', '杭州市西湖区6号', 'M006', '商业用户', '正常',
        '330101199006061234', '2025-03-11 12:00:00', '2025-03-03 10:00:00', '2025-03-11 12:00:00'),
       ('user7', 'hashed_pass7', '吴九', '13399990000', '南京市玄武区7号', 'M007', '居民用户', '正常',
        '320101199007071234', '2025-03-12 13:00:00', '2025-03-04 09:00:00', '2025-03-12 13:00:00'),
       ('user8', 'hashed_pass8', '郑十', '13222223333', '武汉市武昌区8号', 'M008', '商业用户', '欠费',
        '420101199008081234', NULL, '2025-03-04 10:00:00', '2025-03-04 10:00:00'),
       ('user9', 'hashed_pass9', '钱十一', '13144445555', '西安市雁塔区9号', 'M009', '居民用户', '正常',
        '610101199009091234', '2025-03-14 14:00:00', '2025-03-05 09:00:00', '2025-03-14 14:00:00'),
       ('user10', 'hashed_pass10', '孙十二', '13066667777', '重庆市渝中区10号', 'M010', '商业用户', '停用',
        '500101199010101234', NULL, '2025-03-05 10:00:00', '2025-03-05 10:00:00');

-- 用户反馈表
CREATE TABLE `eb_user_feedback`
(
    `id`              bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
    `user_id`         bigint(20)  NOT NULL COMMENT '用户ID',
    `feedback_type`   varchar(20) NOT NULL COMMENT '反馈类型: complaint（投诉）/suggestion（建议）/question（问题）',
    `content`         text        NOT NULL COMMENT '反馈内容',
    `status`          varchar(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending（待处理）/processed（已处理）/closed（已关闭）',
    `submit_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `process_time`    datetime             DEFAULT NULL COMMENT '处理时间',
    `processor_id`    bigint(20)           DEFAULT NULL COMMENT '处理人ID',
    `response`        text                 DEFAULT NULL COMMENT '回复内容',
    `notification_id` bigint(20)           DEFAULT NULL COMMENT '通知ID',
    `created_at`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_submit_time` (`submit_time`),
    KEY `idx_notification_id` (`notification_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_user_feedback` (`user_id`, `feedback_type`, `content`, `status`, `submit_time`, `process_time`,
                                `processor_id`, `response`, `notification_id`, `created_at`, `updated_at`)
VALUES (1, 'complaint', '电表读数不准', 'pending', '2025-03-10 14:00:00', NULL, NULL, NULL, 1, '2025-03-10 14:00:00',
        '2025-03-10 14:00:00'),
       (2, 'suggestion', '建议增加缴费提醒', 'processed', '2025-03-11 09:00:00', '2025-03-11 10:00:00', 1,
        '感谢建议，已记录', NULL, '2025-03-11 09:00:00', '2025-03-11 10:00:00'),
       (3, 'question', '如何查看历史账单？', 'processed', '2025-03-12 10:00:00', '2025-03-12 11:00:00', 2,
        '请登录系统查看', NULL, '2025-03-12 10:00:00', '2025-03-12 11:00:00'),
       (4, 'complaint', '电费计算错误', 'pending', '2025-03-11 15:00:00', NULL, NULL, NULL, 5, '2025-03-11 15:00:00',
        '2025-03-11 15:00:00'),
       (5, 'suggestion', '希望支持分期付款', 'pending', '2025-03-13 09:00:00', NULL, NULL, NULL, NULL,
        '2025-03-13 09:00:00', '2025-03-13 09:00:00'),
       (6, 'question', '电表更换流程是什么？', 'processed', '2025-03-14 10:00:00', '2025-03-14 11:00:00', 3,
        '请联系客服', NULL, '2025-03-14 10:00:00', '2025-03-14 11:00:00'),
       (7, 'complaint', '账单未及时更新', 'pending', '2025-03-14 16:00:00', NULL, NULL, NULL, 8, '2025-03-14 16:00:00',
        '2025-03-14 16:00:00'),
       (8, 'suggestion', '增加用电统计功能', 'pending', '2025-03-15 09:00:00', NULL, NULL, NULL, NULL,
        '2025-03-15 09:00:00', '2025-03-15 09:00:00'),
       (9, 'question', '电费余额在哪里查看？', 'processed', '2025-03-15 10:00:00', '2025-03-15 11:00:00', 4,
        '在个人中心查看', NULL, '2025-03-15 10:00:00', '2025-03-15 11:00:00'),
       (1, 'complaint', '支付失败未退款', 'pending', '2025-03-15 14:00:00', NULL, NULL, NULL, NULL,
        '2025-03-15 14:00:00', '2025-03-15 14:00:00');

-- 账单表
CREATE TABLE `eb_bill`
(
    `id`           bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '账单ID',
    `user_id`      bigint(20)     NOT NULL COMMENT '用户ID',
    `bill_no`      varchar(50)    NOT NULL COMMENT '账单编号',
    `start_date`   date           NOT NULL COMMENT '账单开始日期',
    `end_date`     date           NOT NULL COMMENT '账单结束日期',
    `usage_amount` decimal(10, 2) NOT NULL COMMENT '用电量（度）',
    `total_amount` decimal(10, 2) NOT NULL COMMENT '总金额',
    `status`       varchar(20)    NOT NULL DEFAULT '未支付' COMMENT '状态: 未支付/已支付/逾期',
    `payment_id`   bigint(20)              DEFAULT NULL COMMENT '支付记录ID',
    `created_at`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `bill_no` (`bill_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_bill` (`user_id`, `bill_no`, `start_date`, `end_date`, `usage_amount`, `total_amount`, `status`,
                       `payment_id`, `created_at`, `updated_at`)
VALUES (1, 'BILL20250301', '2025-03-01', '2025-03-05', 160.50, 84.30, '已支付', 1, '2025-03-06 09:00:00',
        '2025-03-06 15:00:00'),
       (2, 'BILL20250302', '2025-03-01', '2025-03-05', 75.30, 60.24, '已支付', 2, '2025-03-07 09:00:00',
        '2025-03-07 09:30:00'),
       (3, 'BILL20250303', '2025-03-01', '2025-03-05', 120.75, 72.45, '已支付', 3, '2025-03-08 09:00:00',
        '2025-03-08 10:00:00'),
       (4, 'BILL20250304', '2025-03-01', '2025-03-05', 90.20, 72.16, '已支付', 4, '2025-03-09 09:00:00',
        '2025-03-09 11:00:00'),
       (5, 'BILL20250305', '2025-03-01', '2025-03-05', 50.00, 20.00, '未支付', NULL, '2025-03-10 09:00:00',
        '2025-03-10 09:00:00'),
       (6, 'BILL20250306', '2025-03-01', '2025-03-05', 110.30, 66.18, '已支付', 6, '2025-03-11 09:00:00',
        '2025-03-11 12:00:00'),
       (7, 'BILL20250307', '2025-03-01', '2025-03-05', 85.60, 68.48, '已支付', 7, '2025-03-12 09:00:00',
        '2025-03-12 13:00:00'),
       (8, 'BILL20250308', '2025-03-01', '2025-03-05', 70.40, 28.16, '未支付', NULL, '2025-03-13 09:00:00',
        '2025-03-13 09:00:00'),
       (9, 'BILL20250309', '2025-03-01', '2025-03-05', 130.90, 78.54, '已支付', 9, '2025-03-14 09:00:00',
        '2025-03-14 14:00:00'),
       (1, 'BILL20250310', '2025-03-06', '2025-03-10', 60.00, 24.00, '已支付', 10, '2025-03-15 09:00:00',
        '2025-03-15 15:00:00');

-- 公告表
CREATE TABLE `eb_announcement`
(
    `id`         bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title`      varchar(100) NOT NULL COMMENT '标题',
    `content`    text         NOT NULL COMMENT '内容',
    `start_time` datetime     NOT NULL COMMENT '开始时间',
    `end_time`   datetime              DEFAULT NULL COMMENT '结束时间',
    `status`     varchar(20)  NOT NULL DEFAULT '有效' COMMENT '状态: 有效/过期',
    `created_at` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_time` (`start_time`, `end_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

INSERT INTO `eb_announcement` (`title`, `content`, `start_time`, `end_time`, `status`, `created_at`, `updated_at`)
VALUES ('停电通知', '3月20日8:00-12:00停电，请做好准备', '2025-03-15 00:00:00', '2025-03-20 12:00:00', '有效',
        '2025-03-14 09:00:00', '2025-03-14 09:00:00'),
       ('费率调整', '4月起居民用户费率调整为0.55元/度', '2025-03-15 00:00:00', NULL, '有效', '2025-03-14 10:00:00',
        '2025-03-14 10:00:00'),
       ('系统维护', '3月16日凌晨0:00-2:00系统维护', '2025-03-15 00:00:00', '2025-03-16 02:00:00', '有效',
        '2025-03-14 11:00:00', '2025-03-14 11:00:00'),
       ('春节优惠', '春节期间用电折扣10%', '2025-01-20 00:00:00', '2025-02-10 00:00:00', '过期', '2025-01-15 09:00:00',
        '2025-02-10 00:00:00'),
       ('安全提示', '请勿私自改装电表', '2025-03-10 00:00:00', NULL, '有效', '2025-03-09 09:00:00',
        '2025-03-09 09:00:00'),
       ('用电高峰', '夏季用电高峰，请节约用电', '2025-06-01 00:00:00', '2025-09-30 00:00:00', '有效',
        '2025-03-01 09:00:00', '2025-03-01 09:00:00'),
       ('新功能上线', '用电统计功能已上线', '2025-03-15 00:00:00', NULL, '有效', '2025-03-14 12:00:00',
        '2025-03-14 12:00:00'),
       ('政策通知', '新能源补贴政策更新', '2025-03-01 00:00:00', '2025-12-31 00:00:00', '有效', '2025-02-28 09:00:00',
        '2025-02-28 09:00:00'),
       ('客服热线', '新客服热线：400-123-4567', '2025-03-10 00:00:00', NULL, '有效', '2025-03-09 10:00:00',
        '2025-03-09 10:00:00'),
       ('节电倡议', '倡导绿色用电，从我做起', '2025-03-15 00:00:00', NULL, '有效', '2025-03-14 13:00:00',
        '2025-03-14 13:00:00');