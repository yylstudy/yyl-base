# 分机状态迁移日志
CREATE TABLE IF NOT EXISTS `cloudcc_ext_status_log_${company_code}_${month}`
(
    `log_id`           bigint(20) NOT NULL COMMENT '变更主键id',
    `uuid`             varchar(64) DEFAULT NULL COMMENT '主叫侧uuid',
    `company_code`     varchar(16) DEFAULT NULL COMMENT '企业id',
    `ext_id`           varchar(32) DEFAULT NULL COMMENT '分机id',
    `ext_ip`           varchar(32) DEFAULT NULL COMMENT '分机IP',
    `source_status`    varchar(32) DEFAULT NULL COMMENT '上一次状态',
    `source_timestamp` bigint(20)  DEFAULT NULL COMMENT '上一次状态的时间戳',
    `transfer_action`  varchar(32) DEFAULT NULL COMMENT '状态变更操作类型',
    `target_status`    varchar(32) DEFAULT NULL COMMENT '变更后的当前状态',
    `target_timestamp` bigint(20)  DEFAULT NULL COMMENT '当前状态时间戳',
    `reason`           varchar(32) DEFAULT NULL COMMENT '状态变更原因',
    PRIMARY KEY (`log_id`) USING BTREE,
    KEY `idx_ext_id` (`ext_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='话机状态变迁记录 ${company_code}_${month}';

# 坐席状态迁移日志
CREATE TABLE IF NOT EXISTS `cloudcc_agent_status_log_${company_code}_${month}`
(
    `log_id`            bigint(20) NOT NULL COMMENT '变更主键id',
    `uuid`              varchar(64) DEFAULT NULL COMMENT '主叫侧uuid',
    `company_code`      varchar(16) DEFAULT NULL COMMENT '企业id',
    `agent_id`          varchar(32) DEFAULT NULL COMMENT '坐席工号',
    `ext_id`            varchar(32) DEFAULT NULL COMMENT '分机id',
    `source_status`     varchar(32) DEFAULT NULL COMMENT '上一次状态',
    `source_timestamp`  bigint(20)  DEFAULT NULL COMMENT '上一次状态的时间戳',
    `source_sub_status` varchar(32) DEFAULT NULL COMMENT '上一次子状态',
    `source_duration`   int(1)      DEFAULT NULL COMMENT '上一次状态持续时间',
    `transfer_action`   varchar(32) DEFAULT NULL COMMENT '状态变更操作类型',
    `target_status`     varchar(32) DEFAULT NULL COMMENT '变更后的当前状态',
    `target_timestamp`  bigint(20)  DEFAULT NULL COMMENT '当前状态时间戳',
    `target_duration`   int(1)      DEFAULT NULL COMMENT '当前状态持续时间',
    `target_sub_status` varchar(32) DEFAULT NULL COMMENT '当前子状态',
    `reason`            varchar(32) DEFAULT NULL COMMENT '状态变更原因',
    PRIMARY KEY (`log_id`) USING BTREE,
    KEY `idx_agent_id` (`agent_id`) USING BTREE,
    KEY `idx_target_timestamp` (`target_timestamp`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='坐席状态变更日志表 ${company_code}_${month}';

# 主话单
CREATE TABLE IF NOT EXISTS `cloudcc_main_cdr_${company_code}_${month}`
(
    `call_id`                 varchar(128) NOT NULL COMMENT '自定义主话单id',
    `uuid`                    varchar(128) DEFAULT NULL COMMENT '主叫侧uuid',
    `client_uuid`             varchar(128) DEFAULT NULL COMMENT '客户侧uuid(外呼被叫, 呼入主叫)',
    `company_code`            varchar(8)   DEFAULT NULL COMMENT '企业id',
    `agent_id`                varchar(32)  DEFAULT NULL COMMENT '坐席id',
    `ext_id`                  varchar(32)  DEFAULT NULL COMMENT '分机id',
    `skill_id`                varchar(32)  DEFAULT NULL COMMENT '客户呼入技能id',
    `caller_number`           varchar(32)  DEFAULT NULL COMMENT '主叫号码(外呼-分机id, 呼入-客户手机号(分机id))',
    `display_number`          varchar(32)  DEFAULT NULL COMMENT '外显号码(外呼-企业主显号, 呼入-客户手机号(企业主显号))',
    `callee_number`           varchar(32)  DEFAULT NULL COMMENT '被叫号码(外呼-客户手机号(分机id), 呼入-企业号码(分机id))',
    `platform_number`         varchar(32)  DEFAULT NULL COMMENT '平台号码',
    `charge_number`           varchar(32)  DEFAULT NULL COMMENT '计费号码',
    `caller_area_code`        varchar(8)   DEFAULT NULL COMMENT '主叫区号',
    `callee_area_code`        varchar(8)   DEFAULT NULL COMMENT '被叫区号',
    `out_line`                tinyint(1)   DEFAULT NULL COMMENT '内外线: 0-内线, 1-外线',
    `voice_mail_flag`         tinyint(1)   DEFAULT NULL COMMENT '是否有留言(语音信箱)ivr. 1-有, 0-没有',
    `satisfaction_flag`       tinyint(1)   DEFAULT NULL COMMENT '是否有满意度. 1-有, 0-没有',
    `callin_ivr_flag`         tinyint(1)   DEFAULT NULL COMMENT '是否有呼入ivr. 1-有, 0-没有',
    `trans_ivr_flag`          tinyint(1)   DEFAULT NULL COMMENT '是否有转接ivr. 1-有, 0-没有',
    `direction`               tinyint(1)   DEFAULT NULL COMMENT '呼叫方向(1-呼入inbound, 0-呼出outbound)',

    `callin_time`             datetime     DEFAULT NULL COMMENT '呼入时间 (yyyy-MM-dd HH:mm:ss)',
    `invite_time`             datetime     DEFAULT NULL COMMENT '外呼时间 (yyyy-MM-dd HH:mm:ss)',
    `ring_time`               datetime     DEFAULT NULL COMMENT '振铃时间 (yyyy-MM-dd HH:mm:ss)',
    `answer_time`             datetime     DEFAULT NULL COMMENT '接通时间 (yyyy-MM-dd HH:mm:ss)',
    `bridge_time`             datetime     DEFAULT NULL COMMENT '桥接时间 (yyyy-MM-dd HH:mm:ss)',
    `hangup_time`             datetime     DEFAULT NULL COMMENT '挂断时间 (yyyy-MM-dd HH:mm:ss)',
    `call_start_time`         datetime     DEFAULT NULL COMMENT '开始通话时间 (yyyy-MM-dd HH:mm:ss)',
    `call_end_time`           datetime     DEFAULT NULL COMMENT '结束通话时间 (yyyy-MM-dd HH:mm:ss)',
    `start_queue_time`        datetime     DEFAULT NULL COMMENT '开始排队时间 (yyyy-MM-dd HH:mm:ss)',
    `end_queue_time`          datetime     DEFAULT NULL COMMENT '结束排队时间 (yyyy-MM-dd HH:mm:ss)',
    `start_ivr_time`          datetime     DEFAULT NULL COMMENT '开始ivr时间 (yyyy-MM-dd HH:mm:ss)',
    `end_ivr_time`            datetime     DEFAULT NULL COMMENT '结束ivr时间 (yyyy-MM-dd HH:mm:ss)',
    `start_satisfaction_time` datetime     DEFAULT NULL COMMENT '开始满意度时间 (yyyy-MM-dd HH:mm:ss)',
    `voice_mail_start_time`   datetime     DEFAULT NULL COMMENT '开始留言(语音信箱)时间 (yyyy-MM-dd HH:mm:ss)',
    `voice_mail_end_time`     datetime     DEFAULT NULL COMMENT '结束留言(语音信箱)时间 (yyyy-MM-dd HH:mm:ss)',
    `callin_stamp`            bigint(20)   DEFAULT NULL COMMENT '呼入时间-时间戳',
    `invite_stamp`            bigint(20)   DEFAULT NULL COMMENT '外呼时间-时间戳',
    `ring_stamp`              bigint(20)   DEFAULT NULL COMMENT '振铃时间-时间戳',
    `answer_stamp`            bigint(20)   DEFAULT NULL COMMENT '接通时间-时间戳',
    `bridge_stamp`            bigint(20)   DEFAULT NULL COMMENT '桥接时间-时间戳',
    `hangup_stamp`            bigint(20)   DEFAULT NULL COMMENT '挂断时间-时间戳',
    `answer_second`           bigint(20)   DEFAULT NULL COMMENT '接通秒数',
    `call_start_stamp`        bigint(20)   DEFAULT NULL COMMENT '开始通话时间-时间戳',
    `call_end_stamp`          bigint(20)   DEFAULT NULL COMMENT '结束通话时间-时间戳',
    `duration`                bigint(20)   DEFAULT NULL COMMENT '通话时长(variable_duration) hangup_time - answer_time',

    `release_dir`             tinyint(1)   DEFAULT NULL COMMENT '挂机方(0-平台释放, 1-主叫释放, 2-被叫释放)',
    `release_code`            tinyint(2)   DEFAULT NULL COMMENT '结束原因值',
    `release_desc`            varchar(32)  DEFAULT NULL COMMENT '结束原因描述',
    `hangup_cause`            varchar(128) DEFAULT NULL COMMENT '挂断原因(挂断事件)',
    `service_id`              varchar(128) DEFAULT NULL COMMENT 'fs服务器',

    `media_type`              tinyint(1)   DEFAULT NULL COMMENT '媒体类型(1-audio, 2-video)',
    `audio`                   tinyint(1)   DEFAULT NULL COMMENT '【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3',
    `video`                   tinyint(1)   DEFAULT NULL COMMENT '【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0',
    `record_url`              varchar(256) DEFAULT NULL COMMENT '录音url (主叫侧的录音)',
    PRIMARY KEY (`call_id`) USING BTREE,
#     KEY `idx_voice_mail_flag` (`voice_mail_flag`) USING BTREE,
    KEY `idx_agent_id` (`agent_id`) USING BTREE,
    KEY `idx_call_start_time` (`call_start_time`) USING BTREE,
    KEY `idx_call_end_time` (`call_end_time`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='主话单(主叫为准) ${company_code}_${month}';

# 留言
CREATE TABLE IF NOT EXISTS `cloudcc_leave_message_${company_code}_${month}`
(
    `call_id`                 varchar(128) NOT NULL COMMENT '自定义主话单id',
    `uuid`                    varchar(128) DEFAULT NULL COMMENT '主叫侧uuid',
    `client_uuid`             varchar(128) DEFAULT NULL COMMENT '客户侧uuid(外呼被叫, 呼入主叫)',
    `company_code`            varchar(8)   DEFAULT NULL COMMENT '企业id',
    `agent_id`                varchar(32)  DEFAULT NULL COMMENT '坐席id',
    `ext_id`                  varchar(32)  DEFAULT NULL COMMENT '分机id',
    `skill_id`                varchar(32)  DEFAULT NULL COMMENT '客户呼入技能id',
    `caller_number`           varchar(32)  DEFAULT NULL COMMENT '主叫号码(外呼-分机id, 呼入-客户手机号(分机id))',
    `display_number`          varchar(32)  DEFAULT NULL COMMENT '外显号码(外呼-企业主显号, 呼入-客户手机号(企业主显号))',
    `callee_number`           varchar(32)  DEFAULT NULL COMMENT '被叫号码(外呼-客户手机号(分机id), 呼入-企业号码(分机id))',
    `platform_number`         varchar(32)  DEFAULT NULL COMMENT '平台号码',
    `charge_number`           varchar(32)  DEFAULT NULL COMMENT '计费号码',
    `caller_area_code`        varchar(8)   DEFAULT NULL COMMENT '主叫区号',
    `callee_area_code`        varchar(8)   DEFAULT NULL COMMENT '被叫区号',
    `out_line`                tinyint(1)   DEFAULT NULL COMMENT '内外线: 0-内线, 1-外线',
    `voice_mail_flag`         tinyint(1)   DEFAULT NULL COMMENT '是否有留言(语音信箱)ivr. 1-有, 0-没有',
    `satisfaction_flag`       tinyint(1)   DEFAULT NULL COMMENT '是否有满意度. 1-有, 0-没有',
    `callin_ivr_flag`         tinyint(1)   DEFAULT NULL COMMENT '是否有呼入ivr. 1-有, 0-没有',
    `trans_ivr_flag`          tinyint(1)   DEFAULT NULL COMMENT '是否有转接ivr. 1-有, 0-没有',
    `direction`               tinyint(1)   DEFAULT NULL COMMENT '呼叫方向(1-呼入inbound, 0-呼出outbound)',

    `callin_time`             datetime     DEFAULT NULL COMMENT '呼入时间 (yyyy-MM-dd HH:mm:ss)',
    `invite_time`             datetime     DEFAULT NULL COMMENT '外呼时间 (yyyy-MM-dd HH:mm:ss)',
    `ring_time`               datetime     DEFAULT NULL COMMENT '振铃时间 (yyyy-MM-dd HH:mm:ss)',
    `answer_time`             datetime     DEFAULT NULL COMMENT '接通时间 (yyyy-MM-dd HH:mm:ss)',
    `bridge_time`             datetime     DEFAULT NULL COMMENT '桥接时间 (yyyy-MM-dd HH:mm:ss)',
    `hangup_time`             datetime     DEFAULT NULL COMMENT '挂断时间 (yyyy-MM-dd HH:mm:ss)',
    `call_start_time`         datetime     DEFAULT NULL COMMENT '开始通话时间 (yyyy-MM-dd HH:mm:ss)',
    `call_end_time`           datetime     DEFAULT NULL COMMENT '结束通话时间 (yyyy-MM-dd HH:mm:ss)',
    `start_queue_time`        datetime     DEFAULT NULL COMMENT '开始排队时间 (yyyy-MM-dd HH:mm:ss)',
    `end_queue_time`          datetime     DEFAULT NULL COMMENT '结束排队时间 (yyyy-MM-dd HH:mm:ss)',
    `start_ivr_time`          datetime     DEFAULT NULL COMMENT '开始ivr时间 (yyyy-MM-dd HH:mm:ss)',
    `end_ivr_time`            datetime     DEFAULT NULL COMMENT '结束ivr时间 (yyyy-MM-dd HH:mm:ss)',
    `start_satisfaction_time` datetime     DEFAULT NULL COMMENT '开始满意度时间 (yyyy-MM-dd HH:mm:ss)',
    `voice_mail_start_time`   datetime     DEFAULT NULL COMMENT '开始留言(语音信箱)时间 (yyyy-MM-dd HH:mm:ss)',
    `voice_mail_end_time`     datetime     DEFAULT NULL COMMENT '结束留言(语音信箱)时间 (yyyy-MM-dd HH:mm:ss)',
    `callin_stamp`            bigint(20)   DEFAULT NULL COMMENT '呼入时间-时间戳',
    `invite_stamp`            bigint(20)   DEFAULT NULL COMMENT '外呼时间-时间戳',
    `ring_stamp`              bigint(20)   DEFAULT NULL COMMENT '振铃时间-时间戳',
    `answer_stamp`            bigint(20)   DEFAULT NULL COMMENT '接通时间-时间戳',
    `bridge_stamp`            bigint(20)   DEFAULT NULL COMMENT '桥接时间-时间戳',
    `hangup_stamp`            bigint(20)   DEFAULT NULL COMMENT '挂断时间-时间戳',
    `answer_second`           bigint(20)   DEFAULT NULL COMMENT '接通秒数',
    `call_start_stamp`        bigint(20)   DEFAULT NULL COMMENT '开始通话时间-时间戳',
    `call_end_stamp`          bigint(20)   DEFAULT NULL COMMENT '结束通话时间-时间戳',
    `duration`                bigint(20)   DEFAULT NULL COMMENT '通话时长(variable_duration) hangup_time - answer_time',

    `release_dir`             tinyint(1)   DEFAULT NULL COMMENT '挂机方(0-平台释放, 1-主叫释放, 2-被叫释放)',
    `release_code`            tinyint(2)   DEFAULT NULL COMMENT '结束原因值',
    `release_desc`            varchar(32)  DEFAULT NULL COMMENT '结束原因描述',
    `hangup_cause`            varchar(128) DEFAULT NULL COMMENT '挂断原因(挂断事件)',
    `service_id`              varchar(128) DEFAULT NULL COMMENT 'fs服务器',

    `media_type`              tinyint(1)   DEFAULT NULL COMMENT '媒体类型(1-audio, 2-video)',
    `audio`                   tinyint(1)   DEFAULT NULL COMMENT '【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3',
    `video`                   tinyint(1)   DEFAULT NULL COMMENT '【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0',
    `record_url`              varchar(256) DEFAULT NULL COMMENT '录音url (主叫侧的录音)',
    `callback_status`         tinyint(1)   DEFAULT NULL COMMENT '回呼状态(0-未回呼, 2-已回呼)',
    `callback_agent`    varchar(32)  DEFAULT NULL COMMENT '回呼坐席',
    PRIMARY KEY (`call_id`) USING BTREE,
#     KEY `idx_voice_mail_flag` (`voice_mail_flag`) USING BTREE,
    KEY `idx_agent_id` (`agent_id`) USING BTREE,
    KEY `idx_call_start_time` (`call_start_time`) USING BTREE,
    KEY `idx_call_end_time` (`call_end_time`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='留言 ${company_code}_${month}';

# 子话单
CREATE TABLE IF NOT EXISTS `cloudcc_sub_cdr_${company_code}_${month}`
(
    `uuid`                    varchar(128) NOT NULL COMMENT '被叫侧uuid',
    `call_id`                 varchar(128) NOT NULL COMMENT '自定义主话单id',
    `client_uuid`             varchar(128) DEFAULT NULL COMMENT '客户侧uuid(外呼被叫, 呼入主叫)',
    `company_code`            varchar(8)   DEFAULT NULL COMMENT '企业id',
    `agent_id`                varchar(32)  DEFAULT NULL COMMENT '坐席id',
    `ext_id`                  varchar(32)  DEFAULT NULL COMMENT '分机id',
    `skill_id`                varchar(32)  DEFAULT NULL COMMENT '客户呼入技能id',
    `caller_number`           varchar(32)  DEFAULT NULL COMMENT '主叫号码(外呼-分机id, 呼入-客户手机号(分机id))',
    `display_number`          varchar(32)  DEFAULT NULL COMMENT '外显号码(外呼-企业主显号, 呼入-客户手机号(企业主显号))',
    `callee_number`           varchar(32)  DEFAULT NULL COMMENT '被叫号码(外呼-客户手机号(分机id), 呼入-企业号码(分机id))',
    `platform_number`         varchar(32)  DEFAULT NULL COMMENT '平台号码',
    `charge_number`           varchar(32)  DEFAULT NULL COMMENT '计费号码',
    `caller_area_code`        varchar(8)   DEFAULT NULL COMMENT '主叫区号',
    `callee_area_code`        varchar(8)   DEFAULT NULL COMMENT '被叫区号',

    `called_party_number`     varchar(64)  DEFAULT NULL COMMENT '被叫号码，即用户拨叫的号码\r\n对于主叫话单，填写主叫实际拨打的号码\r\n对于被叫话单，填写被叫计费的16位分机号\r\n对于前转话单，填写前转流程处理前的被叫号码',
    `calling_party_number`    varchar(64)  DEFAULT NULL COMMENT '主叫号码 \r\n对于主叫话单，填写主叫计费的16位分机号\r\n对于被叫话单，填写主叫号码，即INVITE的PAI消息头，如果PAI消息头为空，则填写INVITE的From消息头\r\n对于前转话单，填写前转计费的16位分机号\r\n对于被叫话单，填写主叫号码，即INVITE的PAI消息头，如果PAI消息头为空，则填写INVITE的From消息头\r\n对于前转话单，填写前转计费的16位分机号\r\n',

    `out_line`                tinyint(1)   DEFAULT NULL COMMENT '内外线: 0-内线, 1-外线',
    `satisfaction_flag`       tinyint(1)   DEFAULT NULL COMMENT '是否有满意度. 1-有, 0-没有',
    `callin_ivr_flag`         tinyint(1)   DEFAULT NULL COMMENT '是否有呼入ivr. 1-有, 0-没有',
    `trans_ivr_flag`          tinyint(1)   DEFAULT NULL COMMENT '是否有转接ivr. 1-有, 0-没有',
    `direction`               tinyint(1)   DEFAULT NULL COMMENT '呼叫方向(1-呼入inbound, 0-呼出outbound)',
    `a_uuid`                  varchar(128) DEFAULT NULL COMMENT 'A路uuid',
    `a_call_type`             tinyint(1)   DEFAULT NULL COMMENT 'A路 话务类型 agent-0 client-1',
    `a_callin_time`           datetime     DEFAULT NULL COMMENT 'A路呼入时间 (yyyy-MM-dd HH:mm:ss)',
    `a_invite_time`           datetime     DEFAULT NULL COMMENT 'A路外呼时间 (yyyy-MM-dd HH:mm:ss)',
    `a_ring_time`             datetime     DEFAULT NULL COMMENT 'A路振铃时间 (yyyy-MM-dd HH:mm:ss)',
    `a_answer_time`           datetime     DEFAULT NULL COMMENT 'A路接通时间 (yyyy-MM-dd HH:mm:ss)',
    `a_bridge_time`           datetime     DEFAULT NULL COMMENT 'A路桥接时间 (yyyy-MM-dd HH:mm:ss)',
    `a_hangup_time`           datetime     DEFAULT NULL COMMENT 'A路挂断时间 (yyyy-MM-dd HH:mm:ss)',
    `a_call_start_time`       datetime     DEFAULT NULL COMMENT 'A路开始通话时间 (yyyy-MM-dd HH:mm:ss)',
    `a_call_end_time`         datetime     DEFAULT NULL COMMENT 'A路结束通话时间 (yyyy-MM-dd HH:mm:ss)',
    `a_callin_stamp`          bigint(20)   DEFAULT NULL COMMENT 'A路呼入时间-时间戳',
    `a_invite_stamp`          bigint(20)   DEFAULT NULL COMMENT 'A路外呼时间-时间戳',
    `a_ring_stamp`            bigint(20)   DEFAULT NULL COMMENT 'A路振铃时间-时间戳',
    `a_answer_stamp`          bigint(20)   DEFAULT NULL COMMENT 'A路接通时间-时间戳',
    `a_answer_second`         bigint(20)   DEFAULT NULL COMMENT 'A路接通秒数',
    `a_bridge_stamp`          bigint(20)   DEFAULT NULL COMMENT 'A路桥接时间-时间戳',
    `a_hangup_stamp`          bigint(20)   DEFAULT NULL COMMENT 'A路挂断时间-时间戳',
    `a_call_start_stamp`      bigint(20)   DEFAULT NULL COMMENT 'A路开始通话时间-时间戳',
    `a_call_end_stamp`        bigint(20)   DEFAULT NULL COMMENT 'A路结束通话时间-时间戳',
    `a_duration`              bigint(20)   DEFAULT NULL COMMENT 'A路通话时长(variable_duration) hangup_time - answer_time',

    `b_uuid`                  varchar(128) DEFAULT NULL COMMENT 'B路uuid',
    `b_call_type`             tinyint(1)   DEFAULT NULL COMMENT 'B路 话务类型 agent-0 client-1',
    `b_callin_time`           datetime     DEFAULT NULL COMMENT 'B路呼入时间 (yyyy-MM-dd HH:mm:ss)',
    `b_invite_time`           datetime     DEFAULT NULL COMMENT 'B路外呼时间 (yyyy-MM-dd HH:mm:ss)',
    `b_ring_time`             datetime     DEFAULT NULL COMMENT 'B路振铃时间 (yyyy-MM-dd HH:mm:ss)',
    `b_answer_time`           datetime     DEFAULT NULL COMMENT 'B路接通时间 (yyyy-MM-dd HH:mm:ss)',
    `b_bridge_time`           datetime     DEFAULT NULL COMMENT 'B路桥接时间 (yyyy-MM-dd HH:mm:ss)',
    `b_hangup_time`           datetime     DEFAULT NULL COMMENT 'B路挂断时间 (yyyy-MM-dd HH:mm:ss)',
    `b_call_start_time`       datetime     DEFAULT NULL COMMENT 'B路开始通话时间 (yyyy-MM-dd HH:mm:ss)',
    `b_call_end_time`         datetime     DEFAULT NULL COMMENT 'B路结束通话时间 (yyyy-MM-dd HH:mm:ss)',
    `b_callin_stamp`          bigint(20)   DEFAULT NULL COMMENT 'B路呼入时间-时间戳',
    `b_invite_stamp`          bigint(20)   DEFAULT NULL COMMENT 'B路外呼时间-时间戳',
    `b_ring_stamp`            bigint(20)   DEFAULT NULL COMMENT 'B路振铃时间-时间戳',
    `b_answer_stamp`          bigint(20)   DEFAULT NULL COMMENT 'B路接通时间-时间戳',
    `b_answer_second`         bigint(20)   DEFAULT NULL COMMENT 'B路接通秒数',
    `b_bridge_stamp`          bigint(20)   DEFAULT NULL COMMENT 'B路桥接时间-时间戳',
    `b_hangup_stamp`          bigint(20)   DEFAULT NULL COMMENT 'B路挂断时间-时间戳',
    `b_call_start_stamp`      bigint(20)   DEFAULT NULL COMMENT 'B路开始通话时间-时间戳',
    `b_call_end_stamp`        bigint(20)   DEFAULT NULL COMMENT 'B路结束通话时间-时间戳',
    `b_duration`              bigint(20)   DEFAULT NULL COMMENT 'B路通话时长(variable_duration) hangup_time - answer_time',

    `start_queue_time`        datetime     DEFAULT NULL COMMENT '开始排队时间 (yyyy-MM-dd HH:mm:ss)',
    `end_queue_time`          datetime     DEFAULT NULL COMMENT '结束排队时间 (yyyy-MM-dd HH:mm:ss)',
    `queue_duration`          int(1)       DEFAULT NULL COMMENT '排队时长(s)',
    `queue_count`             int(1)       DEFAULT NULL COMMENT '排队次数',
    `start_ivr_time`          datetime     DEFAULT NULL COMMENT '开始ivr时间 (yyyy-MM-dd HH:mm:ss)',
    `end_ivr_time`            datetime     DEFAULT NULL COMMENT '结束ivr时间 (yyyy-MM-dd HH:mm:ss)',
    `start_satisfaction_time` datetime     DEFAULT NULL COMMENT '开始满意度时间 (yyyy-MM-dd HH:mm:ss)',

    `cdr_type`                tinyint(1)   DEFAULT NULL COMMENT '话单类型(0-外呼, 1-呼入, 2-监听, 3-耳语, 4-咨询, 5-转接, 6-代接, 7-三方通话, 8-强插)',

    `release_dir`             tinyint(1)   DEFAULT NULL COMMENT '挂机方(0-平台释放, 1-主叫释放, 2-被叫释放)',
    `release_code`            tinyint(2)   DEFAULT NULL COMMENT '结束原因值',
    `release_desc`            varchar(32)  DEFAULT NULL COMMENT '结束原因描述',
    `hangup_cause`            varchar(128) DEFAULT NULL COMMENT '挂断原因(挂断事件)',
    `service_id`              varchar(128) DEFAULT NULL COMMENT 'fs服务器',

    `media_type`              tinyint(1)   DEFAULT NULL COMMENT '媒体类型(1-audio, 2-video)',
    `audio`                   tinyint(1)   DEFAULT NULL COMMENT '【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3',
    `video`                   tinyint(1)   DEFAULT NULL COMMENT '【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0',
    `record_url`              varchar(256) DEFAULT NULL COMMENT '录音url (主叫侧的录音)',
    `ws_rtsp`                 varchar(255) DEFAULT NULL COMMENT '录音文件具体url',
    `record_url_rtsp`         varchar(255) DEFAULT NULL COMMENT '录音文件具体url',
    `absolute_url`            varchar(255) DEFAULT NULL COMMENT '录音文件具体url',
    `record_url_in`           varchar(255) DEFAULT NULL COMMENT '录音文件具体url',
    PRIMARY KEY (`uuid`) USING BTREE,
    KEY `idx_agent_id` (`agent_id`) USING BTREE,
    KEY `idx_call_start_time` (`b_call_start_time`) USING BTREE,
    KEY `idx_call_end_time` (`b_call_end_time`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='子话单(被叫为准) ${company_code}_${month}';

# 通道变量表
CREATE TABLE IF NOT EXISTS `cloudcc_cdr_channeldata_${company_code}_${month}`
(
    `log_id`              BIGINT(20)   NOT NULL COMMENT '变更主键id',
    `call_id`             VARCHAR(128) NOT NULL COMMENT '自定义主话单id',
    `uuid`                VARCHAR(64)  DEFAULT NULL COMMENT '主话单的uuid',
    `client_uuid`         VARCHAR(64)  DEFAULT NULL COMMENT '主话单的clien_uuid',
    `direction`           TINYINT(1)   DEFAULT NULL COMMENT '方向(1-呼入, 0-呼出)',
    `ivrid_e`             VARCHAR(55)  DEFAULT NULL COMMENT '最后的IVR标识',
    `satisfaction_ivrid`  VARCHAR(55)  DEFAULT NULL COMMENT '满意度的IVR标识',
    `satisfaction_f1`     VARCHAR(32)  DEFAULT '-1' COMMENT '满意度1',
    `satisfaction_f2`     VARCHAR(32)  DEFAULT '-1' COMMENT '满意度2',
    `satisfaction_f3`     VARCHAR(32)  DEFAULT '-1' COMMENT '满意度3',
    `satisfaction_f4`     VARCHAR(32)  DEFAULT '-1' COMMENT '满意度4',
    `satisfaction_f5`     VARCHAR(32)  DEFAULT '-1' COMMENT '满意度5',
    `satisfaction_f6`     VARCHAR(32)  DEFAULT '-1' COMMENT '满意度6',
    `satisfaction_f7`     VARCHAR(32)  DEFAULT '-1' COMMENT '满意度7',
    `satisfaction_f8`     VARCHAR(32)  DEFAULT '-1' COMMENT '满意度8',
    `satisfaction_f9`     VARCHAR(32)  DEFAULT '-1' COMMENT '满意度9',
    `satisfaction_f10`    VARCHAR(32)  DEFAULT '-1' COMMENT '满意度10',
    `userkey_f1`          VARCHAR(32)  DEFAULT '-1' COMMENT '用户按键1',
    `userkey_f2`          VARCHAR(32)  DEFAULT '-1' COMMENT '用户按键2',
    `userkey_f3`          VARCHAR(32)  DEFAULT '-1' COMMENT '用户按键3',
    `userkey_f4`          VARCHAR(32)  DEFAULT '-1' COMMENT '用户按键4',
    `userkey_f5`          VARCHAR(32)  DEFAULT '-1' COMMENT '用户按键5',
    `userkey_f6`          VARCHAR(32)  DEFAULT '-1' COMMENT '用户按键6',
    `userkey_f7`          VARCHAR(32)  DEFAULT '-1' COMMENT '用户按键7',
    `userkey_f8`          VARCHAR(32)  DEFAULT '-1' COMMENT '用户按键8',
    `userkey_f9`          VARCHAR(32)  DEFAULT '-1' COMMENT '用户按键9',
    `userkey_f10`         text COMMENT '用户按键10',
    `short_channel_name`  VARCHAR(255) DEFAULT NULL COMMENT '生成话单通道 方',
    `company_code`        VARCHAR(30)  DEFAULT NULL COMMENT '企业标识 ',
    `ifqa`                INT(1)       DEFAULT '0' COMMENT '是否已质检',
    `caseid`              VARCHAR(88)  DEFAULT NULL COMMENT '绑定的工单',
    `cusid`               VARCHAR(88)  DEFAULT NULL COMMENT '绑定的客户',
    `ifplayedleavemsg`    INT(1)       DEFAULT '0' COMMENT '是否听过留言',
    `ivr_tracks`          text COMMENT 'ivr轨迹',
    `cc_queue_hangup`     VARCHAR(10)  DEFAULT NULL COMMENT '队列标识，1进队列、0出队列、空没触发队列节点',
    `runcc_times`         datetime     DEFAULT NULL COMMENT '第一次进队列开始时间',
    `cc_busy_no`          VARCHAR(50)  DEFAULT NULL COMMENT '排队次数',
    `cti_callid`          VARCHAR(255) DEFAULT NULL COMMENT '热线号码',
    `ivr_parameters_data` VARCHAR(255) DEFAULT NULL COMMENT '随路数据',
    `main_menu_push_key`  VARCHAR(255) DEFAULT NULL COMMENT '主话单推送的key',
    `sec_menu_push_key`   VARCHAR(255) DEFAULT NULL COMMENT '子话单推送的key',
    `create_time`         datetime     DEFAULT NULL COMMENT '创建时间 (yyyy-MM-dd HH:mm:ss)',
    PRIMARY KEY (`log_id`) USING BTREE,
    KEY `idx_call_id` (`call_id`) USING BTREE
) ENGINE = INNODB COMMENT = '通道变量表 ${company_code}_${month}';

# 计费话单表
# CREATE TABLE IF NOT EXISTS `acr_record_${vcc_id}_${month}`
# (
#     `streamnumber`       varchar(50) NOT NULL COMMENT '序列号',
#     `servicekey`         varchar(11)  DEFAULT NULL COMMENT '业务关键字\r\n点击拨号servicekey＝900001\r\nsip直拨servicekey＝900002电话会议（）\r\n回呼Referto呼转话单servicekey＝900003(呼转)\r\n回呼 servicekey＝900004\r\n电话会议 servicekey＝900005\r\n国家6位省级行政区划表（如北京110000，天津120000）',
#     `callcost`           int(11)      DEFAULT NULL COMMENT '通话费用，单位：人民币分',
#     `calledpartynumber`  varchar(64)  DEFAULT NULL COMMENT '被叫号码，即用户拨叫的号码\r\n对于主叫话单，填写主叫实际拨打的号码\r\n对于被叫话单，填写被叫计费的16位分机号\r\n对于前转话单，填写前转流程处理前的被叫号码',
#     `callingpartynumber` varchar(64)  DEFAULT NULL COMMENT '主叫号码 \r\n对于主叫话单，填写主叫计费的16位分机号\r\n对于被叫话单，填写主叫号码，即INVITE的PAI消息头，如果PAI消息头为空，则填写INVITE的From消息头\r\n对于前转话单，填写前转计费的16位分机号\r\n对于被叫话单，填写主叫号码，即INVITE的PAI消息头，如果PAI消息头为空，则填写INVITE的From消息头\r\n对于前转话单，填写前转计费的16位分机号\r\n',
#     `chargemode`         smallint(6)  DEFAULT NULL COMMENT '计费模式， 1：计费 ',
#     `specificchargedpar` varchar(64)  DEFAULT NULL COMMENT '计费号码\r\n对于主叫话单，填写主叫计费分机的显示号码\r\n对于被叫话单，填写被叫计费分机的显示号码\r\n对于前转话单，填写前转计费分机的显示号码',
#     `translatednumber`   varchar(64)  DEFAULT NULL COMMENT '翻译号码，即用户实际接通的号码。\r\n对于主叫话单，填写主叫流程处理后的被叫号码\r\n对于被叫话单，填写被叫流程处理后的被叫号码\r\n对于前转话单，填写前转流程处理后的被叫号码',
#     `startdateandtime`   varchar(64)  DEFAULT NULL COMMENT '开始时间，格式：yyyymmddhhmmss',
#     `stopdateandtime`    varchar(64)  DEFAULT NULL COMMENT '结束时间，格式：yyyymmddhhmmss',
#     `duration`           int(11)      DEFAULT NULL COMMENT '通话时长，单位：秒',
#     `chargeclass`        int(11)      DEFAULT NULL COMMENT '计费类别',
#     `transparentparamet` varchar(255) DEFAULT NULL COMMENT '透明参数',
#     `calltype`           int(11)      DEFAULT NULL COMMENT '呼叫类型：（目前未使用）\r\n0:正常呼叫\r\n13:计费码呼叫',
#     `callersubgroup`     varchar(64)  DEFAULT NULL COMMENT '坐席工号',
#     `calleesubgroup`     varchar(64)  DEFAULT NULL COMMENT '网关名称',
#     `acrcallid`          varchar(40)  DEFAULT NULL COMMENT '当前呼叫的CallID',
#     `oricallednumber`    varchar(64)  DEFAULT NULL COMMENT '原始被叫\r\n对于主叫话单，填写原始INVITE消息的To消息头\r\n对于被叫话单，填写原始INVITE消息的To消息头\r\n对于前转话单，填写原始INVITE消息的To消息头',
#     `oricallingnumber`   varchar(64)  DEFAULT NULL COMMENT '原始主叫\r\n对于主叫话单，填写原始INVITE消息的From消息头\r\n对于被叫话单，填写原始INVITE消息的From消息头\r\n对于前转话单，填写原始INVITE消息的From消息头',
#     `callerpnp`          varchar(24)  DEFAULT NULL COMMENT '主叫短号（暂时未用）',
#     `calleepnp`          varchar(24)  DEFAULT NULL COMMENT '叫短号（暂时未用）',
#     `reroute`            int(11)      DEFAULT NULL COMMENT '重路由类型（目前未使用）',
#     `groupnumber`        varchar(32)  DEFAULT NULL COMMENT '集团号',
#     `callcategory`       int(11)      DEFAULT NULL COMMENT '点击拨号的呼叫顺序：1：第一通呼叫， 2：第二通呼叫；默认值：1。',
#     `chargetype`         int(11)      DEFAULT NULL COMMENT '话单类型：\r\n0：市话\r\n1：国内长途\r\n2：国际长途',
#     `userpin`            varchar(8)   DEFAULT NULL COMMENT '计费码',
#     `acrtype`            int(11)      DEFAULT NULL COMMENT '呼叫类型，指发端或终端\r\n1：主叫话单\r\n2：被叫话单\r\n3：前转话单\r\n100：质检\r\n注：servicekey=900003 的referTo转移话单的时候，  ACRTYPE 这个字段的值填的是  3：前转话单 ；ACRTYPE=3 前传话单是对于CTD 来说跟主叫是一样，会产生话费。',
#     `videocallflag`      int(11)      DEFAULT NULL COMMENT '0非视频，1是视频，默认0',
#     `serviceid`          varchar(64)  DEFAULT NULL,
#     `forwardnumber`      varchar(64)  DEFAULT NULL COMMENT '通话Uuid',
#     `extforwardnumber`   varchar(64)  DEFAULT NULL COMMENT '振铃时间',
#     `srfmsgid`           varchar(255) DEFAULT NULL COMMENT '录音地址',
#     `msserver`           varchar(40)  DEFAULT NULL COMMENT '所在的媒体服务器名称',
#     `begintime`          varchar(24)  DEFAULT NULL COMMENT '呼叫开始时间，时间戳，\r\n例如：20140401091026.759',
#     `releasecause`       int(11)      DEFAULT NULL COMMENT '结束码\r\n0 应答后主叫挂机\r\n1 应答后被叫挂机\r\n10 应答前主叫放弃 （通话时长都是0）\r\n99 未接通，分辨不出来原因\r\n>=300被叫未接通错误码 （通话时长都是0）\r\n结束码\r\n0 应答后主叫挂机\r\n1 应答后被叫挂机\r\n10 应答前主叫放弃 （通话时长都是0）\r\n99 未接通，分辨不出来原因\r\n>=300被叫未接通错误码 （通话时长都是0）',
#     `releasereason`      varchar(128) DEFAULT NULL COMMENT '结束原因值',
#     `areanumber`         varchar(8)   DEFAULT NULL COMMENT '区号',
#     `calledareacode`     varchar(20)  DEFAULT NULL COMMENT '呼入时间',
#     `localorlong`        varchar(20)  DEFAULT NULL COMMENT '市话或长途（0市话，1长途）',
#     `id`                 int(11)      DEFAULT '0',
#     `dtmfkey`            varchar(11)  DEFAULT NULL,
#     `callintime`         varchar(255) DEFAULT NULL COMMENT '呼入时间',
#     PRIMARY KEY (`streamnumber`),
#     KEY `index_acr_record_cutstreamnumber` (`startdateandtime`, `chargeclass`)
# ) ENGINE = InnoDB COMMENT = '计费表 ${vcc_id}_${month}';


# 外呼任务话单
CREATE TABLE IF NOT EXISTS `cloudcc_outbound_call_task_cdr_${company_code}_${month}`
(
    `call_id`            bigint(20) NOT NULL COMMENT '话单id',
    `task_id`            varchar(128) DEFAULT NULL COMMENT '任务id',
    `task_type`          int(1) DEFAULT NULL COMMENT '任务类型: 1-IVR, 2-语音通知, 3-预测外呼, 4-预览外呼',
    `company_code`       varchar(128) DEFAULT NULL COMMENT '企业id',
    `main_call_id`       varchar(128) DEFAULT NULL COMMENT '主话单id',
    `sub_uuid`           varchar(128) DEFAULT NULL COMMENT '子话单通话uuid',
    `number_id`          varchar(128) DEFAULT NULL COMMENT '号码id',
    `caller_number`      varchar(32)  DEFAULT NULL COMMENT '主叫号码',
    `display_number`     varchar(32)  DEFAULT NULL COMMENT '外显号码(平台号码)',
    `client_number`      varchar(32)  DEFAULT NULL COMMENT '被叫号码(客户号码)',
    `call_start_time`    datetime     DEFAULT NULL COMMENT '通话开始时间',
    `client_answer_time` datetime     DEFAULT NULL COMMENT '客户接通时间',
    `agent_answer_time`  datetime     DEFAULT NULL COMMENT '坐席接通时间',
    `hangup_time`        datetime     DEFAULT NULL COMMENT '挂断时间',
    `duration`           int(1)       DEFAULT NULL COMMENT '通话时长',
    `agent_id`           varchar(32)  DEFAULT NULL COMMENT '坐席id',
    `current_times`      int(1)       DEFAULT NULL COMMENT '当前呼叫次数',
    `answer_status`      tinyint(1)   DEFAULT NULL COMMENT '接通状态 1-已接通 0 -未接通',
    `fail_cause`         varchar(32)  DEFAULT NULL COMMENT '失败原因',
    `record_url`         varchar(512)  DEFAULT NULL COMMENT '录制文件',
    PRIMARY KEY (`call_id`) USING BTREE,
    KEY `idx_call_start_time` (`call_start_time`) USING BTREE,
    KEY `idx_task_id` (`task_id`, `number_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='外呼任务话单 ${company_code}_${month}';
