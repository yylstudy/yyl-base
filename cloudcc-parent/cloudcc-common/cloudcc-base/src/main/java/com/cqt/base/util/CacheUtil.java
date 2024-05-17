package com.cqt.base.util;

import cn.hutool.core.text.StrFormatter;
import com.cqt.base.contants.CacheConstant;
import com.cqt.base.contants.CallTaskCacheConstant;
import com.cqt.base.contants.LockCacheConstant;
import com.cqt.base.enums.agent.AgentServiceModeEnum;

/**
 * @author linshiqiang
 * date:  2023-07-04 17:37
 */
public class CacheUtil {

    /**
     * 话单生成加锁
     * cdrGenerateLock:{companyCode}:{mainCallId}
     */
    public static String getCdrGenerateLockKey(String companyCode, String mainCallId) {
        return StrFormatter.format(LockCacheConstant.CDR_GENERATE_LOCK, companyCode, mainCallId);
    }

    /**
     * 坐席签出加锁
     * checkoutLock:{companyCode}:{agentId}
     */
    public static String getCheckoutLockKey(String companyCode, String agentId) {
        return StrFormatter.format(LockCacheConstant.CHECKOUT_LOCK, companyCode, agentId);
    }

    /**
     * 坐席状态修改lock
     * agentStatusUpdateLock:{companyCode}:{agentId}
     */
    public static String getAgentStatusUpdateLockKey(String companyCode, String agentId) {
        return StrFormatter.format(LockCacheConstant.AGENT_STATUS_UPDATE_LOCK, companyCode, agentId);
    }

    /**
     * uuid上下文状态修改lock
     * agentStatusUpdateLock:{uuid}
     */
    public static String getUuidContextUpdateLockKey(String uuid) {
        return StrFormatter.format(LockCacheConstant.UUID_CONTEXT_UPDATE_LOCK, uuid);
    }

    /**
     * 咨询lock
     * consultLock:{uuid}
     */
    public static String getConsultLockKey(String uuid) {
        return StrFormatter.format(LockCacheConstant.CONSULT_LOCK, uuid);
    }

    /**
     * 通话事件消息幂等处理
     * msgIdempotentLock:{uuid}:{status}
     */
    public static String getMessageIdempotentLockKey(String uuid, String status) {
        return StrFormatter.format(LockCacheConstant.MESSAGE_IDEMPOTENT_LOCK, uuid, status);
    }

    /**
     * 排队定时任务执行锁
     * 同时只有一台设备执行排队逻辑
     */
    public static String getQueueScheduleLockKey() {
        return LockCacheConstant.QUEUE_SCHEDULE_LOCK_KEY;
    }

    /**
     * 排队 按企业id加锁
     * userQueueLock:{company_code}
     */
    public static String getUserQueueLockKey(String companyCode) {
        return StrFormatter.format(LockCacheConstant.USER_QUEUE_LOCK_KEY, companyCode);
    }

    /**
     * 事件消息消费, 按uuid加锁
     * eventConsumerLock:{uuid}
     * ttl 60s
     */
    public static String getEventMessageConsumerLockKey(String companyCode) {
        return StrFormatter.format(LockCacheConstant.EVENT_MESSAGE_CONSUMER_LOCK_KEY, companyCode);
    }

    public static String getThreeWayRemainCallLockKey(String companyCode, String callId) {
        return StrFormatter.format(LockCacheConstant.THREE_WAY_REMAIN_CALL_LOCK_KEY, companyCode, callId);
    }

    /**
     * 企业基本信息
     * key: cloudcc:companyInfo:{companyCode}
     * value: json
     */
    public static String getCompanyInfoKey(String companyCode) {
        return StrFormatter.format(CacheConstant.COMPANY_INFO_KEY, companyCode);
    }

    /**
     * 坐席与分机的绑定关系
     * key: cloudcc:agentBindExt:{agentId}
     * value: ext_id
     */
    public static String getAgentBindExtKey(String companyCode, String agentId) {
        return StrFormatter.format(CacheConstant.AGENT_BIND_EXT_KEY, agentId);
    }

