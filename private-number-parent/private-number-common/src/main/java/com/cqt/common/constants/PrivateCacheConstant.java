package com.cqt.common.constants;

/**
 * @author linshiqiang
 * @since 2021/7/7 16:15
 * 缓存key常量
 */
public interface PrivateCacheConstant {

    /**
     * AXE 分机号绑定统计-按地市
     * {vcc_id}:axeStats:{area_code}
     */
    String AXE_EXT_BIND_STATS = "{}:axeStats:{}";

    /**
     * AREA_LOCATION 监听nacos, 同步mysql加锁
     */
    String LOCK_AREA_LOCATION_INIT = "private:lock:changeAreaLocation";

    /**
     * 企业信息管理-业务配置
     * private:crop:business:info:{实体json}
     */
    String CORP_BUSINESS_INFO = "private:crop:business:info:%s";


    /**
     * bindIdKey: {vcc_id}:{type}:bind:{bind_id}
     * string
     */
    String BIND_ID_KEY = "%s:%s:bind:%s";

    /**
     * requestIdKey: {vcc_id}:{type}:request:{request_id}
     * string
     */
    String REQUEST_ID_KEY = "%s:%s:request:%s";

    /**
     * fs bindInfoKey: {vcc_id}:{type}:bind:{tel_a/tel_b}:{tel_x}
     * X号码和分机号bindInfoKey: {vcc_id}:{type}:bind:{tel_x}:{tel_x_ext}
     * string
     */
    String BIND_INFO_KEY = "{}:{}:bind:{}:{}";

    /**
     * setnx X号码和分机号bindInfoKey: {vcc_id}:uniqueExt:{area_code}:{tel_x}:{tel_x_ext}
     * string
     */
    String UNIQUE_EXT_BIND_KEY = "{}:uniqueExt:{}:{}:{}";

    /**
     * AX fs bindInfoKey: {vcc_id}:{type}:bind:{tel_x}
     * string
     */
    String X_BIND_INFO_KEY = "%s:%s:bind:%s";

    /**
     * AX/BX可用号码池初始化标识: {vcc_id}:{type}:pool:init:{area_code}:{tel_a/tel_b}
     * string
     */
    String INIT_FLAG_KEY = "%s:%s:pool:init:%s:%s";

    /**
     * AX/BX可用号码池: {vcc_id}:{type}:pool:{area_code}:{tel_a/tel_b}
     * set
     */
    String USABLE_X_POOL_KEY = "%s:%s:pool:%s:%s";

    String USABLE_X_POOL_SLOT_KEY = "{%s:%s:pool:%s}:%s";

    /**
     * axbn {vcc_id}:{type}:pool:{area_code}
     */
    String USABLE_AXBN_POOL_SLOT_KEY = "{%s:%s:pool:%s}";

    /**
     * X号码的未使用分机号池: {vcc_id}:{type}:pool:ext:{tel_x}
     * set
     */
    String USABLE_EXT_POOL_KEY = "%s:%s:pool:ext:%s";

    /**
     * 未使用X号码 list
     * X号码的未使用分机号池: {vcc_id}:{type}:pool:ext:{area_code}:{tel_x}
     */
    String USABLE_EXT_POOL_LIST_KEY = "{}:{}:pool:ext:{}:{}";

    /**
     * redisson分布式锁hash:
     * axb ax池初始化: {vcc_id}:{type}_lock_init_ax:{tel_a}
     * hash
     */
    String AXB_LOCK_INIT_AX_KEY = "%s:%s_lock_init_ax:%s";

    /**
     * axb bx池初始化: {vcc_id}:{type}_lock_init_ax:{tel_b}
     * hash
     */
    String AXB_LOCK_INIT_BX_KEY = "%s:%s_lock_init_bx:%s";

    /**
     * 企业信息键
     */
    String VCC_INFO_KEY = "private:vcc:info:%s";

    /**
     * axb单号码锁
     */
    String NUM_POOL_AXB_ONE_LOCK = "private:pool:axb:one:lock:%s";


