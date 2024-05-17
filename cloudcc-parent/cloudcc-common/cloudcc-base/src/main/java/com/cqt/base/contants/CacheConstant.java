package com.cqt.base.contants;

/**
 * @author linshiqiang
 * date:  2023-07-04 9:06
 * redis 缓存键定义
 */
public interface CacheConstant {

    Long TTL = 24L;

    Long TTL_2D = 48L;

    String PREFIX = "cloudcc:";

    /**
     * 企业空闲坐席队列
     * 1-客服型 2-外呼型
     * redis hash
     * key: freeAgent:{companyCode}:{agent_service_mode}
     * value: @see com.cqt.model.queue.dto.FreeAgentQueueDTO
     */
    String COMPANY_FREE_AGENT_KEY_BY_MODE = PREFIX + "freeAgents:{}:{}";

    /**
     * 技能id的空闲坐席队列
     * redis hash
     * key: freeAgent:{companyCode}:{agent_service_mode}:{skillId}
     * value: @see com.cqt.model.queue.dto.FreeAgentQueueDTO
     */
    String SKILL_FREE_AGENT_KEY_BY_MODE = PREFIX + "freeAgents:{}:{}:{}";

    /**
     * 企业离线坐席队列, 配置了手机号
     * redis hash
     * key: offlineAgent:{companyCode}:{agent_service_mode}
     * value: @see com.cqt.model.queue.dto.FreeAgentQueueDTO
     */
    String COMPANY_OFFLINE_AGENT_KEY_BY_MODE = PREFIX + "offlineAgents:{}:{}";

    /**
     * 技能id的离线坐席队列, 配置了手机号
     * redis hash
     * key: offlineAgent:{companyCode}:{agent_service_mode}:{skillId}
     * value: @see com.cqt.model.queue.dto.FreeAgentQueueDTO
     */
    String SKILL_OFFLINE_AGENT_KEY_BY_MODE = PREFIX + "offlineAgents:{}:{}:{}";

    /**
     * 呼入客户排队等级队列 hash
     * key: userQueueLevel:{companyCode}
     * value: @see com.cqt.model.queue.dto.UserQueueUpDTO
     */
    String USER_QUEUE_LEVEL_KEY = PREFIX + "userQueueLevel:{}";

    /**
     * 闲时队列
     * key: idleQueue:{companyCode}
     * value: @see com.cqt.model.queue.dto.UserQueueUpDTO
     */
    String USER_QUEUE_IDLE_KEY = PREFIX + "idleQueue:{}:{}";

    /**
     * 呼入客户排队等级队列 hash
     * key: userQueueLevel:{companyCode}:{skillId}
     * value: @see com.cqt.model.queue.dto.UserQueueUpDTO
     */
    String USER_QUEUE_LEVEL_SKILL_KEY = PREFIX + "userQueueLevel:{}:{}";

    /**
     * 坐席的状态string
     * redis string
     * key: agentStatus:{companyCode}:{agentId}
     * value: status + timestamp + uuid
     */
    String AGENT_STATUS_KEY = PREFIX + "agentStatus:{}:{}";

    /**
     * 全部坐席的状态hash
     * redis hash
     * key: allAgentStatus:{companyCode}
     * item: agentId
     * value: json
     */
    String ALL_AGENT_STATUS_KEY = PREFIX + "allAgentStatus:{}";

    /**
     * 分机的状态
     * redis string
     * key: extStatus:{companyCode}:{extId}
     * value: status + timestamp + uuid
     */
    String EXT_STATUS_KEY = PREFIX + "extStatus:{}:{}";

    /**
     * 企业所有分机的状态
     * redis hash
     * key: allExtStatus:{companyCode}
     * value: status + timestamp + uuid
     */
    String ALL_EXT_STATUS_KEY = PREFIX + "allExtStatus:{}";

    /**
     * 坐席的权值信息
     * redis string
     * key: agentWithWeightInfo:{agentId}
     * value: json
     * 坐席权值和技能权值信息json
     * <p>
     * {
     * agent: [
     * {
     * skillId: weight
     * }
     * ]
     * skill: [
     * {
     * skillId: weight
     * }
     * ]
     * }
     */
    String AGENT_WITH_WEIGHT_INFO_KEY = PREFIX + "agentWithSkillWeightInfo:{}";

