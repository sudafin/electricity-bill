create database electricity_bill;
use electricity_bill;

-- 1. 用户表
CREATE TABLE eb_user
(
    id                BIGINT PRIMARY KEY Auto_Increment NOT Null comment '用户ID',
    username          VARCHAR(50)        NOT NULL COMMENT '用户名',
    phone             VARCHAR(20)        NOT NULL COMMENT '电话',
    email             VARCHAR(100) COMMENT '邮箱',
    password          VARCHAR(100)       NOT NULL COMMENT '密码',
    address           VARCHAR(255)       NOT NULL COMMENT '地址',
    meter_no          VARCHAR(50) UNIQUE NOT NULL COMMENT '电表编号',
    user_type         VARCHAR(20)        NOT NULL COMMENT '用户类型:居民用户/商业用户',
    account_status    VARCHAR(20)        NOT NULL DEFAULT '正常' COMMENT '账号状态:正常/欠费/停用',
    balance           DECIMAL(10, 2)              DEFAULT 0 COMMENT '电费余额',
    electricity_usage DECIMAL(10, 2)              DEFAULT 0 COMMENT '用电量',
    last_payment_date DATETIME COMMENT '最近缴费时间',
    created_at        DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_meter_no (meter_no),
    INDEX idx_user_type (user_type),
    INDEX idx_account_status (account_status)
);
-- 8. 管理员表
CREATE TABLE eb_admin
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    username        VARCHAR(50)  NOT NULL COMMENT '用户名',
    password        VARCHAR(100) NOT NULL COMMENT '密码',
    email           VARCHAR(100) COMMENT '邮箱',
    phone           VARCHAR(20) COMMENT '电话',
    role_id         BIGINT       NOT NULL COMMENT '角色ID',
    status          TINYINT               DEFAULT 1 COMMENT '状态:0禁用/1启用',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES eb_role (id),
    INDEX idx_username (username),
    INDEX idx_status (status)
);

-- 9. 角色表
CREATE TABLE eb_role
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name  VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_desc  VARCHAR(255) COMMENT '角色描述',
    status     TINYINT              DEFAULT 1 COMMENT '状态:0禁用/1启用',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role_name (role_name),
    INDEX idx_status (status)
);

-- 10. 权限表
CREATE TABLE eb_permission
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(50) NOT NULL COMMENT '权限编码',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型:menu/action',
    parent_id       BIGINT COMMENT '父权限ID',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_permission_code (permission_code),
    INDEX idx_permission_type (permission_type)
);

-- 11. 角色权限关联表
CREATE TABLE eb_role_permission
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id       BIGINT   NOT NULL COMMENT '角色ID',
    permission_id BIGINT   NOT NULL COMMENT '权限ID',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES eb_role (id),
    FOREIGN KEY (permission_id) REFERENCES eb_permission (id),
    INDEX idx_role_permission (role_id, permission_id)
);

-- 12. 系统日志表
CREATE TABLE eb_system_log
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id    BIGINT      NOT NULL COMMENT '操作人ID',
    operator_name  VARCHAR(50) NOT NULL COMMENT '操作人姓名',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    module         VARCHAR(50) NOT NULL COMMENT '模块名称',
    description    TEXT        NOT NULL COMMENT '操作描述',
    request_params TEXT COMMENT '请求参数',
    response_data  TEXT COMMENT '响应数据',
    ip             VARCHAR(50) NOT NULL COMMENT 'IP地址',
    user_agent     VARCHAR(255) COMMENT '用户代理',
    status         VARCHAR(20) NOT NULL COMMENT '操作状态:success/fail',
    error_msg      TEXT COMMENT '错误信息',
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operator_id (operator_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_status (status)
);

-- 2. 电费费率表
CREATE TABLE eb_rate
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    rate_name      VARCHAR(50)    NOT NULL COMMENT '费率名称',
    user_type      VARCHAR(20)    NOT NULL COMMENT '用户类型',
    price          DECIMAL(10, 2) NOT NULL COMMENT '每度电费价格',
    start_time     TIME           NOT NULL COMMENT '开始时间',
    end_time       TIME           NOT NULL COMMENT '结束时间',
    peak_price     DECIMAL(10, 2) COMMENT '峰时价格',
    flat_price     DECIMAL(10, 2) COMMENT '平时价格',
    valley_price   DECIMAL(10, 2) COMMENT '谷时价格',
    status         TINYINT                 DEFAULT 1 COMMENT '状态:0禁用/1启用',
    effective_date DATE           NOT NULL COMMENT '生效日期',
    expire_date    DATE COMMENT '失效日期',
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_type (user_type),
    INDEX idx_status (status)
);