    /**
     * axb批量号码锁
     */
    String NUM_POOL_AXB_BATCH_LOCK = "private:pool:axb:batch:lock";

    /**
     * AX 可用X号码池: {vcc_id}:{type}:pool:{area_code}
     */
    String AX_USABLE_POOL_KEY = "%s:%s:pool:%s";

    /**
     * 短信发送数量限制键 key: private:sms:limit:{number}  value：json(NumberSmsLimitDto)
     */
    String XSMS_SHORT_MESSAGE_LIMIT_KEY = "private:sms:limit:%s";

    /**
     * 通用短信黑名单词汇的redis key
     * private:sms:black_words:{vccId}
     */
    String COMMON_BLACK_WORDS_KEY = "private:sms:black_words:%s";

    /**
     * 通用短信白名单词汇的redis key
     * private:sms:black_words:{vccId}
     */
    String COMMON_WHITE_WORDS_KEY = "private:sms:white_words:%s";

    /**
     * 通用短信敏感词汇的redis key
     * private:sms:sensitive_words
     */
    String COMMON_SENSITIVE_WORDS_KEY = "private:sms:sensitive_words";

    /**
     * 当月短信发送数量
     * 转发成功的才会计数
     * private:month:sms:sent:count:{month}:{number}
     */
    String MONTH_SMS_SENT_COUNT_KEY = "private:month:sms:sent:count:%s:%s";


    /**
     * 当日短信发送数量
     * 转发成功的才会计数
     * private:day:sms:sent:count:{day}:{number}
     */
    String DAY_SMS_SENT_COUNT_KEY = "private:day:sms:sent:count:%s:%s";

    /**
     * 106号码初始化标识
     * 1007:pool:106:axb:{area_code}:{A/B}
     */
    String INDUSTRY_SMS_TEL_INIT_KEY = "%s:pool:106:axb:%s:%s";

    /**
     * 106号码可用池
     * mt:pool:106:axb:pool:{area_code}:{A/B}
     */
    String INDUSTRY_SMS_TEL_USABLE_POOL_KEY = "%s:pool:106:axb:pool:%s:%s";

    String LOCK_INIT_INDUSTRY_USABLE_POOL_KEY = "lock_init_ax_key_%s";

    /**
     * 获取第三方 url配置key
     * third:supplier:{supplier_id}:{urlType}
     */
    String THIRD_SUPPLIER_URL_KEY = "third:supplier:%s:%s";

    /**
     * 获取第三方 url配置key
     * third:supplier:exception:count:{supplier_id}
     */
    String THIRD_SUPPLIER_EXCEPTION_KEY = "third:supplier:exception:count:%s";

    /**
     * 供应商信息 string
     * key 供应商id
     * value: json
     */
    String PRIVATE_SUPPLIER_INFO_KEY = "private:supplierInfo:%s";

    /**
     * 获取第三方 bindid 和本平台绑定关系
     * third:bind:{vccid}:axb:{cqt-bindid}:
     */
    String THIRD_CQTBIND_MAPPER = "third:bind:%s:axb:%s";

    /**
     * 获取第三方 bindid 和本平台绑定关系
     * third:bind:{vccid}:axb:{third-bindid}:
     */
    String THIRD_BIND_MAPPER = "third:bind:%s:axb:%s";

    String THIRD_BIND_ID_INFO = "third:bind:axb:%s";

    /**
     * x号码所属vccId string
     * key: msrn_{number}
     * value: vccId
     */
    String X_NUMBER_BELONG_VCC_ID_KEY = "msrn_%s";

    /**
     * 根据漫游号查询gtCode string
     * gt_msrn_{msrn}
     */
    String GT_MSRN = "gt_msrn_%s";

    /**
     * 根据gtCode 查询漫游号set
     * msrn_status0_set_{gtCode}
     */
    String MSRN_STATUS0_SET = "msrn_status0_set_%s";

    /**
     * 中间号监控, 修改sbc配置文件加锁
     * private:updateSbcDisMonitor:{sbc_ip}
     */
    String UPDATE_SBC_DIS_MONITOR_LOCK_KEY = "private:dis:updateSbcDisMonitorLock:%s";