    /**
     * 坐席与分机的绑定关系
     * key: cloudcc:agentBindExt:{extId}
     * value: agentId
     */
    public static String getExtBindAgentKey(String companyCode, String extId) {
        return StrFormatter.format(CacheConstant.EXT_BIND_AGENT_KEY, extId);
    }

    /**
     * 坐席基本信息
     * key: cloudcc:agentInfo:{agentId}
     * value: json
     */
    public static String getAgentInfoKey(String agentId) {
        return StrFormatter.format(CacheConstant.AGENT_INFO_KEY, agentId);
    }

    /**
     * 分机基本信息
     * key: cloudcc:extInfo:{extId}
     * value: json
     */
    public static String getExtInfoKey(String extId) {
        return StrFormatter.format(CacheConstant.EXT_INFO_KEY, extId);
    }

    /**
     * 分机实时状态
     */
    public static String getExtStatusKey(String companyCode, String extId) {
        return StrFormatter.format(CacheConstant.EXT_STATUS_KEY, companyCode, extId);
    }

    /**
     * 所有分机实时状态
     */
    public static String getAllExtStatusKey(String companyCode) {
        return StrFormatter.format(CacheConstant.ALL_EXT_STATUS_KEY, companyCode);
    }

    /**
     * 通话uuid之间关联关系
     */
    public static String getCallUuidKey(String companyCode, String uuid) {
        return StrFormatter.format(CacheConstant.CALL_UUID_KEY, companyCode, uuid);
    }

    /**
     * 坐席的状态string
     * redis hash
     * key: agentStatus:{companyCode}:{agentId}
     * value: status + timestamp + uuid
     */
    public static String getAgentStatusKey(String companyCode, String agentId) {
        return StrFormatter.format(CacheConstant.AGENT_STATUS_KEY, companyCode, agentId);
    }

    /**
     * 全部坐席的状态hash
     * redis hash
     * key: allAgentStatus:{companyCode}
     * item: agentId
     * value: json
     */
    public static String getAllAgentStatusKey(String companyCode) {
        return StrFormatter.format(CacheConstant.ALL_AGENT_STATUS_KEY, companyCode);
    }

    /**
     * 呼入客户排队等级队列 hash
     * key: userQueueLevel:{companyCode}
     * value: @see com.cqt.model.queue.dto.UserQueueUpDTO
     */
    public static String getUserQueueLevelKey(String companyCode) {
        return StrFormatter.format(CacheConstant.USER_QUEUE_LEVEL_KEY, companyCode);
    }

    /**
     * 闲时队列
     * key: idleQueue:{companyCode}
     * value: @see com.cqt.model.queue.dto.UserQueueUpDTO
     */
    public static String getUserQueueIdleKey(String companyCode, String skillId) {
        return StrFormatter.format(CacheConstant.USER_QUEUE_IDLE_KEY, companyCode, skillId);
    }

    /**
     * 呼入客户排队等级队列 hash
     * key: userQueueLevel:{companyCode}:{skillId}
     * value: @see com.cqt.model.queue.dto.UserQueueUpDTO
     */
    public static String getUserQueueLevelSkillKey(String companyCode, String skillId) {
        return StrFormatter.format(CacheConstant.USER_QUEUE_LEVEL_SKILL_KEY, companyCode, skillId);
    }

    /**
     * 企业空闲坐席队列
     * 1-客服型 2-外呼型
     * redis hash
     */
    public static String getCompanyAgentQueueKey(String companyCode, AgentServiceModeEnum agentMode, boolean free) {
        Integer code = agentMode.getCode();
        if (free) {
            return StrFormatter.format(CacheConstant.COMPANY_FREE_AGENT_KEY_BY_MODE, companyCode, code);
        }
        return StrFormatter.format(CacheConstant.COMPANY_OFFLINE_AGENT_KEY_BY_MODE, companyCode, code);
    }