-- 3. 支付记录表  
CREATE TABLE eb_payment
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT             NOT NULL COMMENT '用户ID',
    bill_id        BIGINT COMMENT '账单ID',
    order_no       VARCHAR(50) UNIQUE NOT NULL COMMENT '订单号',
    amount         DECIMAL(10, 2)     NOT NULL COMMENT '支付金额',
    payment_method VARCHAR(20)        NOT NULL COMMENT '支付方式',
    transaction_no VARCHAR(100) COMMENT '支付流水号',
    status         VARCHAR(20)        NOT NULL COMMENT '支付状态:pending/success/failed',
    failure_reason VARCHAR(255) COMMENT '失败原因',
    payment_time   DATETIME COMMENT '支付时间',
    refund_status  VARCHAR(20)                 DEFAULT 'none' COMMENT '退款状态:none/processing/success/failed',
    refund_amount  DECIMAL(10, 2) COMMENT '退款金额',
    refund_time    DATETIME COMMENT '退款时间',
    created_at     DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES eb_user (id),
    INDEX idx_user_payment (user_id, payment_time),
    INDEX idx_bill_id (bill_id),
    INDEX idx_status (status),
    INDEX idx_refund_status (refund_status)
);

-- 4. 用电记录表
CREATE TABLE eb_electricity_usage
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT         NOT NULL COMMENT '用户ID',
    meter_no     VARCHAR(50)    NOT NULL COMMENT '电表编号',
    usage_amount DECIMAL(10, 2) NOT NULL COMMENT '用电量',
    rate_id      BIGINT         NOT NULL COMMENT '费率ID',
    fee_amount   DECIMAL(10, 2) NOT NULL COMMENT '电费金额',
    start_time   DATETIME       NOT NULL COMMENT '用电开始时间',
    end_time     DATETIME       NOT NULL COMMENT '用电结束时间',
    time_segment VARCHAR(10)    NOT NULL COMMENT '用电时段:peak/flat/valley',
    created_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES eb_user (id),
    FOREIGN KEY (rate_id) REFERENCES eb_rate (id),
    INDEX idx_user_usage (user_id, start_time),
    INDEX idx_meter_no (meter_no),
    INDEX idx_time_segment (time_segment)
);

-- 5. 对账单表
CREATE TABLE eb_reconciliation
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    reconciliation_no VARCHAR(50) UNIQUE NOT NULL COMMENT '对账单号',
    user_id           BIGINT             NOT NULL COMMENT '用户ID',
    start_date        DATE               NOT NULL COMMENT '开始日期',
    end_date          DATE               NOT NULL COMMENT '结束日期',
    total_usage       DECIMAL(10, 2)     NOT NULL COMMENT '总用电量',
    total_amount      DECIMAL(10, 2)     NOT NULL COMMENT '总金额',
    status            VARCHAR(20)        NOT NULL COMMENT '状态:pending/completed',
    payment_status    VARCHAR(20)        NOT NULL COMMENT '支付状态:unpaid/paid',
    approver_id       BIGINT COMMENT '审批人ID',
    approval_time     DATETIME COMMENT '审批时间',
    comment           TEXT COMMENT '审批意见',
    created_at        DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES eb_user (id),
    FOREIGN KEY (approver_id) REFERENCES eb_admin (id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_payment_status (payment_status)
);

-- 6. 通知表
CREATE TABLE eb_notification
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(100) NOT NULL COMMENT '标题',
    content     TEXT         NOT NULL COMMENT '内容',
    type        VARCHAR(20)  NOT NULL COMMENT '类型:system/approval',
    level       VARCHAR(20)  NOT NULL COMMENT '级别:low/medium/high',
    sender_id   BIGINT       NOT NULL COMMENT '发送人ID',
    expire_time DATETIME COMMENT '过期时间',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES eb_admin (id),
    INDEX idx_type (type),
    INDEX idx_level (level)
);

