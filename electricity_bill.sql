create table eb_admin
(
    id              bigint auto_increment comment '管理员ID'
        primary key,
    account         varchar(50)                        not null comment '账号',
    password        varchar(100)                       not null comment '密码',
    email           varchar(100)                       null comment '邮箱',
    phone           varchar(20)                        null comment '电话',
    role_id         bigint                             not null comment '角色ID',
    status          tinyint  default 1                 null comment '状态: 0禁用/1启用',
    last_login_time datetime                           null comment '最后登录时间',
    created_at      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    admin_name      varchar(50)                        not null
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_account
    on eb_admin (account);

create index idx_status
    on eb_admin (status);

create index role_id
    on eb_admin (role_id);

create table eb_announcement
(
    id         bigint auto_increment comment '公告ID'
        primary key,
    title      varchar(100)                          not null comment '标题',
    content    text                                  not null comment '内容',
    start_time datetime                              not null comment '开始时间',
    end_time   datetime                              null comment '结束时间',
    status     varchar(20) default '有效'            not null comment '状态: 有效/过期',
    created_at datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_status
    on eb_announcement (status);

create index idx_time
    on eb_announcement (start_time, end_time);

create table eb_bill
(
    id             bigint auto_increment comment '账单ID'
        primary key,
    user_id        bigint                                not null comment '用户ID',
    usage_amount   decimal(10, 2)                        not null comment '用电量（度）',
    total_amount   decimal(10, 2)                        not null comment '总金额',
    status         varchar(20) default '未支付'          not null comment '状态: 未支付/已支付/逾期',
    payment_id     bigint                                null comment '支付记录ID',
    created_at     datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at     datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    payment_method varchar(50)                           not null comment '支付方式',
    due_date       datetime    default CURRENT_TIMESTAMP not null comment '最晚支付时间',
    start_reading  decimal(9, 2)                         not null comment '一个周期内读表开始的度数',
    ending_reading decimal(9, 2)                         not null comment '一个周期内读表现在的度数',
    meter_id       varchar(50)                           not null comment '电表id'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_status
    on eb_bill (status);

create index idx_user_id
    on eb_bill (user_id);

create table eb_electricity_usage
(
    id           bigint auto_increment comment '用电记录ID'
        primary key,
    user_id      bigint                             not null comment '用户ID',
    meter_id     varchar(50)                        not null comment '电表编号',
    usage_amount decimal(10, 2)                     not null comment '用电量（度）',
    rate_id      bigint                             not null comment '费率ID',
    start_time   datetime                           not null comment '用电开始时间',
    end_time     datetime                           not null comment '用电结束时间',
    period_type  varchar(10)                        not null comment '用电时段: peak（峰）/flat（平）/valley（谷）',
    created_at   datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_meter_no
    on eb_electricity_usage (meter_id);

create index idx_time_segment
    on eb_electricity_usage (period_type);

create index idx_user_usage
    on eb_electricity_usage (user_id, start_time);

create index rate_id
    on eb_electricity_usage (rate_id);

create table eb_meter
(
    id                       varchar(50)                             not null comment '电表ID'
        primary key,
    model                    varchar(50)                             null comment '电表型号',
    install_date             date                                    null comment '安装日期',
    install_place            varchar(50)                             null comment '安装位置',
    status                   varchar(20)   default '正常'            not null comment '状态: 正常/故障/停用',
    user_id                  bigint                                  null comment '用户ID',
    last_meter_reading_date  datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '周期最后一次抄表时间',
    start_meter_reading_date datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '周期内开始读表的时间',
    created_at               datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at               datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    start_reading            decimal(9, 2) default 0.00              not null comment '一个周期内读表开始的度数',
    ending_reading           decimal(9, 2) default 0.00              not null comment '一个周期内读表现在的度数',
    inspection_id            bigint                                  not null comment '检查电表的id',
    valid_type               int                                     null comment '有效状态'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_status
    on eb_meter (status);

create index idx_user_id
    on eb_meter (user_id);

create table eb_meter_inspection
(
    id                   bigint auto_increment comment '检测记录ID'
        primary key,
    meter_id             varchar(50)                           not null comment '电表编号',
    inspection_type      varchar(20)                           not null comment '检测类型: routine（常规检查）/fault（故障检修）/calibration（校准）',
    inspection_result    varchar(20)                           null comment '检测结果: normal（正常）/fault（故障）/fixed（已修复）',
    fault_description    varchar(255)                          null comment '故障描述',
    solution             varchar(255)                          null comment '解决方案',
    inspector_id         bigint                                not null comment '检测人员ID',
    inspector_name       varchar(50)                           not null comment '检测人员姓名',
    inspection_time      datetime                              not null comment '检测时间',
    next_inspection_time datetime                              null comment '下次检测时间',
    repair_cost          decimal(10, 2)                        null comment '维修费用',
    user_id              bigint                                null comment '用户ID',
    remark               varchar(255)                          null comment '备注',
    status               varchar(20) default 'completed'       not null comment '状态: pending（待处理）/processing（处理中）/completed（已完成）',
    created_at           datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_inspection_time
    on eb_meter_inspection (inspection_time);

create index idx_inspector_id
    on eb_meter_inspection (inspector_id);

create index idx_meter_no
    on eb_meter_inspection (meter_id);

create index idx_next_inspection_time
    on eb_meter_inspection (next_inspection_time);

create index idx_status
    on eb_meter_inspection (status);

create index idx_user_id
    on eb_meter_inspection (user_id);

create table eb_notification
(
    id          bigint auto_increment comment '通知ID'
        primary key,
    title       varchar(100)                       not null comment '标题',
    content     text                               not null comment '内容',
    type        varchar(20)                        not null comment '类型: feedback（反馈）/billing（账单）/internal（内部）',
    sender_type varchar(20)                        not null comment '发送者类型: system（系统）/admin（管理员）',
    sender_id   bigint                             null comment '发送人ID',
    created_at  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    expire_time datetime default CURRENT_TIMESTAMP not null comment '过期时间',
    valid_type  tinyint  default 0                 not null comment '有效和无效状态'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_type
    on eb_notification (type);

create index sender_id
    on eb_notification (sender_id);

create table eb_notification_recipient
(
    id              bigint auto_increment comment '接收记录ID'
        primary key,
    notification_id bigint                             not null comment '通知ID',
    recipient_type  varchar(20)                        not null comment '接收者类型: user（用户）/admin（管理员）',
    recipient_id    bigint                             not null comment '接收者ID',
    priority        tinyint  default 1                 not null comment '优先级: 1普通/2重要/3紧急',
    read_status     tinyint  default 0                 not null comment '阅读状态: 0未读/1已读',
    read_time       datetime                           null comment '阅读时间',
    created_at      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_notification_id
    on eb_notification_recipient (notification_id);

create index idx_priority
    on eb_notification_recipient (priority);

create index idx_read_status_time
    on eb_notification_recipient (read_status, read_time);

create index idx_recipient
    on eb_notification_recipient (recipient_type, recipient_id);

create table eb_payment
(
    id                bigint auto_increment comment '支付记录ID'
        primary key,
    user_id           bigint                                not null comment '用户ID',
    amount            decimal(10, 2)                        not null comment '支付金额',
    payment_method    varchar(20)                           not null comment '支付方式',
    transaction_no    varchar(100)                          null comment '支付流水号',
    status            varchar(20)                           not null comment '支付状态: pending（待支付）/success（成功）/failed（失败）',
    failure_reason    varchar(255)                          null comment '失败原因',
    payment_time      datetime                              null comment '支付时间',
    refund_status     varchar(20) default 'none'            null comment '退款状态: none（无）/processing（处理中）/success（成功）/failed（失败）',
    refund_amount     decimal(10, 2)                        null comment '退款金额',
    refund_time       datetime                              null comment '退款时间',
    created_at        datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at        datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    remark            varchar(255)                          null comment '备注',
    operator_id       bigint                                not null comment '操作人ID',
    reconciliation_id bigint                                null comment '对账单ID'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_refund_status
    on eb_payment (refund_status);

create index idx_status
    on eb_payment (status);

create index idx_user_payment
    on eb_payment (user_id, payment_time);

create table eb_permission
(
    id              bigint auto_increment comment '权限ID'
        primary key,
    permission_name varchar(50)                        not null comment '权限名称',
    permission_code varchar(50)                        not null comment '权限编码',
    permission_type varchar(20)                        not null comment '权限类型: menu（菜单）/action（操作）',
    parent_id       bigint                             null comment '父权限ID',
    created_at      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_permission_code
    on eb_permission (permission_code);

create index idx_permission_type
    on eb_permission (permission_type);

create table eb_rate
(
    id                bigint auto_increment comment '费率ID'
        primary key,
    user_type         varchar(50)                        not null comment '用户类型',
    peak_price        decimal(10, 4)                     null comment '峰时电价(元/度)',
    flat_price        decimal(10, 4)                     null comment '平时电价(元/度)',
    valley_price      decimal(10, 4)                     null comment '谷时电价(元/度)',
    summer_peak_price decimal(10, 4)                     null comment '夏季尖峰电价(元/度)',
    peak_start        time                               null comment '峰时段开始时间',
    peak_end          time                               null comment '峰时段结束时间',
    valley_start      time                               null comment '谷时段开始时间',
    valley_end        time                               null comment '谷时段结束时间',
    summer_period     varchar(20)                        null comment '夏季时段(如"06-01至08-31")',
    status            tinyint  default 1                 not null comment '状态: 0禁用/1启用',
    effective_date    date                               not null comment '生效日期',
    expire_date       date                               null comment '失效日期',
    created_by        varchar(50)                        null comment '创建人',
    updated_by        varchar(50)                        null comment '更新人',
    created_at        datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    discount          decimal(8, 4)                      null comment '优化率'
)
    comment '电价费率表' charset = utf8
                         row_format = DYNAMIC;

create index idx_date_range
    on eb_rate (effective_date, expire_date);

create index idx_status
    on eb_rate (status);

create index idx_user_type
    on eb_rate (user_type);

create table eb_reconciliation
(
    id             bigint auto_increment comment '对账单ID'
        primary key,
    user_id        bigint                             not null comment '用户ID',
    start_date     date                               not null comment '开始日期',
    end_date       date                               not null comment '结束日期',
    total_usage    decimal(10, 2)                     not null comment '总用电量',
    total_amount   decimal(10, 2)                     not null comment '总金额',
    status         varchar(20)                        not null comment '状态: pending（待处理）/completed（已完成）',
    payment_status varchar(20)                        not null comment '支付状态: unpaid（未支付）/paid（已支付）',
    approver_id    bigint                             null comment '审批人ID',
    approval_time  datetime                           null comment '审批时间',
    comment        text                               null comment '审批意见',
    created_at     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    payment_id     bigint                             null comment '支付记录ID'
)
    charset = utf8
    row_format = DYNAMIC;

create index approver_id
    on eb_reconciliation (approver_id);

create index idx_payment_status
    on eb_reconciliation (payment_status);

create index idx_status
    on eb_reconciliation (status);

create index idx_user_id
    on eb_reconciliation (user_id);

create table eb_role
(
    id         bigint auto_increment comment '角色ID'
        primary key,
    role_name  varchar(50)                        not null comment '角色名称',
    role_desc  varchar(255)                       null comment '角色描述',
    status     tinyint  default 1                 null comment '状态: 0禁用/1启用',
    created_at datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_role_name
    on eb_role (role_name);

create index idx_status
    on eb_role (status);

create table eb_role_permission
(
    id            bigint auto_increment comment '角色权限ID'
        primary key,
    role_id       bigint                             not null comment '角色ID',
    permission_id bigint                             not null comment '权限ID',
    created_at    datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_role_permission
    on eb_role_permission (role_id, permission_id);

create index permission_id
    on eb_role_permission (permission_id);

create table eb_scheduled_task
(
    id              bigint auto_increment comment '任务ID'
        primary key,
    task_name       varchar(100)                       not null comment '任务名称',
    task_type       varchar(30)                        not null comment '任务类型: notification（通知）/report（报表）/billing（账单生成）/meter_reading（抄表）/reminder（催缴）',
    task_desc       varchar(255)                       null comment '任务描述',
    cron_expression varchar(50)                        not null comment 'Cron表达式',
    task_status     tinyint  default 1                 null comment '状态: 0停用/1启用',
    created_by      bigint                             null comment '创建人ID',
    created_at      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_task_status
    on eb_scheduled_task (task_status);

create index idx_task_type
    on eb_scheduled_task (task_type);

create table eb_system_log
(
    id             bigint auto_increment comment '日志ID'
        primary key,
    operator_id    bigint                             not null comment '操作人ID',
    operator_name  varchar(50)                        not null comment '操作人姓名',
    operation_type varchar(50)                        not null comment '操作类型',
    module         varchar(50)                        not null comment '模块名称',
    description    text                               not null comment '操作描述',
    request_params text                               null comment '请求参数',
    response_data  text                               null comment '响应数据',
    ip             varchar(50)                        not null comment 'IP地址',
    user_agent     varchar(255)                       null comment '用户代理',
    status         varchar(20)                        not null comment '操作状态: success（成功）/fail（失败）',
    error_msg      text                               null comment '错误信息',
    created_at     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    request_body   varchar(4000)                      null comment '请求体'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_operation_type
    on eb_system_log (operation_type);

create index idx_operator_id
    on eb_system_log (operator_id);

create index idx_status
    on eb_system_log (status);

create table eb_usage_summary
(
    id                 bigint auto_increment comment '汇总ID'
        primary key,
    user_id            bigint                                   not null comment '用户ID',
    meter_id           varchar(50)                              not null comment '电表编号',
    summary_date_start date                                     not null comment '汇总日期始',
    peak_usage         decimal(10, 2) default 0.00              not null comment '峰时段用电量(度)',
    flat_usage         decimal(10, 2) default 0.00              not null comment '平时段用电量(度)',
    valley_usage       decimal(10, 2) default 0.00              not null comment '谷时段用电量(度)',
    total_usage        decimal(10, 2) as (((`peak_usage` + `flat_usage`) + `valley_usage`)) stored comment '总用电量(度)',
    peak_cost          decimal(10, 2) default 0.00              not null comment '峰时段电费(元)',
    flat_cost          decimal(10, 2) default 0.00              not null comment '平时段电费(元)',
    valley_cost        decimal(10, 2) default 0.00              not null comment '谷时段电费(元)',
    total_cost         decimal(10, 2) as (((`peak_cost` + `flat_cost`) + `valley_cost`)) stored comment '总电费(元)',
    created_at         datetime       default CURRENT_TIMESTAMP not null comment '创建时间',
    summary_date_end   datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '汇总的日期末',
    date_type          varchar(50)                              null comment '用来表示当前是日的数据或者是月或者是年的数据',
    constraint idx_user_meter_date
        unique (user_id, meter_id, summary_date_start)
)
    comment '每日用电量及电费汇总表' charset = utf8
                                     row_format = DYNAMIC;

create index idx_date
    on eb_usage_summary (summary_date_start);

create index idx_user
    on eb_usage_summary (user_id);

create table eb_user
(
    id                  bigint auto_increment comment '用户ID'
        primary key,
    account             varchar(50)                           not null comment '账号',
    password            varchar(100)                          not null comment '密码',
    username            varchar(50)                           not null comment '用户名',
    phone               varchar(20)                           not null comment '电话',
    address             varchar(255)                          not null comment '地址',
    meter_id            varchar(50)                           not null comment '电表编号',
    user_type           varchar(20)                           not null comment '用户类型: 居民用户/商业用户',
    account_status      varchar(20) default '正常'            not null comment '账号状态: 正常/欠费/停用',
    id_card_no          varchar(50)                           null comment '身份证号',
    last_payment_date   datetime                              null comment '最近缴费时间',
    contract_start_date datetime                              null comment '合同开始日期',
    contract_end_date   datetime                              null comment '合同结束日期',
    created_at          datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at          datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    valid_type          tinyint     default 1                 not null comment '有效',
    constraint account
        unique (account),
    constraint meter_no
        unique (meter_id)
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_account_status
    on eb_user (account_status);

create index idx_meter_no
    on eb_user (meter_id);

create index idx_phone
    on eb_user (phone);

create index idx_user_type
    on eb_user (user_type);

create table eb_user_feedback
(
    id              bigint auto_increment comment '反馈ID'
        primary key,
    user_id         bigint                                not null comment '用户ID',
    feedback_type   varchar(20)                           not null comment '反馈类型: complaint（投诉）/suggestion（建议）/question（问题）',
    content         text                                  not null comment '反馈内容',
    feedback_status varchar(20) default 'pending'         not null comment '状态: pending（待处理）/processed（已处理）/closed（已关闭）',
    submit_time     datetime    default CURRENT_TIMESTAMP not null comment '提交时间',
    process_time    datetime                              null comment '处理时间',
    processor_id    bigint      default 0                 not null comment '处理人ID',
    response        text                                  null comment '回复内容',
    created_at      datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    charset = utf8
    row_format = DYNAMIC;

create index idx_status
    on eb_user_feedback (feedback_status);

create index idx_submit_time
    on eb_user_feedback (submit_time);

create index idx_user_id
    on eb_user_feedback (user_id);

create table eb_user_type
(
    id          int auto_increment comment '用户类型ID，主键'
        primary key,
    type_name   varchar(50)                        not null comment '用户类型名称',
    description varchar(255)                       null comment '类型描述',
    status      tinyint  default 1                 not null comment '状态：0-禁用，1-启用',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
);

