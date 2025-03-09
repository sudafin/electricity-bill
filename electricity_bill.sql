/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 50719
 Source Host           : localhost:3306
 Source Schema         : electricity_bill

 Target Server Type    : MySQL
 Target Server Version : 50719
 File Encoding         : 65001

 Date: 25/12/2024 17:32:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for eb_admin
-- ----------------------------
DROP TABLE IF EXISTS `eb_admin`;
CREATE TABLE `eb_admin`
(
    `id`              bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `account`         varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '账号',
    `password`        varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
    `email`           varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '邮箱',
    `phone`           varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '电话',
    `role_id`         bigint(20)                                              NOT NULL COMMENT '角色ID',
    `status`          tinyint(4)                                              NULL     DEFAULT 1 COMMENT '状态:0禁用/1启用',
    `last_login_time` datetime                                                NULL     DEFAULT NULL COMMENT '最后登录时间',
    `created_at`      datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `role_id` (`role_id`) USING BTREE,
    INDEX `idx_username` (`account`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    CONSTRAINT `eb_admin_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `eb_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  AUTO_INCREMENT = 1865685594955948035
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_admin
-- ----------------------------
INSERT INTO `eb_admin`
VALUES (1, 'admin123', '$2a$10$4FUHuxcpYOIomnc3CIJfcOnCYk0P0corhysvagSvIqy234vm3hj9u', 'admin@example.com',
        '13012345678', 1, 1, '2023-05-20 09:00:00', '2024-11-26 16:03:48', '2024-12-04 16:35:04');
INSERT INTO `eb_admin`
VALUES (2, 'admin123456', '$2a$10$U1cWB05CGTfYka10fSAVsO88HRLQgMoye79YVyWKnCyQ0DHBXnE2y', 'auditor@example.com',
        '13112345678', 1, 1, '2023-05-19 16:30:00', '2024-11-26 16:03:48', '2024-11-26 16:03:48');
INSERT INTO `eb_admin`
VALUES (3, 'operator123', '$2a$10$7wSUecjntei4CyfYg25zfOxfOUlg7rjHS27mJgI3jHo0b6JzvA3yW', 'operator@example.com',
        '13212345678', 1866859201658155008, 1, '2023-05-01 11:00:00', '2024-11-26 16:03:48', '2024-11-26 16:03:48');
INSERT INTO `eb_admin`
VALUES (1865685594955948034, 'admin12345', '$2a$10$0iWj5yYJDFEfCEGpoSN3SOlRe3dS.n6KwAyrTV8lUjDLyaDhazb82', NULL, NULL,
        1866859201658155008, 1, NULL, '2024-12-08 17:11:23', '2024-12-08 17:11:23');

-- ----------------------------
-- Table structure for eb_electricity_usage
-- ----------------------------
DROP TABLE IF EXISTS `eb_electricity_usage`;
CREATE TABLE `eb_electricity_usage`
(
    `id`           bigint(20)                                             NOT NULL AUTO_INCREMENT,
    `user_id`      bigint(20)                                             NOT NULL COMMENT '用户ID',
    `meter_no`     varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '电表编号',
    `usage_amount` decimal(10, 2)                                         NOT NULL COMMENT '用电量',
    `rate_id`      bigint(20)                                             NOT NULL COMMENT '费率ID',
    `fee_amount`   decimal(10, 2)                                         NOT NULL COMMENT '电费金额',
    `start_time`   datetime                                               NOT NULL COMMENT '用电开始时间',
    `end_time`     datetime                                               NOT NULL COMMENT '用电结束时间',
    `time_segment` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用电时段:peak/flat/valley',
    `created_at`   datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `rate_id` (`rate_id`) USING BTREE,
    INDEX `idx_user_usage` (`user_id`, `start_time`) USING BTREE,
    INDEX `idx_meter_no` (`meter_no`) USING BTREE,
    INDEX `idx_time_segment` (`time_segment`) USING BTREE,
    CONSTRAINT `eb_electricity_usage_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `eb_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `eb_electricity_usage_ibfk_2` FOREIGN KEY (`rate_id`) REFERENCES `eb_rate` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  AUTO_INCREMENT = 21
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_electricity_usage
-- ----------------------------
INSERT INTO `eb_electricity_usage`
VALUES (13, 1, 'METER1001', 100.00, 1, 50.00, '2022-05-01 09:00:00', '2022-05-01 12:00:00', 'peak',
        '2024-12-09 15:39:53');
INSERT INTO `eb_electricity_usage`
VALUES (14, 1, 'METER1001', 200.00, 1, 100.00, '2022-05-01 12:00:00', '2022-05-01 18:00:00', 'flat',
        '2024-12-09 15:39:53');
INSERT INTO `eb_electricity_usage`
VALUES (15, 2, 'METER1002', 500.00, 2, 500.00, '2023-04-15 08:00:00', '2023-03-15 22:00:00', 'peak',
        '2024-12-09 15:39:53');
INSERT INTO `eb_electricity_usage`
VALUES (16, 3, 'METER1003', 150.00, 1, 75.00, '2023-05-10 09:00:00', '2023-05-10 12:00:00', 'peak',
        '2024-12-09 15:39:53');
INSERT INTO `eb_electricity_usage`
VALUES (17, 3, 'METER1003', 300.00, 1, 150.00, '2024-05-10 12:00:00', '2024-05-10 18:00:00', 'flat',
        '2024-12-09 15:39:53');
INSERT INTO `eb_electricity_usage`
VALUES (18, 3, 'METER1003', 450.00, 1, 225.00, '2024-06-10 18:00:00', '2024-06-10 22:00:00', 'valley',
        '2024-12-09 15:39:53');
INSERT INTO `eb_electricity_usage`
VALUES (19, 3, 'METER1003', 600.00, 1, 300.00, '2024-06-11 00:00:00', '2024-06-11 06:00:00', 'valley',
        '2024-12-09 15:39:53');
INSERT INTO `eb_electricity_usage`
VALUES (20, 3, 'METER1003', 750.00, 1, 375.00, '2024-12-16 06:00:00', '2024-12-16 06:00:00', 'flat',
        '2024-12-09 15:39:53');

-- ----------------------------
-- Table structure for eb_notification
-- ----------------------------
DROP TABLE IF EXISTS `eb_notification`;
CREATE TABLE `eb_notification`
(
    `id`          bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `title`       varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '标题',
    `content`     text CHARACTER SET utf8 COLLATE utf8_general_ci         NOT NULL COMMENT '内容',
    `type`        varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '类型:system/approval',
    `level`       varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '级别:low/medium/high',
    `sender_id`   bigint(20)                                              NOT NULL COMMENT '发送人ID',
    `expire_time` datetime                                                NULL     DEFAULT NULL COMMENT '过期时间',
    `created_at`  datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `sender_id` (`sender_id`) USING BTREE,
    INDEX `idx_type` (`type`) USING BTREE,
    INDEX `idx_level` (`level`) USING BTREE,
    CONSTRAINT `eb_notification_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `eb_admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  AUTO_INCREMENT = 1864903793324937217
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_notification
-- ----------------------------
INSERT INTO `eb_notification`
VALUES (1864903793324937216, '测试', '测试', '系统通知', '普通', 1, '2024-12-25 00:00:00', '2024-12-06 13:24:47',
        '2024-12-06 13:24:47');

-- ----------------------------
-- Table structure for eb_notification_recipient
-- ----------------------------
DROP TABLE IF EXISTS `eb_notification_recipient`;
CREATE TABLE `eb_notification_recipient`
(
    `id`              bigint(20)                                             NOT NULL AUTO_INCREMENT,
    `notification_id` bigint(20)                                             NOT NULL COMMENT '通知ID',
    `recipient_type`  varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '接收对象类型:user/admin',
    `recipient_id`    bigint(20)                                             NOT NULL COMMENT '接收者ID',
    `read_status`     tinyint(4)                                             NULL     DEFAULT 0 COMMENT '阅读状态:0未读/1已读',
    `read_time`       datetime                                               NULL     DEFAULT NULL COMMENT '阅读时间',
    `created_at`      datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `notification_id` (`notification_id`) USING BTREE,
    INDEX `idx_recipient` (`recipient_type`, `recipient_id`) USING BTREE,
    INDEX `idx_read_status` (`read_status`) USING BTREE,
    CONSTRAINT `eb_notification_recipient_ibfk_1` FOREIGN KEY (`notification_id`) REFERENCES `eb_notification` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  AUTO_INCREMENT = 12
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_notification_recipient
-- ----------------------------
INSERT INTO `eb_notification_recipient`
VALUES (11, 1864903793324937216, '系统管理员', 1, 0, NULL, '2024-12-06 13:24:47', '2024-12-06 13:24:47');

-- ----------------------------
-- Table structure for eb_payment
-- ----------------------------
DROP TABLE IF EXISTS `eb_payment`;
CREATE TABLE `eb_payment`
(
    `id`                bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `user_id`           bigint(20)                                              NOT NULL COMMENT '用户ID',
    `amount`            decimal(10, 2)                                          NOT NULL COMMENT '支付金额',
    `payment_method`    varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '支付方式',
    `transaction_no`    varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '支付流水号',
    `status`            varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '支付状态:pending/success/failed',
    `failure_reason`    varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '失败原因',
    `payment_time`      datetime                                                NULL     DEFAULT NULL COMMENT '支付时间',
    `refund_status`     varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT 'none' COMMENT '退款状态:none/processing/success/failed',
    `refund_amount`     decimal(10, 2)                                          NULL     DEFAULT NULL COMMENT '退款金额',
    `refund_time`       datetime                                                NULL     DEFAULT NULL COMMENT '退款时间',
    `created_at`        datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `remark`            varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '备注',
    `operator_id`       bigint(20)                                              NOT NULL COMMENT '操作人id',
    `reconciliation_id` bigint(20)                                              NULL     DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_user_payment` (`user_id`, `payment_time`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    INDEX `idx_refund_status` (`refund_status`) USING BTREE,
    CONSTRAINT `eb_payment_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `eb_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  AUTO_INCREMENT = 1866747996192960514
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_payment
-- ----------------------------
INSERT INTO `eb_payment`
VALUES (1, 1, 500.00, '支付宝', 'TRANS1001', '已支付', NULL, '2023-05-01 10:00:00', 'none', NULL, NULL,
        '2024-11-26 16:03:40', '2024-12-06 19:47:00', '正常缴费', 1, NULL);
INSERT INTO `eb_payment`
VALUES (2, 2, 800.00, '微信', 'TRANS1002', '已支付', NULL, '2023-04-15 15:30:00', 'none', NULL, NULL,
        '2024-11-26 16:03:40', '2024-12-06 19:47:00', '补交上月', 2, NULL);
INSERT INTO `eb_payment`
VALUES (1865011734216531969, 1, 0.00, '微信', NULL, '已支付', NULL, '2024-12-06 20:33:42', '已退款', 0.00,
        '2024-12-06 20:36:24', '2024-12-06 20:33:42', '2024-12-06 20:33:42', NULL, 1, 1865011734216531968);
INSERT INTO `eb_payment`
VALUES (1866729564642893825, 1, 20.00, '微信', NULL, '已支付', NULL, '2024-12-11 14:19:45', 'none', NULL, NULL,
        '2024-12-11 14:19:45', '2024-12-11 14:19:45', NULL, 1, 1866729564642893824);
INSERT INTO `eb_payment`
VALUES (1866747996192960513, 1, 20.00, '微信', NULL, '已支付', NULL, '2024-12-11 15:32:59', 'none', NULL, NULL,
        '2024-12-11 15:32:59', '2024-12-11 15:32:59', NULL, 1, 1866747996192960512);

-- ----------------------------
-- Table structure for eb_permission
-- ----------------------------
DROP TABLE IF EXISTS `eb_permission`;
CREATE TABLE `eb_permission`
(
    `id`              bigint(20)                                             NOT NULL AUTO_INCREMENT,
    `permission_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '权限名称',
    `permission_code` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '权限编码',
    `permission_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '权限类型:menu/action',
    `parent_id`       bigint(20)                                             NULL     DEFAULT NULL COMMENT '父权限ID',
    `created_at`      datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_permission_code` (`permission_code`) USING BTREE,
    INDEX `idx_permission_type` (`permission_type`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 39
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_permission
-- ----------------------------
INSERT INTO `eb_permission`
VALUES (1, '首页', 'dashboard', 'menu', NULL, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (2, '用户管理', 'user', 'menu', NULL, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (3, '用户列表', 'user:page', 'action', 2, '2024-11-26 16:03:42', '2024-12-11 22:11:34');
INSERT INTO `eb_permission`
VALUES (4, '新增用户', 'user:create', 'action', 2, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (5, '编辑用户', 'user:edit', 'action', 2, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (6, '删除用户', 'user:delete', 'action', 2, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (7, '数据统计与报表', 'report', 'menu', NULL, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (8, '对账与审批', 'reconciliation', 'menu', NULL, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (9, '对账列表', 'reconciliation:page', 'action', 8, '2024-11-26 16:03:42', '2024-12-11 22:11:34');
INSERT INTO `eb_permission`
VALUES (10, '审批对账单', 'reconciliation:approve', 'action', 8, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (11, '通知和提醒', 'notification', 'menu', NULL, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (12, '通知列表', 'notification:page', 'action', 11, '2024-11-26 16:03:42', '2024-12-11 22:11:34');
INSERT INTO `eb_permission`
VALUES (13, '新增通知', 'notification:create', 'action', 11, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (14, '编辑通知', 'notification:edit', 'action', 11, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (15, '删除通知', 'notification:delete', 'action', 11, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (16, '支付管理', 'payment', 'menu', NULL, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (17, '支付列表', 'payment:page', 'action', 16, '2024-11-26 16:03:42', '2024-12-11 22:11:34');
INSERT INTO `eb_permission`
VALUES (18, '系统设置', 'setting', 'menu', NULL, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_permission`
VALUES (19, '角色权限管理', 'role', 'menu', NULL, '2024-11-26 16:03:42', '2024-12-11 23:21:59');
INSERT INTO `eb_permission`
VALUES (20, '费率管理', 'rate', 'menu', NULL, '2024-11-26 16:03:42', '2024-12-11 23:21:59');
INSERT INTO `eb_permission`
VALUES (21, '日志管理', 'log', 'menu', NULL, '2024-11-26 16:03:42', '2024-12-11 23:21:59');
INSERT INTO `eb_permission`
VALUES (22, '获取用户详情', 'user:detail', 'action', 2, '2024-12-11 23:11:32', '2024-12-11 23:11:38');
INSERT INTO `eb_permission`
VALUES (31, '获取用户账单', 'user:bill', 'action', 2, '2024-12-11 23:12:08', '2024-12-11 23:19:58');
INSERT INTO `eb_permission`
VALUES (32, '支付用户账单', 'user:pay', 'action', 2, '2024-12-11 23:19:58', '2024-12-11 23:19:58');
INSERT INTO `eb_permission`
VALUES (33, '对账详情', 'reconciliation:detail', 'action', 8, '2024-12-11 23:19:58', '2024-12-11 23:19:58');
INSERT INTO `eb_permission`
VALUES (34, '对账导出', 'reconciliation:export', 'action', 8, '2024-12-11 23:19:58', '2024-12-11 23:19:58');
INSERT INTO `eb_permission`
VALUES (35, '支付详情', 'payment:detail', 'action', 16, '2024-12-11 23:19:58', '2024-12-11 23:19:58');
INSERT INTO `eb_permission`
VALUES (36, '支付导出', 'payment:export', 'action', 16, '2024-12-11 23:19:58', '2024-12-11 23:19:58');
INSERT INTO `eb_permission`
VALUES (37, '支付退款', 'payment:refund', 'action', 16, '2024-12-11 23:19:58', '2024-12-11 23:19:58');
INSERT INTO `eb_permission`
VALUES (38, '支付删除', 'payment:delete', 'action', 16, '2024-12-11 23:19:58', '2024-12-11 23:19:58');

-- ----------------------------
-- Table structure for eb_rate
-- ----------------------------
DROP TABLE IF EXISTS `eb_rate`;
CREATE TABLE `eb_rate`
(
    `id`             bigint(20)                                             NOT NULL AUTO_INCREMENT,
    `rate_name`      varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '费率名称',
    `user_type`      varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户类型',
    `price`          decimal(10, 2)                                         NOT NULL COMMENT '每度电费价格',
    `start_time`     time                                                   NOT NULL COMMENT '开始时间',
    `end_time`       time                                                   NOT NULL COMMENT '结束时间',
    `peak_price`     decimal(10, 2)                                         NULL     DEFAULT NULL COMMENT '峰时价格',
    `flat_price`     decimal(10, 2)                                         NULL     DEFAULT NULL COMMENT '平时价格',
    `valley_price`   decimal(10, 2)                                         NULL     DEFAULT NULL COMMENT '谷时价格',
    `status`         tinyint(4)                                             NULL     DEFAULT 1 COMMENT '状态:0禁用/1启用',
    `effective_date` date                                                   NOT NULL COMMENT '生效日期',
    `expire_date`    date                                                   NULL     DEFAULT NULL COMMENT '失效日期',
    `created_at`     datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_user_type` (`user_type`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_rate
-- ----------------------------
INSERT INTO `eb_rate`
VALUES (1, '居民电价', '居民用户', 1.40, '09:00:00', '12:00:00', 0.80, 0.50, 0.30, 1, '2023-01-01', '2023-12-31',
        '2024-11-26 16:03:40', '2024-12-05 00:22:50');
INSERT INTO `eb_rate`
VALUES (2, '商业电价', '商业用户', 1.50, '08:00:00', '22:00:00', 1.50, 1.00, 0.80, 1, '2023-01-01', '2023-06-30',
        '2024-11-26 16:03:40', '2024-12-05 00:22:50');

-- ----------------------------
-- Table structure for eb_reconciliation
-- ----------------------------
DROP TABLE IF EXISTS `eb_reconciliation`;
CREATE TABLE `eb_reconciliation`
(
    `id`                bigint(20)                                             NOT NULL AUTO_INCREMENT,
    `reconciliation_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '对账单号',
    `user_id`           bigint(20)                                             NOT NULL COMMENT '用户ID',
    `start_date`        date                                                   NOT NULL COMMENT '开始日期',
    `end_date`          date                                                   NOT NULL COMMENT '结束日期',
    `total_usage`       decimal(10, 2)                                         NOT NULL COMMENT '总用电量',
    `total_amount`      decimal(10, 2)                                         NOT NULL COMMENT '总金额',
    `status`            varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '状态:pending/completed',
    `payment_status`    varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付状态:unpaid/paid',
    `approver_id`       bigint(20)                                             NULL     DEFAULT NULL COMMENT '审批人ID',
    `approval_time`     datetime                                               NULL     DEFAULT NULL COMMENT '审批时间',
    `comment`           text CHARACTER SET utf8 COLLATE utf8_general_ci        NULL COMMENT '审批意见',
    `created_at`        datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        datetime                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `payment_id`        bigint(20)                                             NULL     DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `reconciliation_no` (`reconciliation_no`) USING BTREE,
    INDEX `approver_id` (`approver_id`) USING BTREE,
    INDEX `idx_user_id` (`user_id`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    INDEX `idx_payment_status` (`payment_status`) USING BTREE,
    CONSTRAINT `eb_reconciliation_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `eb_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `eb_reconciliation_ibfk_2` FOREIGN KEY (`approver_id`) REFERENCES `eb_admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  AUTO_INCREMENT = 22
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_reconciliation
-- ----------------------------
INSERT INTO `eb_reconciliation`
VALUES (1, '1001', 1, '2023-05-01', '2023-05-31', 500.00, 250.00, '通过', '已支付', 1, '2024-12-04 01:28:09', '100',
        '2024-11-26 16:03:48', '2024-12-03 16:37:29', NULL);
INSERT INTO `eb_reconciliation`
VALUES (2, '1002', 2, '2023-04-01', '2023-04-30', 800.00, 800.00, '通过', '已支付', 2, '2024-12-03 16:24:59',
        '没有问题', '2024-11-26 16:03:48', '2024-12-03 21:38:45', NULL);
INSERT INTO `eb_reconciliation`
VALUES (3, '1003', 3, '2023-05-01', '2023-05-31', 200.00, 100.00, '拒绝', '未支付', 3, '2024-12-03 21:38:26', '无',
        '2024-11-26 16:03:48', '2024-12-04 00:40:20', NULL);
INSERT INTO `eb_reconciliation`
VALUES (4, '1004', 1, '2024-05-01', '2024-12-11', 500.00, 100.00, '通过', '已支付', 1, '2024-12-03 21:36:07', '无',
        '2024-12-03 21:36:20', '2024-12-03 21:38:45', NULL);
INSERT INTO `eb_reconciliation`
VALUES (10, '1864250205661650944', 1, '2024-12-04', '2024-12-11', 20.00, 20.00, '通过', '已支付', 1,
        '2024-12-04 18:07:56', 'ok', '2024-12-04 18:07:39', '2024-12-04 18:07:39', NULL);
INSERT INTO `eb_reconciliation`
VALUES (11, '1864250353166934016', 1, '2024-12-04', '2024-12-11', 20.00, 20.00, '拒绝', '已支付', 1,
        '2024-12-04 18:08:29', '20', '2024-12-04 18:08:14', '2024-12-04 18:08:14', NULL);
INSERT INTO `eb_reconciliation`
VALUES (12, '1864903596465278976', 1, '2024-12-06', '2024-12-13', 20.00, 20.00, '暂缓', '已支付', 1,
        '2024-12-06 13:52:07', NULL, '2024-12-06 13:24:00', '2024-12-06 13:24:00', NULL);
INSERT INTO `eb_reconciliation`
VALUES (13, '1864911318304776192', 1, '2024-12-06', '2024-12-13', 10.00, 10.00, '退回', '已支付', 1,
        '2024-12-06 13:54:51', NULL, '2024-12-06 13:54:41', '2024-12-06 13:54:41', NULL);
INSERT INTO `eb_reconciliation`
VALUES (14, '1864911518331133952', 1, '2024-12-06', '2024-12-13', 10.00, 10.00, '待审批', '已支付', NULL, NULL, NULL,
        '2024-12-06 13:55:29', '2024-12-06 13:55:29', NULL);
INSERT INTO `eb_reconciliation`
VALUES (15, '1864948579117395968', 1, '2024-12-06', '2024-12-13', 10.00, 10.00, '待审批', '已支付', NULL, NULL, NULL,
        '2024-12-06 16:22:45', '2024-12-06 16:22:45', NULL);
INSERT INTO `eb_reconciliation`
VALUES (16, '1864950173514964992', 1, '2024-12-06', '2024-12-13', 10.00, 10.00, '待审批', '已支付', NULL, NULL, NULL,
        '2024-12-06 16:29:05', '2024-12-06 16:29:05', 1864950173514964993);
INSERT INTO `eb_reconciliation`
VALUES (17, '1865007921308983296', 1, '2024-12-06', '2024-12-13', 20.00, 20.00, '待审批', '已支付', NULL, NULL, NULL,
        '2024-12-06 20:18:33', '2024-12-06 20:18:33', 1865007921308983297);
INSERT INTO `eb_reconciliation`
VALUES (18, '1865007949595369472', 1, '2024-12-06', '2024-12-13', 20.00, 20.00, '待审批', '已支付', NULL, NULL, NULL,
        '2024-12-06 20:18:40', '2024-12-06 20:18:40', 1865007949595369473);
INSERT INTO `eb_reconciliation`
VALUES (19, '1865011734216531968', 1, '2024-12-06', '2024-12-13', 0.00, 0.00, '退回', '已支付', 1,
        '2024-12-06 20:36:15', NULL, '2024-12-06 20:33:42', '2024-12-06 20:33:42', 1865011734216531969);
INSERT INTO `eb_reconciliation`
VALUES (20, '1866729564642893824', 1, '2024-12-11', '2024-12-18', 30.00, 20.00, '待审批', '已支付', NULL, NULL, NULL,
        '2024-12-11 14:19:45', '2024-12-11 14:19:45', 1866729564642893825);
INSERT INTO `eb_reconciliation`
VALUES (21, '1866747996192960512', 1, '2024-12-11', '2024-12-18', 30.00, 20.00, '待审批', '已支付', NULL, NULL, NULL,
        '2024-12-11 15:32:59', '2024-12-11 15:32:59', 1866747996192960513);

-- ----------------------------
-- Table structure for eb_role
-- ----------------------------
DROP TABLE IF EXISTS `eb_role`;
CREATE TABLE `eb_role`
(
    `id`         bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `role_name`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '角色名称',
    `role_desc`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '角色描述',
    `status`     tinyint(4)                                              NULL     DEFAULT 1 COMMENT '状态:0禁用/1启用',
    `created_at` datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_role_name` (`role_name`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1866859201658155009
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_role
-- ----------------------------
INSERT INTO `eb_role`
VALUES (1, '系统管理员', '拥有系统所有权限', 1, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_role`
VALUES (2, '运营人员', '负责对账单的审核工作', 1, '2024-11-26 16:03:42', '2024-12-05 01:07:55');
INSERT INTO `eb_role`
VALUES (3, '操作员', '负责日常数据维护工作', 1, '2024-11-26 16:03:42', '2024-11-26 16:03:42');
INSERT INTO `eb_role`
VALUES (1865739095018901504, 'cao', 'cao', 1, '2024-12-08 20:43:58', '2024-12-08 20:43:58');
INSERT INTO `eb_role`
VALUES (1865739385931657216, 'cao', 'cao', 1, '2024-12-08 20:45:13', '2024-12-08 20:45:13');
INSERT INTO `eb_role`
VALUES (1865741851934912512, 'xccc', '财政', 1, '2024-12-08 20:54:55', '2024-12-08 20:54:55');
INSERT INTO `eb_role`
VALUES (1865742330161065984, 'test', 'test', 1, '2024-12-08 20:56:49', '2024-12-08 20:56:49');
INSERT INTO `eb_role`
VALUES (1866859201658155008, '测试角色1', '测试', 1, '2024-12-11 22:54:52', '2024-12-11 22:54:52');

-- ----------------------------
-- Table structure for eb_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `eb_role_permission`;
CREATE TABLE `eb_role_permission`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT,
    `role_id`       bigint(20) NOT NULL COMMENT '角色ID',
    `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
    `created_at`    datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `permission_id` (`permission_id`) USING BTREE,
    INDEX `idx_role_permission` (`role_id`, `permission_id`) USING BTREE,
    CONSTRAINT `eb_role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `eb_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `eb_role_permission_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `eb_permission` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  AUTO_INCREMENT = 88
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_role_permission
-- ----------------------------
INSERT INTO `eb_role_permission`
VALUES (1, 1, 1, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (2, 1, 2, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (3, 1, 3, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (4, 1, 4, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (5, 1, 5, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (6, 1, 6, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (7, 1, 7, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (8, 1, 8, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (9, 1, 9, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (10, 1, 10, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (11, 1, 11, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (12, 1, 12, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (13, 1, 13, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (14, 1, 14, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (15, 1, 15, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (16, 1, 16, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (17, 1, 17, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (18, 1, 18, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (19, 1, 19, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (20, 1, 20, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (21, 1, 21, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (22, 2, 1, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (23, 2, 7, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (24, 2, 8, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (25, 2, 9, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (26, 2, 10, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (27, 3, 1, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (28, 3, 2, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (29, 3, 3, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (30, 3, 11, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (31, 3, 12, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (32, 3, 16, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (33, 3, 17, '2024-11-26 16:03:42');
INSERT INTO `eb_role_permission`
VALUES (68, 1865739095018901504, 1, '2024-12-08 20:43:58');
INSERT INTO `eb_role_permission`
VALUES (69, 1865739095018901504, 2, '2024-12-08 20:43:58');
INSERT INTO `eb_role_permission`
VALUES (70, 1865739385931657216, 1, '2024-12-08 20:45:52');
INSERT INTO `eb_role_permission`
VALUES (71, 1865739385931657216, 2, '2024-12-08 20:45:52');
INSERT INTO `eb_role_permission`
VALUES (72, 1865741851934912512, 3, '2024-12-08 20:54:55');
INSERT INTO `eb_role_permission`
VALUES (73, 1865741851934912512, 5, '2024-12-08 20:54:55');
INSERT INTO `eb_role_permission`
VALUES (74, 1865742330161065984, 4, '2024-12-08 20:56:49');
INSERT INTO `eb_role_permission`
VALUES (75, 1865742330161065984, 5, '2024-12-08 20:56:49');
INSERT INTO `eb_role_permission`
VALUES (76, 1866859201658155008, 2, '2024-12-11 22:54:52');
INSERT INTO `eb_role_permission`
VALUES (77, 1866859201658155008, 3, '2024-12-11 22:54:52');
INSERT INTO `eb_role_permission`
VALUES (78, 1866859201658155008, 4, '2024-12-11 22:54:52');
INSERT INTO `eb_role_permission`
VALUES (79, 1, 22, '2024-12-11 23:25:54');
INSERT INTO `eb_role_permission`
VALUES (80, 1, 31, '2024-12-11 23:25:54');
INSERT INTO `eb_role_permission`
VALUES (81, 1, 32, '2024-12-11 23:25:54');
INSERT INTO `eb_role_permission`
VALUES (82, 1, 33, '2024-12-11 23:25:54');
INSERT INTO `eb_role_permission`
VALUES (83, 1, 34, '2024-12-11 23:25:54');
INSERT INTO `eb_role_permission`
VALUES (84, 1, 35, '2024-12-11 23:25:54');
INSERT INTO `eb_role_permission`
VALUES (85, 1, 36, '2024-12-11 23:25:54');
INSERT INTO `eb_role_permission`
VALUES (86, 1, 37, '2024-12-11 23:25:54');
INSERT INTO `eb_role_permission`
VALUES (87, 1, 38, '2024-12-11 23:25:54');

-- ----------------------------
-- Table structure for eb_system_log
-- ----------------------------
DROP TABLE IF EXISTS `eb_system_log`;
CREATE TABLE `eb_system_log`
(
    `id`             bigint(20)                                               NOT NULL AUTO_INCREMENT,
    `operator_id`    bigint(20)                                               NOT NULL COMMENT '操作人ID',
    `operator_name`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '操作人姓名',
    `operation_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '操作类型',
    `module`         varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '模块名称',
    `description`    text CHARACTER SET utf8 COLLATE utf8_general_ci          NOT NULL COMMENT '操作描述',
    `request_params` text CHARACTER SET utf8 COLLATE utf8_general_ci          NULL COMMENT '请求参数',
    `response_data`  text CHARACTER SET utf8 COLLATE utf8_general_ci          NULL COMMENT '响应数据',
    `ip`             varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT 'IP地址',
    `user_agent`     varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '用户代理',
    `status`         varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '操作状态:success/fail',
    `error_msg`      text CHARACTER SET utf8 COLLATE utf8_general_ci          NULL COMMENT '错误信息',
    `created_at`     datetime                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `request_body`   varchar(4000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_operator_id` (`operator_id`) USING BTREE,
    INDEX `idx_operation_type` (`operation_type`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1871825613245997059
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_system_log
-- ----------------------------

-- ----------------------------
-- Table structure for eb_user
-- ----------------------------
DROP TABLE IF EXISTS `eb_user`;
CREATE TABLE `eb_user`
(
    `id`                bigint(20)                                              NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`          varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '用户名',
    `phone`             varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '电话',
    `address`           varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '地址',
    `meter_no`          varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '电表编号',
    `user_type`         varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '用户类型:居民用户/商业用户',
    `account_status`    varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '正常' COMMENT '账号状态:正常/欠费/停用',
    `balance`           decimal(10, 2)                                          NULL     DEFAULT 0.00 COMMENT '电费余额',
    `electricity_usage` decimal(10, 2)                                          NULL     DEFAULT 0.00 COMMENT '用电量',
    `last_payment_date` datetime                                                NULL     DEFAULT NULL COMMENT '最近缴费时间',
    `created_at`        datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        datetime                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `meter_no` (`meter_no`) USING BTREE,
    INDEX `idx_phone` (`phone`) USING BTREE,
    INDEX `idx_meter_no` (`meter_no`) USING BTREE,
    INDEX `idx_user_type` (`user_type`) USING BTREE,
    INDEX `idx_account_status` (`account_status`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 32
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of eb_user
-- ----------------------------
INSERT INTO `eb_user`
VALUES (1, '张三', '13812345678', '上海市黄浦区人民广场123号', 'METER1001', '商业用户', '正常', 1380.50, 500.00,
        '2023-05-01 10:00:00', '2024-11-26 16:03:40', '2024-12-02 14:59:27');
INSERT INTO `eb_user`
VALUES (2, '李四', '13912345678', '上海市徐汇区虹桥路456号', 'METER1002', '商业用户', '正常', 50.00, 800.00,
        '2023-04-15 15:30:00', '2024-11-26 16:03:40', '2024-11-26 16:03:40');
INSERT INTO `eb_user`
VALUES (3, '王五', '13712345678', '上海市长宁区延安西路789号', 'METER1003', '居民用户', '正常', 300.00, 200.00,
        '2023-05-10 09:15:00', '2024-11-26 16:03:40', '2024-11-26 16:03:40');
INSERT INTO `eb_user`
VALUES (6, '丽丽', '1231231312', '3123123123', '3213', '居民用户', '正常', 0.00, 0.00, '2024-12-02 14:30:34',
        '2024-12-02 14:30:34', '2024-12-02 14:30:34');
INSERT INTO `eb_user`
VALUES (7, '哈', '123123', '123123123', '12313', '居民用户', '欠费', 0.00, 0.00, '2024-12-02 14:31:12',
        '2024-12-02 14:31:12', '2024-12-02 14:31:12');
INSERT INTO `eb_user`
VALUES (13, '黄大大', '13812345679', '23', '123', '居民用户', '正常', 0.00, 0.00, '2024-12-02 15:00:51',
        '2024-12-02 15:00:51', '2024-12-02 15:00:51');
INSERT INTO `eb_user`
VALUES (14, '黄', '13812345672', 'sadas', 'dsadasd123', '居民用户', '正常', 0.00, 0.00, '2024-12-02 15:01:13',
        '2024-12-02 15:01:13', '2024-12-02 15:01:13');
INSERT INTO `eb_user`
VALUES (15, 'xxx', '17322516597', 'zzz', '111', '居民用户', '正常', 0.20, 0.00, '2024-12-11 14:37:23',
        '2024-12-11 14:37:23', '2024-12-11 14:37:23');
INSERT INTO `eb_user`
VALUES (16, '用户', '17322516592', '11', '11', '居民用户', '正常', 0.20, 0.00, '2024-12-11 16:15:10',
        '2024-12-11 16:15:09', '2024-12-11 16:15:09');
INSERT INTO `eb_user`
VALUES (17, '用户123', '17322516799', 'xx', 'mm', '居民用户', '正常', 0.10, 0.00, '2024-12-11 16:20:41',
        '2024-12-11 16:20:41', '2024-12-11 16:20:41');
INSERT INTO `eb_user`
VALUES (20, '用户123412313123', '17322516798', 'xx', '123333', '居民用户', '正常', 0.10, 0.00, '2024-12-11 16:30:29',
        '2024-12-11 16:30:29', '2024-12-11 16:30:29');
INSERT INTO `eb_user`
VALUES (21, '大撒大撒大撒大大', '17322516900', '黄大大收到', '123073', '居民用户', '正常', 0.20, 0.00,
        '2024-12-11 16:32:56', '2024-12-11 16:32:55', '2024-12-11 16:32:55');
INSERT INTO `eb_user`
VALUES (22, '大撒大撒大撒大大', '17322516901', '黄大大收到', '1230730', '居民用户', '正常', 0.20, 0.00,
        '2024-12-11 16:33:54', '2024-12-11 16:33:53', '2024-12-11 16:33:53');
INSERT INTO `eb_user`
VALUES (23, '中大大大', '13538929311', 'iiiiii', 'i3123', '居民用户', '正常', 0.00, 0.00, '2024-12-11 16:34:25',
        '2024-12-11 16:34:25', '2024-12-11 16:34:25');
INSERT INTO `eb_user`
VALUES (24, '爱神的箭阿三就大数据的撒娇', '17321232334', '阿加达斯健康的', '1231231451', '居民用户', '正常', 0.10, 0.00,
        '2024-12-11 16:49:50', '2024-12-11 16:49:49', '2024-12-11 16:49:49');
INSERT INTO `eb_user`
VALUES (25, '黄大大大大大', '13123213123', '阿斯顿撒旦大苏打大大', '213345667', '居民用户', '欠费', 0.10, 0.00,
        '2024-12-11 17:08:47', '2024-12-11 17:08:47', '2024-12-11 17:08:47');
INSERT INTO `eb_user`
VALUES (28, '黄大大大大大', '13123213155', '阿斯顿撒旦大苏打大大', '213345', '居民用户', '欠费', 0.10, 0.00,
        '2024-12-11 17:40:55', '2024-12-11 17:40:55', '2024-12-11 17:40:55');
INSERT INTO `eb_user`
VALUES (29, '黄大大收到撒大苏打撒旦撒旦', '12312312233', '实打实打算公司的发顺丰', '000000001', '居民用户', '正常',
        0.10, 0.00, '2024-12-11 17:45:16', '2024-12-11 17:45:16', '2024-12-11 17:45:16');
INSERT INTO `eb_user`
VALUES (30, '就就居民', '31123123123', '九女经济学', '1020202', '居民用户', '正常', 0.00, 0.00, '2024-12-11 17:47:02',
        '2024-12-11 17:47:02', '2024-12-11 17:47:02');
INSERT INTO `eb_user`
VALUES (31, 'huangdada', '18231321312', 'HUANDADASD', 'MMADSA', '居民用户', '正常', 0.10, 0.00, '2024-12-12 19:17:03',
        '2024-12-12 19:17:03', '2024-12-12 19:17:03');

SET FOREIGN_KEY_CHECKS = 1;