-- 7. 通知接收表
CREATE TABLE eb_notification_recipient
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    notification_id BIGINT      NOT NULL COMMENT '通知ID',
    recipient_type  VARCHAR(20) NOT NULL COMMENT '接收对象类型:user/admin',
    recipient_id    BIGINT      NOT NULL COMMENT '接收者ID',
    read_status     TINYINT              DEFAULT 0 COMMENT '阅读状态:0未读/1已读',
    read_time       DATETIME COMMENT '阅读时间',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES eb_notification (id),
    INDEX idx_recipient (recipient_type, recipient_id),
    INDEX idx_read_status (read_status)
);

-- 插入用户数据
INSERT INTO eb_user (username, phone, email, password, address, meter_no, user_type, account_status, balance, electricity_usage, last_payment_date)
VALUES
    ('张三', '13812345678', 'zhangsan@example.com', 'password123', '上海市黄浦区人民广场123号', 'METER1001', '居民用户', '正常', 1000.50, 500.00, '2023-05-01 10:00:00'),
    ('李四', '13912345678', 'lisi@example.com', 'password456', '上海市徐汇区虹桥路456号', 'METER1002', '商业用户', '欠费', -200.00, 800.00, '2023-04-15 15:30:00'),
    ('王五', '13712345678', 'wangwu@example.com', 'password789', '上海市长宁区延安西路789号', 'METER1003', '居民用户', '正常', 300.00, 200.00, '2023-05-10 09:15:00');

-- 插入电费费率数据
INSERT INTO eb_rate (rate_name, user_type, price, start_time, end_time, peak_price, flat_price, valley_price, status, effective_date, expire_date)
VALUES
    ('居民电价1', '居民用户', 0.50, '09:00:00', '12:00:00', 0.80, 0.50, 0.30, 1, '2023-01-01', '2023-12-31'),
    ('商业电价1', '商业用户', 1.00, '08:00:00', '22:00:00', 1.50, 1.00, 0.80, 1, '2023-01-01', '2023-06-30'),
    ('居民电价2', '居民用户', 0.60, '12:00:00', '18:00:00', 0.90, 0.60, 0.40, 1, '2023-07-01', NULL);

-- 插入支付记录数据
INSERT INTO eb_payment (user_id, bill_id, order_no, amount, payment_method, transaction_no, status, payment_time)
VALUES
    (1, 1, 'ORDER1001', 500.00, '支付宝', 'TRANS1001', 'success', '2023-05-01 10:00:00'),
    (2, 2, 'ORDER1002', 800.00, '微信', 'TRANS1002', 'success', '2023-04-15 15:30:00'),
    (1, 3, 'ORDER1003', 200.00, '银行卡', 'TRANS1003', 'pending', NULL);

-- 插入用电记录数据
INSERT INTO eb_electricity_usage (user_id, meter_no, usage_amount, rate_id, fee_amount, start_time, end_time, time_segment)
VALUES
    (1, 'METER1001', 100.00, 1, 50.00, '2023-05-01 09:00:00', '2023-05-01 12:00:00', 'peak'),
    (1, 'METER1001', 200.00, 1, 100.00, '2023-05-01 12:00:00', '2023-05-01 18:00:00', 'flat'),
    (2, 'METER1002', 500.00, 2, 500.00, '2023-04-15 08:00:00', '2023-04-15 22:00:00', 'peak'),
    (3, 'METER1003', 150.00, 1, 75.00, '2023-05-10 09:00:00', '2023-05-10 12:00:00', 'peak');

-- 插入对账单数据
INSERT INTO eb_reconciliation (reconciliation_no, user_id, start_date, end_date, total_usage, total_amount, status, payment_status)
VALUES
    ('REC1001', 1, '2023-05-01', '2023-05-31', 500.00, 250.00, 'pending', 'unpaid'),
    ('REC1002', 2, '2023-04-01', '2023-04-30', 800.00, 800.00, 'completed', 'paid'),
    ('REC1003', 3, '2023-05-01', '2023-05-31', 200.00, 100.00, 'pending', 'unpaid');

