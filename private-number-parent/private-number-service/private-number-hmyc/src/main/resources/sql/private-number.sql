CREATE TABLE `private_recycle_push_fail`
(
    `bind_id`      varchar(128) NOT NULL COMMENT '绑定id',
    `request_id`   varchar(128) NOT NULL COMMENT '请求id',
    `tel_a`        varchar(32)  DEFAULT NULL COMMENT 'A号码',
    `tel_b`        varchar(32)  DEFAULT NULL COMMENT 'B号码',
    `tel_b_other`  varchar(256) DEFAULT NULL COMMENT '其他B号码',
    `tel`          varchar(32)  DEFAULT NULL COMMENT 'A号码(分机号模式)',
    `tel_x`        varchar(32)  DEFAULT NULL COMMENT 'X号码',
    `tel_y`        varchar(256) DEFAULT NULL COMMENT 'Y号码',
    `ext_num`      varchar(8)   DEFAULT NULL COMMENT '分机号',
    `expire_time`  datetime     DEFAULT NULL COMMENT '过期时间',
    `area_code`    varchar(8)   DEFAULT NULL COMMENT '地市编码',
    `num_type`     varchar(8)   DEFAULT NULL COMMENT '号码类型',
    `operate_type` varchar(8)   DEFAULT NULL COMMENT '操作类型',
    PRIMARY KEY (`bind_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='延时回收数据推送mq失败记录';

CREATE TABLE `private_vcc_info`
(
    `vcc_id`                varchar(8) NOT NULL COMMENT '企业id 同appkey',
    `vcc_name`              varchar(16)  DEFAULT NULL COMMENT '企业名称',
    `num_type`              varchar(128) DEFAULT NULL COMMENT '申请的绑定类型',
    `secret_key`            varchar(128) DEFAULT NULL COMMENT '秘钥',
    `ext_num_count`         int(1)       DEFAULT NULL COMMENT '分机号个数',
    `bind_query_url`        varchar(128) DEFAULT NULL COMMENT '客户 绑定关系查询url',
    `bind_convert_url`      varchar(128) DEFAULT NULL COMMENT '内部 绑定关系结果处理url',
    `master_num`            int(1)       DEFAULT NULL COMMENT '该企业的通用主池数量',
    `bill_push_url`         varchar(128) DEFAULT NULL COMMENT '企业话单推送地址',
    `sms_push_url`          varchar(128) DEFAULT NULL COMMENT '企业短信推送地址',
    `not_bind_ivr`          varchar(36)  DEFAULT NULL COMMENT '无绑定关系默认提示音',
    `digits_ivr`            varchar(36)  DEFAULT NULL COMMENT '请输入分机号提示语',
    `status_push_url`       varchar(255) DEFAULT NULL COMMENT '通话状态推送url地址',
    `status_push_flag`      varchar(128) DEFAULT NULL COMMENT '通话状态推送标记  字符串包含即推送',
    `un_bind_push_url`      varchar(128) DEFAULT NULL COMMENT '解绑推送url',
    `ayb_bind_push_url`     varchar(128) DEFAULT NULL COMMENT 'AXE-AYB绑定推送url',
    `sms_flag`              tinyint(1)   DEFAULT NULL COMMENT '是否禁止发短信 1 是, 0 否',
    `record_flag`           tinyint(1)   DEFAULT NULL COMMENT '是否允许录音 1 是 0 否',
    `binding_param_adapter` text COMMENT '绑定接口适配json\r\n{\r\n  "AXB": {\r\n     "通用字段": "定制字段"\r\n  }\r\n}',
    `auth_flag`             tinyint(1)   DEFAULT NULL COMMENT '绑定关系接口是否需要鉴权 1是, 其他否',
    `create_by`             varchar(32)  DEFAULT NULL COMMENT '创建人',
    `create_time`           datetime     DEFAULT NULL COMMENT '添加时间',
    `update_by`             varchar(32)  DEFAULT NULL COMMENT '更新人',
    `update_time`           datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`vcc_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='企业信息表';

CREATE TABLE `private_master_info`
(
    `id`          varchar(64) NOT NULL COMMENT '主键uuid',
    `vcc_id`      varchar(32) NOT NULL COMMENT '企业id',
    `area_code`   varchar(32) NOT NULL COMMENT '地市编码',
    `num`         int(11)     NOT NULL COMMENT '主池数量',
    `create_by`   varchar(255) DEFAULT NULL COMMENT '用户',
    `create_time` datetime     DEFAULT NULL COMMENT '添加时间',
    `update_by`   varchar(255) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='企业信息表';;

CREATE TABLE `sms_white_words`
(
    `id`           varchar(36) NOT NULL,
    `create_by`    varchar(50)  DEFAULT NULL COMMENT '创建人',
    `create_time`  datetime     DEFAULT NULL COMMENT '创建日期',
    `update_by`    varchar(50)  DEFAULT NULL COMMENT '更新人',
    `update_time`  datetime     DEFAULT NULL COMMENT '更新日期',
    `sys_org_code` varchar(64)  DEFAULT NULL COMMENT '所属部门',
    `vcc_id`       varchar(8)   DEFAULT NULL COMMENT '企业Id',
    `white_words`  varchar(255) DEFAULT NULL COMMENT '白名单词汇，多个则以"、"分割',
    PRIMARY KEY (`id`),
    KEY `idx_vcc_id` (`vcc_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='短信白名单词汇表';

CREATE TABLE `sms_discard_record`
(
    `id`           varchar(36) NOT NULL COMMENT '分布式主键ID',
    `vcc_id`       varchar(8)    DEFAULT NULL COMMENT '企业vccId',
    `msg_id`       varchar(64)   DEFAULT NULL COMMENT '短信业务流水号',
    `tel_a`        varchar(16)   DEFAULT NULL COMMENT '主叫号码',
    `tel_x`        varchar(16)   DEFAULT NULL COMMENT '被叫号码',
    `receive_time` datetime      DEFAULT NULL COMMENT '短信接收时间',
    `sms_content`  varchar(255)  DEFAULT NULL COMMENT '短信内容',
    `discard_info` varchar(255)  DEFAULT NULL COMMENT '丢弃原因',
    `req_json`     varchar(1000) DEFAULT NULL COMMENT 'body请求内容',
    `create_time`  datetime      DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='短信丢弃记录表';

CREATE TABLE `sms_black_words`
(
    `id`           varchar(36) NOT NULL,
    `vcc_id`       varchar(8)   DEFAULT NULL COMMENT '企业Id',
    `black_words`  varchar(255) DEFAULT NULL COMMENT '黑名单词汇，多个则以"、"分割',
    `create_by`    varchar(50)  DEFAULT NULL COMMENT '创建人',
    `create_time`  datetime     DEFAULT NULL COMMENT '创建日期',
    `update_by`    varchar(50)  DEFAULT NULL COMMENT '更新人',
    `update_time`  datetime     DEFAULT NULL COMMENT '更新日期',
    `sys_org_code` varchar(64)  DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`id`),
    KEY `idx_vcc_id` (`vcc_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='短信黑名单词汇表';

CREATE TABLE `private_fail_message`
(
    `id`          varchar(64) NOT NULL COMMENT '主键uuid',
    `body`        text COMMENT '内容json体',
    `ip`          varchar(32)  DEFAULT NULL COMMENT '机器ip',
    `num`         int(11)      DEFAULT NULL COMMENT '重推次数',
    `err_msg`     varchar(255) DEFAULT NULL COMMENT '错误信息',
    `vccid`       varchar(32)  DEFAULT NULL COMMENT '企业id',
    `type`        varchar(32)  DEFAULT NULL COMMENT 'STATUS 通话状态   BILL  通话话单',
    `create_time` datetime     DEFAULT NULL COMMENT '入库时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `private_area_location`
(
    `area_code`       varchar(16) NOT NULL COMMENT '地市编码',
    `init_location`   varchar(8) DEFAULT NULL COMMENT '机房位置, 初始化',
    `update_location` varchar(8) DEFAULT NULL COMMENT '机房位置, 修改',
    `create_time`     datetime   DEFAULT NULL COMMENT '创建时间',
    `update_time`     datetime   DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`area_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='小号平台 地市编码和机房对应关系';

CREATE TABLE `concurrent_count_info`
(
    `id`          varchar(64) NOT NULL COMMENT '主键',
    `vccid`       varchar(32) DEFAULT NULL COMMENT '企业id',
    `num`         int(11)     DEFAULT NULL COMMENT '当前并发数量',
    `count_time`  varchar(16) DEFAULT NULL COMMENT '统计时间',
    `create_time` datetime    DEFAULT NULL COMMENT '入库时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='并发统计呼叫次数表';

CREATE TABLE `concurrent_caller_count`
(
    `id`          varchar(64) NOT NULL COMMENT '主键',
    `vccid`       varchar(32) DEFAULT NULL COMMENT '企业id',
    `business`    varchar(32) DEFAULT NULL COMMENT '业务',
    `num`         int(11)     DEFAULT NULL COMMENT '呼叫次数',
    `caller`      varchar(32) DEFAULT NULL COMMENT '主叫号码',
    `operator`    varchar(32) DEFAULT NULL COMMENT '运营商',
    `count_time`  varchar(32) DEFAULT NULL COMMENT '统计时间 天',
    `create_time` datetime    DEFAULT NULL COMMENT '入库时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='并发统计主叫统计表';

CREATE TABLE `private_number_pool`
(
    `id`                    varchar(64) NOT NULL COMMENT 'uuid',
    `number`                varchar(16) DEFAULT NULL COMMENT 'X号码',
    `num_type`              varchar(8)  DEFAULT NULL COMMENT '号码类型, AXB, AXE',
    `area_code`             varchar(8)  DEFAULT NULL COMMENT '地区编码 010',
    `place`                 varchar(8)  DEFAULT NULL COMMENT '主备池类型, 默认 MASTER',
    `vcc_id`                varchar(8)  DEFAULT NULL COMMENT '企业id',
    `daily_short_message`   int(11)     DEFAULT NULL COMMENT '每日短信上限数',
    `monthly_short_message` int(11)     DEFAULT NULL COMMENT '每月短信上限数',
    `create_time`           datetime    DEFAULT NULL COMMENT '创建时间',
    `update_time`           datetime    DEFAULT NULL COMMENT '修改时间',
    `create_by`             varchar(50) DEFAULT NULL COMMENT '创建人',
    `update_by`             varchar(50) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_area_code` (`area_code`) USING BTREE,
    KEY `idx_number` (`number`) USING BTREE,
    KEY `idx_vcc_id` (`vcc_id`) USING BTREE,
    KEY `idx_num_type` (`num_type`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='隐私号码池';
