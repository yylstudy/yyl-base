CREATE TABLE `cloudcc_ivr_outbound_task_number` (
                                                    `number_id` varchar(64) NOT NULL COMMENT '号码id',
                                                    `task_id` varchar(64) NOT NULL COMMENT '任务id',
                                                    `tenant_id` varchar(10) NOT NULL COMMENT '租户id同企业编码',
                                                    `number` varchar(64) NOT NULL COMMENT '外呼号码',
                                                    `call_status` tinyint(1) DEFAULT '0' COMMENT '呼叫状态 1- 已呼叫 0-未呼叫',
                                                    `answer_status` tinyint(1) DEFAULT NULL COMMENT '接通状态 1-已接通 0 -未接通',
                                                    `call_count` int(1) DEFAULT '0' COMMENT '呼叫次数',
                                                    `call_time` datetime DEFAULT NULL COMMENT '发起呼叫时间',
                                                    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
                                                    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                                    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                                    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
                                                    PRIMARY KEY (`number_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IVR外呼任务-号码';


CREATE TABLE `cloudcc_ivr_outbound_task` (
                                             `task_id`              varchar(64) NOT NULL COMMENT '任务id',
                                             `task_name`            varchar(64)     DEFAULT NULL COMMENT '任务名称',
                                             `tenant_id`            varchar(10)     DEFAULT NULL COMMENT '租户id同企业编码',
                                             `project_id`           varchar(64)     DEFAULT NULL COMMENT '项目id',
                                             `task_type`            tinyint(1)      DEFAULT NULL COMMENT '类型 1-IVR流程、2-语音通知',
                                             `ivr_id`               varchar(128)    DEFAULT NULL COMMENT 'IVR流程id',
                                             `voice_notify_file_id` varchar(128)    DEFAULT NULL COMMENT '语音通知文件id',
                                             `waiting_tone_type`    tinyint(1)      DEFAULT NULL COMMENT '等待音类型(默认音/默认视频和自选)',
                                             `start_time`           datetime        DEFAULT NULL COMMENT '任务开始时间',
                                             `end_time`             datetime        DEFAULT NULL COMMENT '任务结束时间',
                                             `callable_time`        varchar(512)    DEFAULT NULL COMMENT '呼出时段 [{"beginTime":"00:00:00","endTime":"23:00:00" }]',
                                             `display_number`       varchar(512)    DEFAULT NULL COMMENT '外显号码 号码数组json',
                                             `duplicate_type`       tinyint(1)      DEFAULT NULL COMMENT '判重类型',
                                             `duplicate_time`       int(1)          DEFAULT NULL COMMENT '判重时间（天）',
                                             `outbound_frequency`   int(1)          DEFAULT NULL COMMENT '外呼频率（秒）',
                                             `outbound_ratio`       int(1) unsigned DEFAULT NULL COMMENT '外呼比例',
                                             `max_ring_time`        int(1)          DEFAULT NULL COMMENT '无人接听振铃时长（秒）',
                                             `max_attempt_count`    int(1)          DEFAULT NULL COMMENT '外呼次数',
                                             `attempt_interval`     int(1)          DEFAULT NULL COMMENT '重呼间隔（秒）',
                                             `task_state`           tinyint(1)      DEFAULT NULL COMMENT '任务状态 1-启动 0-未启动, 2-暂停',
                                             `job_id`               int(1)          DEFAULT NULL COMMENT 'xxl-job对应的任务id',
                                             `create_by`            varchar(64)     DEFAULT NULL COMMENT '创建人',
                                             `create_time`          datetime        DEFAULT NULL COMMENT '创建时间',
                                             `update_time`          datetime        DEFAULT NULL COMMENT '更新时间',
                                             `update_by`            varchar(64)     DEFAULT NULL COMMENT '更新人',
                                             PRIMARY KEY (`task_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IVR外呼任务';