-- 插入通知数据
INSERT INTO eb_notification (title, content, type, level, sender_id, expire_time)
VALUES
    ('系统升级通知', '系统将于2023年6月1日凌晨2点至6点进行升级维护,届时将无法登录和使用,请提前做好准备。', 'system', 'high', 1, '2023-06-01 06:00:00'),
    ('电费缴纳提醒', '您的电费账单已生成,请及时登录系统查看并缴纳,以免影响正常用电。', 'system', 'medium', 1, '2023-06-10 23:59:59'),
    ('对账单审批通过通知', '您提交的2023年4月对账单已审批通过,感谢您的配合。', 'approval', 'low', 2, NULL);

-- 插入通知接收数据
INSERT INTO eb_notification_recipient (notification_id, recipient_type, recipient_id, read_status, read_time)
VALUES
    (1, 'user', 1, 0, NULL),
    (1, 'user', 2, 0, NULL),
    (1, 'user', 3, 1, '2023-05-20 10:00:00'),
    (2, 'user', 1, 0, NULL),
    (2, 'user', 3, 0, NULL),
    (3, 'admin', 1, 1, '2023-05-15 15:30:00');

-- 插入管理员数据
INSERT INTO eb_admin (username, password, email, phone, role_id, status, last_login_time)
VALUES
    ('admin', 'password', 'admin@example.com', '13012345678', 1, 1, '2023-05-20 09:00:00'),
    ('auditor', 'password', 'auditor@example.com', '13112345678', 2, 1, '2023-05-19 16:30:00'),
    ('operator', 'password', 'operator@example.com', '13212345678', 3, 0, '2023-05-01 11:00:00');

-- 插入角色数据
INSERT INTO eb_role (role_name, role_desc, status)
VALUES
    ('系统管理员', '拥有系统所有权限', 1),
    ('审核员', '负责对账单的审核工作', 1),
    ('操作员', '负责日常数据维护工作', 1);

-- 插入权限数据
INSERT INTO eb_permission (permission_name, permission_code, permission_type, parent_id)
VALUES
    ('首页', 'dashboard', 'menu', NULL),
    ('用户管理', 'user', 'menu', NULL),
    ('用户列表', 'user:list', 'action', 2),
    ('新增用户', 'user:create', 'action', 2),
    ('编辑用户', 'user:edit', 'action', 2),
    ('删除用户', 'user:delete', 'action', 2),
    ('数据统计与报表', 'report', 'menu', NULL),
    ('对账与审批', 'reconciliation', 'menu', NULL),
    ('对账列表', 'reconciliation:list', 'action', 8),
    ('审批对账单', 'reconciliation:approve', 'action', 8),
    ('通知和提醒', 'notification', 'menu', NULL),
    ('通知列表', 'notification:list', 'action', 11),
    ('新增通知', 'notification:create', 'action', 11),
    ('编辑通知', 'notification:edit', 'action', 11),
    ('删除通知', 'notification:delete', 'action', 11),
    ('支付管理', 'payment', 'menu', NULL),
    ('支付列表', 'payment:list', 'action', 16),
    ('系统设置', 'setting', 'menu', NULL),
    ('角色权限管理', 'setting:role', 'action', 18),
    ('费率管理', 'setting:rate', 'action', 18),
    ('日志管理', 'setting:log', 'action', 18);

-- 插入角色权限关联数据
INSERT INTO eb_role_permission (role_id, permission_id)
VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
    (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18), (1, 19), (1, 20), (1, 21),
    (2, 1), (2, 7), (2, 8), (2, 9), (2, 10),
    (3, 1), (3, 2), (3, 3), (3, 11), (3, 12), (3, 16), (3, 17);

-- 插入系统日志数据
INSERT INTO eb_system_log (operator_id, operator_name, operation_type, module, description, request_params, response_data, ip, user_agent, status)
VALUES
    (1, 'admin', '登录', '系统管理', '管理员admin登录系统', '{"username":"admin","password":"password"}', '{"code":200,"message":"登录成功","data":{"token":"abc123"}}', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36', 'success'),
    (2, 'auditor', '审批', '对账与审批', '审核员auditor审批了对账单REC1002', '{"id":2,"status":"completed"}', '{"code":200,"message":"审批成功"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36', 'success'),
    (3, 'operator', '新增', '用户管理', '操作员operator新增了用户张三', '{"username":"张三","phone":"13812345678","address":"上海市黄浦区人民广场123号","meterNo":"METER1001","userType":"居民用户"}', '{"code":200,"message":"新增成功","data":{"id":1}}', '192.168.1.200', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36', 'success');