    /**
     * 技能 - 空闲坐席队列
     * 1-客服型 2-外呼型
     * redis hash
     */
    public static String getSkillAgentQueueKey(String companyCode, String skillId,
                                               AgentServiceModeEnum agentMode, boolean free) {
        Integer code = agentMode.getCode();
        if (free) {
            return StrFormatter.format(CacheConstant.SKILL_FREE_AGENT_KEY_BY_MODE, companyCode, code, skillId);
        }
        return StrFormatter.format(CacheConstant.SKILL_OFFLINE_AGENT_KEY_BY_MODE, companyCode, code, skillId);
    }

    /**
     * 坐席的权值信息
     * redis string
     * key: agentWithWeightInfo:{agentId}
     * value: json
     * 坐席权值和技能权值信息json
     */
    public static String getAgentWithWeightInfoKey(String agentId) {
        return StrFormatter.format(CacheConstant.AGENT_WITH_WEIGHT_INFO_KEY, agentId);
    }

    /**
     * 技能的信息
     * redis string
     * key: skillInfo:{skillId}
     * value: 排队超时时间, 等待音
     */
    public static String getSkillInfoKey(String skillId) {
        return StrFormatter.format(CacheConstant.SKILL_INFO_KEY, skillId);
    }

    /**
     * IVR服务配置
     * redis string
     * key: cloudcc:service:ivr:{ivr_service_id}
     * value: json
     */
    public static String getIvrServiceKey(String ivrServiceId) {
        return StrFormatter.format(CacheConstant.IVR_SERVICE_KEY, ivrServiceId);
    }
    
    /**
     * 文件id与目录对应关系 string
     * key: cloudcc:fileId:{company_code}:{file_id}
     * value: file_path(底层文件路径)
     */
    public static String getFileIdKey(String companyCode, String fileId) {
        return StrFormatter.format(CacheConstant.FILE_ID_KEY, companyCode, fileId);

    }

    /**
     * 企业的企业id集合 set
     * value: company_code
     */
    public static String getEnableCompanyCodeKey() {
        return CacheConstant.ENABLE_COMPANY_CODE_KEY;
    }

    /**
     * 号码信息缓存string
     * key: cloudcc:numberInfo:{number}:{company_code}
     * value: json
     */
    public static String getNumberInfoKey(String number) {
        return StrFormatter.format(CacheConstant.NUMBER_INFO_KEY, number);
    }

    /**
     * 运营级黑名单
     * redis string
     * key: cloudcc:blacklist:global:{number}
     * value: 通话类型(1-呼入 0-呼出 2-呼出+呼入)
     */
    public static String getBlacklistGlobalKey(String number) {
        return StrFormatter.format(CacheConstant.BLACKLIST_GLOBAL_KEY, number);
    }

    /**
     * 企业级黑名单
     * redis string
     * key: cloudcc:blacklist:company:{companyCode}:{number}
     * value: 通话类型(1-呼入 0-呼出 2-呼出+呼入)
     */
    public static String getBlacklistCompanyKey(String companyCode, String number) {
        return StrFormatter.format(CacheConstant.BLACKLIST_COMPANY_KEY, companyCode, number);
    }

    /**
     * 客户来电优先级
     * redis string
     * key: cloudcc:clientPriority::{companyCode}:{number}
     * value: 等级
     */
    public static String getClientPriorityKey(String companyCode, String number) {
        return StrFormatter.format(CacheConstant.CALLER_NUMBER_PRIORITY_KEY, companyCode, number);
    }

    /**
     * 坐席签入记录
     * key: cloudcc:checkInRecord:{sysAgentId}
     * hKey: clientType
     * value: （包含设备标识可能是IP数组, 以及签入时间戳）
     * ttl: 24h
     */
    public static String getCheckInRecordKey(String sysAgentId) {
        return String.format(CacheConstant.CHECK_IN_RECORD_KEY, sysAgentId);
    }