    /**
     * 异常的节点 dis组权重为0 断开连接
     * string
     * private:dis:disconnectNode:{SN_IP}
     */
    String DISCONNECT_NODE_KEY = "private:dis:disconnectNode:%s";


    /**
     * 内部配置实体json
     * corp_interior:{VCCD}:{900007}
     */
    String CORP_INTERIOR_INFO = "private:corp_interior:%s:%s";

    /**
     * 同步地市供应商分配策略到nacos加锁
     */
    String SYNC_DISTRIBUTION_STRATEGY_LOCK_KEY = "SYNC_DISTRIBUTION_STRATEGY_LOCK";


    /**
     * 号码拨测任务锁 - 任务标识
     * {jobId}
     * val: currentTime
     */
    String HMBC_JOB_LOCK_KEY = "private:lock:hmbc:%s";

    /**
     * 号码外呼拨测配置信息
     * {vccId}
     * ttl: 1 days
     */
    String HMBC_OUTBOUND_CALL_CONFIG_KEY = "private:hmbc:config:%s";

    /**
     * 号码外呼拨测配置执行中的任务数量
     * {taskRecordId}
     * ttl: 1 days
     */
    String HMBC_OUTBOUND_CALL_EXECUTING_COUNT_KEY = "private:hmbc:count:executing:%s";

    /**
     * 号码外呼拨测配置执行成功的计数
     * {taskRecordId}
     * ttl: 1 days
     */
    String HMBC_OUTBOUND_CALL_SUCCESS_COUNT_KEY = "private:hmbc:count:success:%s";

    /**
     * 号码外呼拨测配置执行失败的计数
     * {taskRecordId}
     * ttl: 1 days
     */
    String HMBC_OUTBOUND_CALL_FAILED_COUNT_KEY = "private:hmbc:count:failed:%s";

    /**
     * 拨测执行失败的号码
     * {number}
     * ttl: 86400 s
     */
    String HMBC_FAILED_NUMBER_KEY = "private:hmbc:failed:%s";

    /**
     * 号码外呼拨测失败计数
     * {number}
     * ttl: 8h
     * */
    String HMBC_NUMBER_FAILED_COUNT_KEY = "private:hmbc:%s:count:failed";

    /**
     * h码
     */
    String TEL_CODE_OF_AREA_CODE_KEY = "h_%s";

    /**
     * 广电 话单重推次数
     * private:brodnet:retry:{type}:{vcc_id}:{unique_id}
     */
    String BROAD_NET_CALL_BILL_RETRY_COUNT_KEY = "private:broadnet:retry:%s:%s:%s";

    /**
     * 广电呼叫id和绑定id对应关系
     * "private:brodnet:callIdWithBindId:{callId}
     */
    String BROAD_NET_CALL_ID_WITH_BIND_ID_KEY = "private:broadnet:callIdWithBindId:%s";

    /**
     * 微信鉴权token key
     */
    String WECHAT_TOKEN = "access_token";

    /**
     * 企业并发控制
     * bus:call:limit:[vccid]:info
     */
    String BUS_CALL_LIMIT_INFO = "bus:call:limit:%s:info";

    /**
     * private:numberInfo:{号码}
     * val：平台号码实体
     */
    String PRIVATE_NUMBER_INFO = "private:numberInfo:%s";

    /**
     * thirdRecordUrl:{callID}
     * val：获取录音地址
     */
    String THIRD_RECORD_URL = "thirdRecordUrl:%s";

    /**
     * recordUrl:{号码}
     * val：获取录音地址
     */
    String RECORD_URL = "recordUrl:%s";

    /**
     * private:numberInfo:{num}
     * val：平台号码信息
     */
    String NUM_INFO = "private:numberInfo:%s";

    /**
     * key: private:blacklist:caller:{vcc_id}:{business_type}:{caller_number}:{callee_number}
     * value：1
     */
    String CALLER_BLACKLIST_KEY = "private:blacklist:caller:{}:{}:{}:{}";

}