    /**
     * 技能的信息
     * redis string
     * key: skillInfo:{skillId}
     * value: 排队超时时间, 等待音
     */
    String SKILL_INFO_KEY = PREFIX + "skillInfo:{}";

    /**
     * IVR服务配置
     * redis string
     * key: cloudcc:service:ivr:{ivr_service_id}
     * value: json
     */
    String IVR_SERVICE_KEY = PREFIX + "service:ivr:{}";

    /**
     * 文件id与目录对应关系 string
     * key: cloudcc:fileId:{company_code}:{file_id}
     * value: file_path(底层文件路径)
     */
    String FILE_ID_KEY = PREFIX + "fileId:{}:{}";

    /**
     * 通话uuid之间关联关系
     * redis string
     * 某一方挂断, 拒接或结束, 收到挂断事件后挂断关联的uuid
     * key: callUuid:{companyCode}:{uuid}
     * value:
     * {
     * number: 分机id或号码,
     * request_id: xxx,
     * time: xxxx,   uuid: xxx,
     * role: '主叫, 被叫, 监听坐席, 转接坐席, 耳语坐席..',
     * relation: [{
     * number: 分机id或号码,
     * request_id: xxx,
     * time: xxxx,
     * uuid: xxx,
     * role: '主叫, 被叫, 监听坐席, 转接坐席, 耳语坐席..'
     * }]
     * }
     */
    String CALL_UUID_KEY = PREFIX + "callUuid:{}:{}";

    /**
     * 企业基本信息
     * key: cloudcc:companyInfo:{companyCode}
     * value: json
     */
    String COMPANY_INFO_KEY = PREFIX + "companyInfo:{}";

    /**
     * 坐席与分机的绑定关系
     * key: cloudcc:agentBindExt:{agentId}
     * value: ext_id
     */
    String AGENT_BIND_EXT_KEY = PREFIX + "agentBindExt:{}";

    /**
     * 分机与坐席的绑定关系
     * key: cloudcc:agentBindExt:{extId}
     * value: agent_id
     */
    String EXT_BIND_AGENT_KEY = PREFIX + "extBindAgent:{}";

    /**
     * 坐席基本信息
     * key: cloudcc:agentInfo:{agentId}
     * value: json
     */
    String AGENT_INFO_KEY = PREFIX + "agentInfo:{}";

    /**
     * 分机基本信息
     * key: cloudcc:extInfo:{extId}
     * value: json
     */
    String EXT_INFO_KEY = PREFIX + "extInfo:{}";

    /**
     * 企业的企业id集合 set
     * value: company_code
     */
    String ENABLE_COMPANY_CODE_KEY = PREFIX + "enableCompanyCode";

    /**
     * 号码信息缓存string
     * key: cloudcc:numberInfo:{number}
     * value: json
     */
    String NUMBER_INFO_KEY = PREFIX + "numberInfo:{}";

    /**
     * 运营级黑名单
     * redis string
     * key: cloudcc:blacklist:operate:{number}
     * value: 通话类型(1-呼入 0-呼出 2-呼出+呼入)
     */
    String BLACKLIST_GLOBAL_KEY = PREFIX + "blacklist:operate:{}";

    /**
     * 企业级黑名单
     * redis string
     * key: cloudcc:blacklist:company:{companyCode}:{number}
     * value: 通话类型(1-呼入 0-呼出 2-呼出+呼入)
     */
    String BLACKLIST_COMPANY_KEY = PREFIX + "blacklist:company:{}:{}";

    /**
     * 客户来电优先级
     * redis string
     * key: cloudcc:clientPriority::{companyCode}:{number}
     * value: 等级
     */
    String CALLER_NUMBER_PRIORITY_KEY = PREFIX + "clientPriority:{}:{}";

    /**
     * 坐席签入记录
     * key: cloudcc:checkInRecord:{sysAgentId}
     * hKey: clientType
     * value: （包含设备标识可能是IP数组, 以及签入时间戳）
     * ttl: 24h
     */
    String CHECK_IN_RECORD_KEY = PREFIX + "checkInRecord:%s";

    /**
     * 事后处理任务执行机器 string
     * key: arrangeTask:{companyCode}:{agentId}
     * value: serverIp
     * ttl: 12h
     */
    String ARRANGE_TASK_KEY = PREFIX + "arrangeTask:{}:{}";