    /**
     * 事后处理任务执行机器 string
     * key: arrangeTask:{companyCode}:{agentId}
     * value: serverIp
     * ttl: 12h
     */
    public static String getArrangeTaskKey(String companyCode, String agentId) {
        return StrFormatter.format(CacheConstant.ARRANGE_TASK_KEY, companyCode, agentId);
    }

    /**
     * uuid挂断标志
     * key: uuidHangup:{company_code}:{uuid}
     * value: timestamp
     * ttl: 12h
     */
    public static String getUuidHangupFlagKey(String companyCode, String uuid) {
        return StrFormatter.format(CacheConstant.UUID_HANGUP_FLAG_KEY, companyCode, uuid);
    }

    /**
     * 话单连接 set
     * key: cdrLink:{company_code}:{mainCallId}
     * value: {主叫uuid}:{被叫uuid}
     * ttl: 24h
     */
    public static String getCdrLinkKey(String companyCode, String mainCallId) {
        return StrFormatter.format(CacheConstant.CDR_LINK_KEY, companyCode, mainCallId);
    }

    /**
     * 话单计费号码 string
     * key: cdr:{company_code}:{mainCallId}
     * value: 计费号码
     * ttl: 24h
     */
    public static String getCdrChargeKey(String companyCode, String mainCallId) {
        return StrFormatter.format(CacheConstant.CDR_CHARGE_KEY, companyCode, mainCallId);
    }

    /**
     * 话单生成标志 string
     * cdrGenerateFlag:{companyCode}:{mainCallId}
     * ttl: 24h
     */
    public static String getCdrGenerateFlagKey(String companyCode, String mainCallId) {
        return StrFormatter.format(CacheConstant.CDR_GENERATE_FLAG, companyCode, mainCallId);
    }

    /**
     * 有ivr或满意度侧通话的通道变量 string
     * type: callInIVR transIVR satisfaction
     * key: cdrChannelData:{company_code}:{uuid}
     * value: json
     * ttl: 24h
     */
    public static String getCdrChannelDataKey(String companyCode, String uuid) {
        return StrFormatter.format(CacheConstant.CDR_CHANNEL_DATA_KEY, companyCode, uuid);
    }

    /**
     * 话单连接 set
     * key: inCallNumbers:{company_code}:{mainCallId}
     * value: number
     * ttl: 24h
     */
    public static String getInCallNumbersKey(String companyCode, String mainCallId) {
        return StrFormatter.format(CacheConstant.IN_CALL_NUMBERS_KEY, companyCode, mainCallId);
    }

    /**
     * 放音标志 string
     * playbackFlag:{companyCode}:{uuid}
     * ttl: 24h
     */
    public static String getPlaybackFlagKey(String companyCode, String uuid) {
        return StrFormatter.format(CacheConstant.PLAYBACK_FLAG, companyCode, uuid);
    }

    /**
     * 坐席通话时间统计 zset
     * agentCallTime:{companyCode}:{date}
     * ttl: 48h
     */
    public static String getAgentCallTimeKey(String companyCode, String date) {
        return StrFormatter.format(CacheConstant.AGENT_CALL_TIME_KEY, companyCode, date);
    }

    /**
     * 坐席通话次数统计 zset
     * agentCallTime:{companyCode}:{date}
     * ttl: 48h
     */
    public static String getAgentCallCountKey(String companyCode, String date) {
        return StrFormatter.format(CacheConstant.AGENT_CALL_COUNT_KEY, companyCode, date);
    }

    /**
     * 技能坐席通话时间统计 zset
     * agentCallTime:{companyCode}:{skill}:{date}
     * ttl: 48h
     */
    public static String getAgentSkillCallTimeKey(String companyCode, String skillId, String date) {
        return StrFormatter.format(CacheConstant.AGENT_SKILL_CALL_TIME_KEY, companyCode, skillId, date);
    }

    /**
     * 技能坐席通话次数统计 zset
     * agentCallTime:{companyCode}:{skill}:{date}
     * ttl: 48h
     */
    public static String getAgentSkillCallCountKey(String companyCode, String skillId, String date) {
        return StrFormatter.format(CacheConstant.AGENT_SKILL_CALL_COUNT_KEY, companyCode, skillId, date);
    }

    /**
     * ivr外呼号码列表
     * redis zset
     * key: cloudcc:ivrTask:{task_id}
     * member: {number_id}:{number}
     * score: 0
     */
    public static String getIvrTaskNumberKey(String taskId) {
        return StrFormatter.format(CallTaskCacheConstant.IVR_TASK_NUMBER_KEY, taskId);
    }

    /**
     * ivr外呼号码呼叫状态
     * redis hash
     * key: cloudcc:ivrTaskCallStatus:{task_id}
     * value: {timestamp}
     */
    public static String getIvrTaskNumberCallStatusKey(String taskId) {
        return StrFormatter.format(CallTaskCacheConstant.IVR_TASK_NUMBER_CALL_STATUS_KEY, taskId);
    }

    /**
     * 预测外呼号码列表
     * redis zset
     * key: cloudcc:predictTask:{task_id}
     * member: {number_id}:{number}
     * score: 0
     */
    public static String getPredictTaskNumberKey(String taskId) {
        return StrFormatter.format(CallTaskCacheConstant.PREDICT_TASK_NUMBER_KEY, taskId);
    }

    /**
     * 预测外呼-坐席列表
     * redis set
     * key: cloudcc:predictTaskAgents:{task_id}
     * member: {agent_id}
     */
    public static String getPredictTaskAgentKey(String taskId) {
        return StrFormatter.format(CallTaskCacheConstant.PREDICT_TASK_AGENT_KEY, taskId);
    }

    /**
     * 预测外呼号码呼叫状态
     * redis hash
     * key: cloudcc:ivrTaskCallStatus:{task_id}
     * value: {timestamp}
     */
    public static String getPredictTaskNumberCallStatusKey(String taskId) {
        return StrFormatter.format(CallTaskCacheConstant.PREDICT_TASK_NUMBER_CALL_STATUS_KEY, taskId);
    }

    /**
     * 外呼任务锁
     * outCallTaskLock:{task_id}
     */
    public static String getOutCallTaskLockKey(String taskId) {
        return StrFormatter.format(LockCacheConstant.OUT_CALL_TASK_LOCK, taskId);
    }

    /**
     * 外呼锁 内线
     * originateLock:{caller_number}
     * ttl 10s
     */
    public static String getOriginateLockKey(String calleeNumber) {
        return StrFormatter.format(LockCacheConstant.ORIGINATE_LOCK, calleeNumber);
    }

    /**
     * 平台默认音效配置
     */
    public static String getPlatformDefaultToneKey(String type) {
        return StrFormatter.format(CacheConstant.PLATFORM_DEFAULT_TONE_KEY, type);
    }

    /**
     * uuid 录制文件保存
     */
    public static String getUuidRecordFileKey(String companyCode, String uuid) {
        return StrFormatter.format(CacheConstant.UUID_RECORD_FILE_KEY, companyCode, uuid);
    }

    /**
     * 坐席token
     * sdkToken:{company_code}:{os}:{agent_id}
     */
    public static String getTokenKey(String companyCode, String os, String agentId) {
        return StrFormatter.format(CacheConstant.TOKEN_KEY, companyCode, os, agentId);
    }

    /**
     * token对应坐席信息
     */
    public static String getTokenInfoKey(String token) {
        return StrFormatter.format(CacheConstant.TOKEN_INFO_KEY, token);
    }

    /**
     * 坐席 发起咨询标志
     * agentConsult:{uuid}
     * ttl: 1d
     */
    public static String getAgentConsultKey(String uuid) {
        return StrFormatter.format(CacheConstant.AGENT_CONSULT_KEY, uuid);
    }

    /**
     * 已外呼标志 setnx
     * key: outCallFlag:{number_id}
     * value: 1
     * ttl 300s
     */
    public static String getOutCallFlagKey(String numberId) {
        return StrFormatter.format(CacheConstant.OUTBOUND_CALL_FLAG_KEY, numberId);
    }

}