    /**
     * uuid挂断标志
     * key: uuidHangup:{company_code}:{uuid}
     * value: timestamp
     * ttl: 12h
     */
    String UUID_HANGUP_FLAG_KEY = PREFIX + "uuidHangup:{}:{}";

    /**
     * 话单连接 set
     * key: cdrLink:{company_code}:{mainCallId}
     * value: {主叫uuid}:{被叫uuid}
     * ttl: 24h
     */
    String CDR_LINK_KEY = PREFIX + "cdrLink:{}:{}";

    /**
     * 话单计费号码 string
     * key: cdr:{company_code}:{mainCallId}
     * value: 计费号码
     * ttl: 24h
     */
    String CDR_CHARGE_KEY = PREFIX + "cdrCharge:{}:{}";

    /**
     * 话单生成标志 string
     * cdrGenerateFlag:{companyCode}:{mainCallId}
     * ttl: 24h
     */
    String CDR_GENERATE_FLAG = CacheConstant.PREFIX + "cdrGenerateFlag:{}:{}";

    /**
     * 有ivr或满意度侧通话的通道变量 string
     * type: callInIVR transIVR satisfaction
     * key: cdrChannelData:{company_code}:{uuid}
     * value: json
     * ttl: 24h
     */
    String CDR_CHANNEL_DATA_KEY = PREFIX + "cdrChannelData:{}:{}";

    /**
     * 话单连接 set
     * key: inCallNumbers:{company_code}:{mainCallId}
     * value: number
     * ttl: 24h
     */
    String IN_CALL_NUMBERS_KEY = PREFIX + "inCallNumbers:{}:{}";

    /**
     * 放音标志 string
     * playbackFlag:{companyCode}:{uuid}
     * ttl: 24h
     */
    String PLAYBACK_FLAG = CacheConstant.PREFIX + "playbackFlag:{}:{}";

    /**
     * 坐席通话时间统计 zset
     * agentCallTime:{companyCode}:{date}
     * ttl: 48h
     */
    String AGENT_CALL_TIME_KEY = CacheConstant.PREFIX + "agentCallTime:{}:{}";

    /**
     * 坐席通话次数统计 zset
     * agentCallTime:{companyCode}:{date}
     * ttl: 48h
     */
    String AGENT_CALL_COUNT_KEY = CacheConstant.PREFIX + "agentCallCount:{}:{}";

    /**
     * 技能坐席通话时间统计 zset
     * agentCallTime:{companyCode}:{skill}:{date}
     * ttl: 48h
     */
    String AGENT_SKILL_CALL_TIME_KEY = CacheConstant.PREFIX + "agentSkillCallTime:{}:{}:{}";

    /**
     * 技能坐席通话次数统计 zset
     * agentCallTime:{companyCode}:{skill}:{date}
     * ttl: 48h
     */
    String AGENT_SKILL_CALL_COUNT_KEY = CacheConstant.PREFIX + "agentSkillCallCount:{}:{}:{}";

    /**
     * 平台默认音效配置
     * defaultTone:{type}
     * json
     * ttl: -1
     */
    String PLATFORM_DEFAULT_TONE_KEY = CacheConstant.PREFIX + "defaultTone:{}";

    /**
     * uuid 录制文件保存
     */
    String UUID_RECORD_FILE_KEY = PREFIX + "recordFile:{}:{}";

    /**
     * 坐席token
     * sdkToken:{company_code}:{os}:{agent_id}
     */
    String TOKEN_KEY = PREFIX + "sdkToken:{}:{}:{}";

    /**
     * token对应坐席信息
     * sdkToken:{token}
     */
    String TOKEN_INFO_KEY = PREFIX + "sdkToken:{}";

    /**
     * 坐席 发起咨询标志
     * agentConsult:{uuid}
     * ttl: 1d
     */
    String AGENT_CONSULT_KEY = PREFIX + "agentConsult:{}";

    /**
     * 已外呼标志 setnx
     * key: outCallFlag:{number_id}
     * value: 1
     * ttl 300s
     */
    String OUTBOUND_CALL_FLAG_KEY = PREFIX + "outCallFlag:{}";
}
