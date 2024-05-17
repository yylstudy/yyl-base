package com.cqt.cdr.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.cqt.cdr.conf.DynamicConfig;
import com.cqt.cdr.entity.DictItem;
import com.cqt.cdr.service.PushErrService;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.feign.client.CdrClient;
import com.cqt.feign.freeswitch.FreeswitchApiFeignClient;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.cdr.dto.CdrMessageDTO;
import com.cqt.model.cdr.dto.RemoteCdrDTO;
import com.cqt.model.cdr.dto.RemoteQualityCdrDTO;
import com.cqt.model.cdr.entity.*;
import com.cqt.model.cdr.vo.RemoteCdrVO;
import com.cqt.model.freeswitch.dto.api.PlayRecordDTO;
import com.cqt.model.freeswitch.vo.PlayRecordVO;
import com.cqt.model.number.entity.NumberInfo;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.cqt.cdr.util.CommonUtils.date2Str;

@Component
@Slf4j
public class AccessRemote {
    @Resource
    private FreeswitchApiFeignClient freeswitchApiFeignClient;
    @Resource
    private CdrClient cdrClient;
    @Resource
    private DynamicConfig dynamicConfig;
    @Resource
    private RedissonUtil redissonUtil;
    @Resource
    private PushErrService pushErrService;
    @Resource
    CommonDataOperateService commonDataOperateService;

    /**
     * 获取存储路径
     *
     * @param serviceId
     * @param subCdr
     * @param LOG_TAG
     * @return
     */
    public boolean getStoragePath(String serviceId, CallCenterSubCdr subCdr, String LOG_TAG) {
        String recordUrl = subCdr.getRecordUrl();
        String specificPath = "/" + serviceId + recordUrl.substring("/home/lcc_media".length());
        PlayRecordDTO playRecordDTO = new PlayRecordDTO(subCdr.getCompanyCode(), specificPath);
        PlayRecordVO playRecord = null;
        for (int i = 0; i < dynamicConfig.getPushnum(); i++) {
            playRecord = getPlayRecordVO(playRecordDTO, LOG_TAG);
            if (playRecord != null) {
                break;
            }
            ThreadUtils.sleep(dynamicConfig.getSleepTime());
        }
        if (playRecord.getResult()) {
            subCdr.setWsRtsp(playRecord.getWsRtsp());
            subCdr.setRecordUrlRtsp(playRecord.getRecordUrlRtsp());
            subCdr.setAbsoluteUrl(playRecord.getRecordUrl());
            subCdr.setRecordUrlIn(playRecord.getRecordUrlIn());
        } else {
            return false;
        }
        return true;
    }

    /**
     * 访问远程接口获取录音或视频路径
     *
     * @param playRecordDTO
     * @param LOG_TAG
     * @return
     */
    public PlayRecordVO getPlayRecordVO(PlayRecordDTO playRecordDTO, String LOG_TAG) {
        PlayRecordVO playRecord = null;
        try {
            log.info(LOG_TAG + "访问底层接口接收参数：{}", playRecordDTO);
            playRecord = freeswitchApiFeignClient.getPlayRecord(playRecordDTO);
            log.info(LOG_TAG + "访问底层接口返回结果：{}", playRecord);
        } catch (Exception e) {
            log.error(LOG_TAG + "访问底层接口异常：", e);
        }
        return playRecord;
    }

    /**
     * 访问第三方接口
     *
     * @param dictItem
     * @param remoteCdrDTO
     * @param LOG_TAG
     * @return
     */
    public RemoteCdrVO sendCdr(DictItem dictItem, RemoteCdrDTO remoteCdrDTO, String LOG_TAG) {
        RemoteCdrVO remoteCdrVO = null;
        try {
            log.info(LOG_TAG + "推送cdr，请求路径：{}，请求参数：{}", dictItem.getItemValue(), remoteCdrDTO);
            remoteCdrVO = cdrClient.sendCdr(new URI(dictItem.getItemValue()), remoteCdrDTO);
        } catch (URISyntaxException e) {
            log.error(LOG_TAG + "字典配置的url[{}]有误", dictItem.getItemValue(), e);
        } catch (Exception e) {
            log.error(LOG_TAG + "请求第三方接口异常", e);
        }
        log.info(LOG_TAG + "推送cdr，返回结果{}", remoteCdrVO);
        return remoteCdrVO;
    }

    /**
     * 访问第三方接口
     *
     * @param remoteCdrDTO
     * @param LOG_TAG
     * @return
     */
    public RemoteCdrVO sendCdr(String url, RemoteCdrDTO remoteCdrDTO, String LOG_TAG) {
        RemoteCdrVO remoteCdrVO = null;
        try {
            log.info(LOG_TAG + "推送cdr，请求路径：{}，请求参数：{}", url, remoteCdrDTO);
            remoteCdrVO = cdrClient.sendCdr(new URI(url), remoteCdrDTO);
        } catch (URISyntaxException e) {
            log.error(LOG_TAG + "字典配置的url[{}]有误", url, e);
        } catch (Exception e) {
            log.error(LOG_TAG + "请求第三方接口异常", e);
        }
        log.info(LOG_TAG + "推送cdr，返回结果{}", remoteCdrVO);
        return remoteCdrVO;
    }

    /**
     * 访问第三方接口，访问失败参试重试
     *
     * @param dictItem
     * @param remoteCdrDTO
     * @param LOG_TAG
     * @return
     */
    public RemoteCdrVO resendCdr(DictItem dictItem, RemoteCdrDTO remoteCdrDTO, String LOG_TAG) {
        RemoteCdrVO remoteCdrVO = null;
        for (int i = 1; i <= dynamicConfig.getPushnum(); i++) {
            log.info(LOG_TAG + "开始第{}次重试", i);
            remoteCdrVO = sendCdr(dictItem, remoteCdrDTO, LOG_TAG);
            if (remoteCdrVO != null && remoteCdrVO.getCode() == "200") {
                break;
            }
        }
        return remoteCdrVO;
    }

    /**
     * 获取第三方接口访问对象
     *
     * @param rees
     * @return
     */
    public RemoteCdrDTO getRemoteCdrDTO(List<CdrDatapushPushEntity> rees) {
        String token = DigestUtils.md5Hex(dynamicConfig.getSfkeysfdiscern() + StringUtil.getRegDate("yyyyMMddHHmmss"));
        RemoteCdrDTO remoteCdrDTO = new RemoteCdrDTO();
        remoteCdrDTO.setData(rees);
        remoteCdrDTO.setToken(token);
        remoteCdrDTO.setRequestId(UUID.randomUUID().toString());
        remoteCdrDTO.setSynTime(new Date());
        return remoteCdrDTO;
    }

    /**
     * 将质检话单推送给第三方
     *
     * @param dictItem
     * @param remoteQualityCdrDTO
     * @param LOG_TAG
     * @return
     */
    public RemoteCdrVO sendQualityCdr(DictItem dictItem, RemoteQualityCdrDTO remoteQualityCdrDTO, String LOG_TAG) {
        RemoteCdrVO remoteCdrVO = null;
        try {
            log.info(LOG_TAG + "推送质检cdr，请求路径：{}，请求参数：{}", dictItem.getItemValue(), remoteQualityCdrDTO);
            remoteCdrVO = cdrClient.sendQualityCdr(new URI(dictItem.getItemValue()), remoteQualityCdrDTO);
        } catch (URISyntaxException e) {
            log.error(LOG_TAG + "字典配置的url[{}]有误", dictItem.getItemValue(), e);
        } catch (Exception e) {
            log.error(LOG_TAG + "请求第三方接口异常", e);
        }
        log.info(LOG_TAG + "推送cdr，返回结果{}", remoteCdrVO);
        return remoteCdrVO;
    }

    /**
     * 将质检话单推送给第三方
     *
     * @param remoteQualityCdrDTO
     * @param LOG_TAG
     * @return
     */
    public <T> RemoteCdrVO sendQualityCdr(String url, T remoteQualityCdrDTO, String LOG_TAG) {
        RemoteCdrVO remoteCdrVO = null;
        try {
            log.info(LOG_TAG + "推送质检cdr，请求路径：{}，请求参数：{}", url, remoteQualityCdrDTO);
            remoteCdrVO = cdrClient.sendQualityCdr(new URI(url), remoteQualityCdrDTO);
        } catch (URISyntaxException e) {
            log.error(LOG_TAG + "字典配置的url[{}]有误", url, e);
        } catch (Exception e) {
            log.error(LOG_TAG + "请求第三方接口异常", e);
        }
        log.info(LOG_TAG + "推送cdr，返回结果{}", remoteCdrVO);
        return remoteCdrVO;
    }

    /**
     * 访问第三方接口，访问失败参试重试
     *
     * @param dictItem
     * @param remoteQualityCdrDTO
     * @param LOG_TAG
     * @return
     */
    public RemoteCdrVO resendQualityCdr(DictItem dictItem, RemoteQualityCdrDTO remoteQualityCdrDTO, String LOG_TAG) {
        RemoteCdrVO remoteCdrVO = null;
        for (int i = 1; i <= dynamicConfig.getPushnum(); i++) {
            log.info(LOG_TAG + "开始第{}次重试", i);
            remoteCdrVO = sendQualityCdr(dictItem, remoteQualityCdrDTO, LOG_TAG);
            if (remoteCdrVO != null && remoteCdrVO.getCode() == "200") {
                break;
            }
        }
        return remoteCdrVO;
    }

    /**
     * 获取推送全量话单对象
     *
     * @param mainCdr
     * @param cdrChanneldata
     * @param callCenterSubCdr
     * @param LOG_TAG
     * @return
     */
    public CdrDatapushPushEntity getCdrDatapushPushEntity(CallCenterMainCdr mainCdr, CdrChanneldata cdrChanneldata, CallCenterSubCdr callCenterSubCdr, String LOG_TAG) {
        CdrDatapushPushEntity cdrDatapushPushEntity = null;
        NumberInfo numberInfo = null;
        Optional<NumberInfo> numberInfoOptional = commonDataOperateService.getNumberInfo(callCenterSubCdr.getDisplayNumber());
        if (numberInfoOptional.isPresent()) {
            numberInfo = numberInfoOptional.get();
        }
        try {
            cdrDatapushPushEntity = new CdrDatapushPushEntity();

            // carType=1 呼入 cdrType=0 呼出
            Integer cdrType = callCenterSubCdr.getCdrType();
            // 呼入
            if (cdrType == 1) {
                // 原始主叫(分机) 呼入：客户号码
                cdrDatapushPushEntity.setFirst_real_caller(CommonUtils.replaceNumPerfix(callCenterSubCdr.getDisplayNumber()));

                // 原始被叫(分机) 呼入：分机
                cdrDatapushPushEntity.setFirst_real_callee(callCenterSubCdr.getExtId());

                // 原始主叫 呼入：客户号码
                cdrDatapushPushEntity.setReal_caller(CommonUtils.replaceNumPerfix(callCenterSubCdr.getDisplayNumber()));

                // 原始被叫 呼入：坐席
                cdrDatapushPushEntity.setReal_callee(callCenterSubCdr.getAgentId());

                // 主叫号码 呼入：客户号码
                cdrDatapushPushEntity.setCaller_id_number(CommonUtils.replaceNumPerfix(callCenterSubCdr.getDisplayNumber()));

                // 被叫号码，即用户拨叫的号码 呼入：客户呼入的平台号码（有配置特服号码的使用特服号码）
                // 平台号码 呼入：客户呼入的平台号码（有配置特服号码的使用特服号码）
                if (numberInfo != null && com.alibaba.nacos.common.utils.StringUtils.isNotEmpty(numberInfo.getDisplayNumber())) {
                    cdrDatapushPushEntity.setCallee_id_number(CommonUtils.replaceNumPerfix(numberInfo.getDisplayNumber()));
                    cdrDatapushPushEntity.setCr_destination(CommonUtils.replaceNumPerfix(numberInfo.getDisplayNumber()));
                    cdrDatapushPushEntity.setAni(CommonUtils.replaceNumPerfix(numberInfo.getDisplayNumber()));
                } else {
                    cdrDatapushPushEntity.setCallee_id_number(CommonUtils.replaceNumPerfix(callCenterSubCdr.getCallerNumber()));
                    cdrDatapushPushEntity.setCr_destination(CommonUtils.replaceNumPerfix(callCenterSubCdr.getCallerNumber()));
                    cdrDatapushPushEntity.setAni(CommonUtils.replaceNumPerfix(callCenterSubCdr.getCallerNumber()));
                }

                // type=call_out 呼出type=call_in  呼type=TRANSFER  转接type=MEETING   会议
                cdrDatapushPushEntity.setType("call_in");

                // 平台号
                cdrDatapushPushEntity.setPlatform_number(CommonUtils.replaceNumPerfix(callCenterSubCdr.getChargeNumber()));

                // 用户号码
                cdrDatapushPushEntity.setUsrphone(CommonUtils.replaceNumPerfix(callCenterSubCdr.getDisplayNumber()));

                // 录音开始时间 呼入b应答 呼出a应答
                cdrDatapushPushEntity.setRecord_start(date2Str(callCenterSubCdr.getBAnswerTime()));

                // 呼叫方向 inbound=主叫方 outbound=被叫方--需要提供enum
                cdrDatapushPushEntity.setDirection("inbound");

                //  区号 客户侧
                cdrDatapushPushEntity.setArea_code(LocalOrLongUtils.getNumberCode(callCenterSubCdr.getDisplayNumber(), redissonUtil));
            }

            // 呼出
            if (cdrType == 0) {
                // 原始主叫(分机) 呼出：分机
                cdrDatapushPushEntity.setFirst_real_caller(callCenterSubCdr.getExtId());

                // 原始被叫(分机) 呼出：客户号码
                cdrDatapushPushEntity.setFirst_real_callee(CommonUtils.replaceNumPerfix(callCenterSubCdr.getCalleeNumber()));

                // 原始主叫(分机) 呼出：坐席
                cdrDatapushPushEntity.setReal_caller(callCenterSubCdr.getAgentId());

                // 原始被叫 呼出：客户号码
                cdrDatapushPushEntity.setReal_callee(CommonUtils.replaceNumPerfix(callCenterSubCdr.getCalleeNumber()));

                // 主叫号码 呼出：呼出的平台号码（有配置特服号码的使用特服号码）
                // 平台号码 呼出的平台号码（有配置特服号码的使用特服号码）
                if (numberInfo != null && com.alibaba.nacos.common.utils.StringUtils.isNotEmpty(numberInfo.getDisplayNumber())) {
                    cdrDatapushPushEntity.setCaller_id_number(CommonUtils.replaceNumPerfix(numberInfo.getDisplayNumber()));
                    cdrDatapushPushEntity.setCr_destination(CommonUtils.replaceNumPerfix(numberInfo.getDisplayNumber()));
                    cdrDatapushPushEntity.setAni(CommonUtils.replaceNumPerfix(numberInfo.getDisplayNumber()));
                } else {
                    cdrDatapushPushEntity.setCaller_id_number(CommonUtils.replaceNumPerfix(callCenterSubCdr.getDisplayNumber()));
                    cdrDatapushPushEntity.setCr_destination(CommonUtils.replaceNumPerfix(callCenterSubCdr.getDisplayNumber()));
                    cdrDatapushPushEntity.setAni(CommonUtils.replaceNumPerfix(callCenterSubCdr.getDisplayNumber()));
                }

                // 被叫号码，即用户拨叫的号码 呼出：客户号码
                cdrDatapushPushEntity.setCallee_id_number(CommonUtils.replaceNumPerfix(callCenterSubCdr.getCalleeNumber()));

                // type=call_out 呼出type=call_in  呼type=TRANSFER  转接type=MEETING   会议
                cdrDatapushPushEntity.setType("call_out");

                // 平台号
                cdrDatapushPushEntity.setPlatform_number(CommonUtils.replaceNumPerfix(callCenterSubCdr.getChargeNumber()));

                // 用户号码
                cdrDatapushPushEntity.setUsrphone(CommonUtils.replaceNumPerfix(callCenterSubCdr.getCalleeNumber()));

                // 录音开始时间 呼入b应答 呼出a应答
                cdrDatapushPushEntity.setRecord_start(date2Str(callCenterSubCdr.getAAnswerTime()));

                //  呼叫方向 inbound=主叫方 outbound=被叫方--需要提供enum
                cdrDatapushPushEntity.setDirection("outbound");

                //  区号 客户侧
                cdrDatapushPushEntity.setArea_code(LocalOrLongUtils.getNumberCode(callCenterSubCdr.getCalleeNumber(), redissonUtil));
            }

            if (cdrType == 5) {
                // type=call_out 呼出type=call_in  呼type=TRANSFER  转接type=MEETING   会议
                cdrDatapushPushEntity.setType("TRANSFER");
            }

            if (cdrType == 7) {
                // type=call_out 呼出type=call_in  呼type=TRANSFER  转接type=MEETING   会议
                cdrDatapushPushEntity.setType("MEETING");
            }

            // 呼叫开始时间，格式：yyyymmddhhmmss
            cdrDatapushPushEntity.setStart_stamp(date2Str(callCenterSubCdr.getACallStartTime()));
            // 应答时间
            cdrDatapushPushEntity.setAnswer_stamp(date2Str(callCenterSubCdr.getAAnswerTime()));
            // 结束时间
            cdrDatapushPushEntity.setEnd_stamp(date2Str(callCenterSubCdr.getBCallEndTime()));

            // 通话时长 秒 ivr + 通话
            // 计费时长 秒 ivr + 通话
            // ivr时长，单位：秒
            Long ivrDuration = null;
            if (callCenterSubCdr.getStartIvrTime() != null && callCenterSubCdr.getEndIvrTime() != null) {
                ivrDuration = (callCenterSubCdr.getEndIvrTime().getTime() - callCenterSubCdr.getStartIvrTime().getTime()) / 1000;
            }
            Long l = (ivrDuration == null ? 0l : ivrDuration) + (callCenterSubCdr.getBDuration() == null ? 0 : callCenterSubCdr.getBDuration()) / 1000;
            cdrDatapushPushEntity.setLinktimes(l.intValue());
            cdrDatapushPushEntity.setBillsec(l.intValue());
            cdrDatapushPushEntity.setIvrtime(ivrDuration == null ? null : ivrDuration.intValue());
            // 总时长
            cdrDatapushPushEntity.setDuration(l.intValue());

            // 振铃时长，单位：秒 b路 应答时间-振铃时间
            // 回铃时长 是振铃时间
            if (callCenterSubCdr.getBRingStamp() == null) {
                cdrDatapushPushEntity.setRingtimes(null);
                cdrDatapushPushEntity.setRingbacktimes(null);
            } else {
                Long ringTimes = ((callCenterSubCdr.getBAnswerStamp() == null ? callCenterSubCdr.getBHangupStamp() : callCenterSubCdr.getBAnswerStamp()) - callCenterSubCdr.getBRingStamp()) / 1000;
                cdrDatapushPushEntity.setRingtimes(ringTimes.intValue());
                cdrDatapushPushEntity.setRingbacktimes(ringTimes.intValue());
            }

            // 话单桥接
            cdrDatapushPushEntity.setFirst_bridge_uuid("");
            // 通话唯一标识
            cdrDatapushPushEntity.setUuid(callCenterSubCdr.getUuid());
            // 录音路径
            cdrDatapushPushEntity.setRecord_filepath(callCenterSubCdr.getAbsoluteUrl());
            // 留言路径  暂无
            cdrDatapushPushEntity.setLeavemsg_filepath(null);
            // 留言开始时间
            cdrDatapushPushEntity.setLeavemsg_start("");
            // 空号识别的挂断原因
            cdrDatapushPushEntity.setCallout_hungup_cause(callCenterSubCdr.getHangupCause());
            // 挂断结果  release_desc
            cdrDatapushPushEntity.setHangup_cause(callCenterSubCdr.getReleaseDesc());
            // 存储通道变量
            cdrDatapushPushEntity.setIvrid_e("");
            // 存储通道变量 （当前存储原始的坐席）
            cdrDatapushPushEntity.setIvrid_s("");
            // 存储通道变量（当前为月结号 ）
            cdrDatapushPushEntity.setSatisfaction_ivrid("");
            // 企业标识
            cdrDatapushPushEntity.setCompany_code(callCenterSubCdr.getCompanyCode());
            // A-leg 类型  1-外线呼入 2-内线 呼出
            cdrDatapushPushEntity.setA_calltype(-1);
            // B-leg 类型  1-外线直接呼入 2-队列呼入 3-内线呼入 4-转接呼入 8-多方通话
            cdrDatapushPushEntity.setB_calltype(-1);
            // 这条话单原始的a_calltype(未切割处理前)
            cdrDatapushPushEntity.setFirst_a_calltype(-1);
            // 这条话单原始的b_calltype(未切割处理前)
            cdrDatapushPushEntity.setFirst_b_calltype(-1);
            // 坐席号
            cdrDatapushPushEntity.setCc_sys_agent_id(callCenterSubCdr.getAgentId());
            // 父uuid(多话单关联) call_id
            cdrDatapushPushEntity.setF_uuid(callCenterSubCdr.getCallId());
            if (cdrChanneldata != null) {
                // 满意度
                cdrDatapushPushEntity.setSatisfaction_f1(cdrChanneldata.getSatisfactionF1());
                cdrDatapushPushEntity.setSatisfaction_f2(cdrChanneldata.getSatisfactionF2());
                cdrDatapushPushEntity.setSatisfaction_f3(cdrChanneldata.getSatisfactionF3());
                cdrDatapushPushEntity.setSatisfaction_f4(cdrChanneldata.getSatisfactionF4());
                cdrDatapushPushEntity.setSatisfaction_f5(cdrChanneldata.getSatisfactionF5());
                cdrDatapushPushEntity.setSatisfaction_f6(cdrChanneldata.getSatisfactionF6());
                cdrDatapushPushEntity.setSatisfaction_f7(cdrChanneldata.getSatisfactionF7());
                cdrDatapushPushEntity.setSatisfaction_f8(cdrChanneldata.getSatisfactionF8());
                cdrDatapushPushEntity.setSatisfaction_f9(cdrChanneldata.getSatisfactionF9());
                cdrDatapushPushEntity.setSatisfaction_f10(cdrChanneldata.getSatisfactionF10());
                // 用户按键
                cdrDatapushPushEntity.setUserkey_f1(cdrChanneldata.getUserkeyF1());
                cdrDatapushPushEntity.setUserkey_f2(cdrChanneldata.getUserkeyF2());
                cdrDatapushPushEntity.setUserkey_f3(cdrChanneldata.getUserkeyF3());
                cdrDatapushPushEntity.setUserkey_f4(cdrChanneldata.getUserkeyF4());
                cdrDatapushPushEntity.setUserkey_f5(cdrChanneldata.getUserkeyF5());
                cdrDatapushPushEntity.setUserkey_f6(cdrChanneldata.getUserkeyF6());
                cdrDatapushPushEntity.setUserkey_f7(cdrChanneldata.getUserkeyF7());
                cdrDatapushPushEntity.setUserkey_f8(cdrChanneldata.getUserkeyF8());
                cdrDatapushPushEntity.setUserkey_f9(cdrChanneldata.getUserkeyF9());
                cdrDatapushPushEntity.setUserkey_f10(cdrChanneldata.getUserkeyF10());
                // 服务器ip
                cdrDatapushPushEntity.setLocal_ip_v4(cdrChanneldata.getFreeSwitchName());
            }
            // 放空
            cdrDatapushPushEntity.setCaller_id_name("");

            cdrDatapushPushEntity.setFirst_bridge_on(0);
            // 接通时间点
            cdrDatapushPushEntity.setBridge_stamp(callCenterSubCdr.getBAnswerStamp() == null ? null : callCenterSubCdr.getBAnswerStamp().toString());

            cdrDatapushPushEntity.setContext("");
            // 桥接端通道呼叫标识
            cdrDatapushPushEntity.setBleg_uuid("");

            cdrDatapushPushEntity.setRead_codec("");

            cdrDatapushPushEntity.setWrite_codec("");

            //  挂机方 release_dir
            cdrDatapushPushEntity.setSip_hangup_disposition(callCenterSubCdr.getReleaseDir() == null ? null : callCenterSubCdr.getReleaseDir().toString());

            cdrDatapushPushEntity.setChannel_register_user("");

            // 技能标识
            cdrDatapushPushEntity.setCc_queue(callCenterSubCdr.getSkillId());

            // 坐席名称
            AgentInfo agentInfo = commonDataOperateService.getAgentInfo(callCenterSubCdr.getCompanyCode(), callCenterSubCdr.getAgentId());
            cdrDatapushPushEntity.setCc_agent_name(agentInfo.getAgentName());

            // 分机标识
            cdrDatapushPushEntity.setCc_agent(callCenterSubCdr.getExtId());

            cdrDatapushPushEntity.setCc_side("");

            cdrDatapushPushEntity.setCc_queue_answered_epoch(0l);

            // 队列时长
            cdrDatapushPushEntity.setCc_queue_times(callCenterSubCdr.getQueueDuration() == null ? null : callCenterSubCdr.getQueueDuration().intValue());

            // 出队列时间
            cdrDatapushPushEntity.setCc_queue_terminated_epoch(callCenterSubCdr.getEndQueueTime() == null ? null : callCenterSubCdr.getEndQueueTime().getTime());

            // 进入队列时间
            long time = callCenterSubCdr.getStartQueueTime() == null ? 0L : callCenterSubCdr.getStartQueueTime().getTime();
            cdrDatapushPushEntity.setCc_queue_joined_epoch(time);

            cdrDatapushPushEntity.setCc_queue_canceled_epoch(0l);

            // 队列总回铃（等待）时长 进入队列-应答（B路）暂时
            long bAnswerTime = callCenterSubCdr.getBAnswerTime() == null ? 0L : callCenterSubCdr.getBAnswerTime().getTime();
            Long t;
            if (time == 0l) {
                Long bCallinStamp = callCenterSubCdr.getBCallinStamp() == null ? 0l : callCenterSubCdr.getBCallinStamp();
                t = (time - bCallinStamp) / 1000;
            } else {
                t = (time - bAnswerTime) / 1000;
            }
            cdrDatapushPushEntity.setCc_ringbacktimes(t.intValue());

            cdrDatapushPushEntity.setObpjnum("");

            cdrDatapushPushEntity.setPjjobid(0);

            cdrDatapushPushEntity.setObc_prjcode("");

            // servename 不用填
            cdrDatapushPushEntity.setServer_name(null);

            cdrDatapushPushEntity.setIfqa(0);

            cdrDatapushPushEntity.setCallin_process_mode("");

            cdrDatapushPushEntity.setCaseid("");

            cdrDatapushPushEntity.setCusid("");

            cdrDatapushPushEntity.setIfplayedleavemsg("");

            cdrDatapushPushEntity.setRelative_uuid("");
        } catch (Exception e) {
            log.error(LOG_TAG + "转换外部话单异常", e);
        }
        return cdrDatapushPushEntity;
    }

    /**
     * 获取质检推送对象
     *
     * @param cdrMessageDTO
     * @param companyCode
     * @return
     */
    public RemoteQualityCdrDTO getRemoteQualityCdrDTO(CdrMessageDTO cdrMessageDTO, String companyCode) {
        List<CallCenterSubCdr> subCdrs = cdrMessageDTO.getSubCdr();
        List<RemoteQualityCdr> list = new ArrayList<>();
        for (CallCenterSubCdr subCdr : subCdrs) {
            RemoteQualityCdr remoteQualityCdr = new RemoteQualityCdr();
            remoteQualityCdr.setCallId(subCdr.getCallId());
            // 呼入方向
            remoteQualityCdr.setDirection(subCdr.getDirection() == 0 ? 2 : 1);
            // 呼叫类型 1.IVR，3：坐席（坐席为空的为ivr）
            remoteQualityCdr.setCalltype(StringUtils.isEmpty(subCdr.getAgentId()) ? "1" : "3");
            // 坐席号码
            remoteQualityCdr.setCno(subCdr.getAgentId());
            // 原始主叫号码 计费oricallingnumber
            remoteQualityCdr.setOrigcalling(subCdr.getCallerNumber());
            // 原始被叫号码 计费oricallednumber
            remoteQualityCdr.setOrigcalled(subCdr.getCalleeNumber());
            // 转移号码
            remoteQualityCdr.setDestnumber("");
            // 转移标识
            remoteQualityCdr.setDestsid("");
            // 振铃时长
            // 振铃时间 b路 应答时间-振铃时间
            if (subCdr.getBRingStamp() == null) {
                remoteQualityCdr.setRingSeconds(null);
            } else {
                long l = (subCdr.getBAnswerStamp() == null ? subCdr.getBHangupStamp() : subCdr.getBAnswerStamp()) - subCdr.getBRingStamp();
                remoteQualityCdr.setRingSeconds(new BigDecimal(l).divide(new BigDecimal(1000)).toString());
            }
            // 主叫号码 计费callingpartynumber
            remoteQualityCdr.setCallingnumber(subCdr.getCallerNumber());
            // 被叫号码 计费calledpartynumber
            remoteQualityCdr.setCallednumber(subCdr.getCalleeNumber());
            // 通话时长
            // 通话时长 秒 ivr + 通话
            Long ivrDuration = null;
            if (subCdr.getStartIvrTime() != null && subCdr.getEndIvrTime() != null) {
                ivrDuration = (subCdr.getEndIvrTime().getTime() - subCdr.getStartIvrTime().getTime()) / 1000;
            }
            Long l = (ivrDuration == null ? 0l : ivrDuration) + (subCdr.getBDuration() == null ? 0 : subCdr.getBDuration()) / 1000;
            remoteQualityCdr.setBridgeDuration(l);
            // 呼叫结果 通话时长判断 （有通话为1否则为2）
            remoteQualityCdr.setStatus(ivrDuration == null ? "2" : "1");
            // 挂机方（1：主叫2：被叫）
            remoteQualityCdr.setDisconnection(subCdr.getReleaseDir());
            // 挂机原因 呼叫结果明细（呼入-IVR溢出、呼入-IVR应答。。。
            remoteQualityCdr.setDetail(subCdr.getReleaseDesc());
            // 排队时长
            remoteQualityCdr.setQueuesec(subCdr.getQueueDuration());
            // 服务（任务）标识
            remoteQualityCdr.setTaskid("");
            // 录音文件
            remoteQualityCdr.setRecordFile(subCdr.getAbsoluteUrl());
            // 后处理时间
            remoteQualityCdr.setPostprocess("");
            // 透明参数
            remoteQualityCdr.setTransparam("");
            // 所在的媒体服务器名称
            remoteQualityCdr.setMsserver(cdrMessageDTO.getServiceId());
            // 技能
            remoteQualityCdr.setUserId(subCdr.getSkillId());
            // 随路数据
            remoteQualityCdr.setCalldata(cdrMessageDTO.getCdrChanneldata().getClientUuid() != null ? redissonUtil.get("cc_ivr_track_data_" + cdrMessageDTO.getCdrChanneldata().getClientUuid()) + "" : null);
            // 单号/流水号
            remoteQualityCdr.setOrderid("");
            remoteQualityCdr.setCalledid("");
            // 部门（班组号）
            String departments = null;
            if (subCdr.getAgentId() != null) {
                Map<String, String> map = redissonUtil.getStringMap(companyCode + ":agent_department_relationship");
                departments = map.get(subCdr.getAgentId());
            }
            remoteQualityCdr.setDepartment(departments);
            // 企业标识
            remoteQualityCdr.setVccid(subCdr.getCompanyCode());
            // 客户手机前三位 手机前三位
            String telephone = null;
            // 呼入
            if (subCdr.getCdrType() == 1) {
                // 呼叫开始时间 计费开始时间
                remoteQualityCdr.setStartTime(date2Str(subCdr.getACallStartTime()));
                // 呼叫应答时间 计费应答时间
                remoteQualityCdr.setAnswerTime(date2Str(subCdr.getAAnswerTime()));
                // 呼叫结束时间 计费stoptime
                remoteQualityCdr.setEndTime(date2Str(subCdr.getACallEndTime()));
                telephone = subCdr.getDisplayNumber();
                // 服务类型
                remoteQualityCdr.setService("0");
                // 区号
                remoteQualityCdr.setAreacode(LocalOrLongUtils.getNumberCode(subCdr.getDisplayNumber(), redissonUtil));
                // S+计费beginnumber+【uuid】22位
                String sid = "S" + date2Str(subCdr.getACallStartTime()) + UUID.randomUUID().toString().replace("-", "").substring(0, 22);
                remoteQualityCdr.setSid(sid);
            }
            // 呼出
            if (subCdr.getCdrType() == 0) {
                // 呼叫开始时间 计费开始时间
                remoteQualityCdr.setStartTime(date2Str(subCdr.getBCallStartTime()));
                // 呼叫应答时间 计费应答时间
                remoteQualityCdr.setAnswerTime(date2Str(subCdr.getBAnswerTime()));
                // 呼叫结束时间 计费stoptime
                remoteQualityCdr.setEndTime(date2Str(subCdr.getBCallEndTime()));
                telephone = subCdr.getCalleeNumber();
                // 服务类型
                remoteQualityCdr.setService("3");
                // 区号
                remoteQualityCdr.setAreacode(LocalOrLongUtils.getNumberCode(subCdr.getCalleeNumber(), redissonUtil));
                // S+计费beginnumber+【uuid】22位
                String sid = "S" + date2Str(subCdr.getBCallStartTime()) + UUID.randomUUID().toString().replace("-", "").substring(0, 22);
                remoteQualityCdr.setSid(sid);
            }
            remoteQualityCdr.setTelthree(telephone == null ? null : telephone.substring(0, 3));
            remoteQualityCdr.setTelseven(telephone == null ? null : telephone.substring(0, 7));
            remoteQualityCdr.setAgentNumber(subCdr.getAgentId());
            remoteQualityCdr.setCustomerNumber(telephone);
            list.add(remoteQualityCdr);
        }
        RemoteQualityCdrDTO remoteQualityCdrDTO = new RemoteQualityCdrDTO();
        remoteQualityCdrDTO.setExtInfos(list);
        remoteQualityCdrDTO.setVccid(companyCode);
        remoteQualityCdrDTO.setType("1");
        return remoteQualityCdrDTO;
    }

    public boolean getRemoteUrlIsSuccess(String LOG_TAG, CdrMessageDTO cdrMessageDTO) {
        List<CallCenterSubCdr> subCdrs = cdrMessageDTO.getSubCdr();
        for (CallCenterSubCdr subCdr : subCdrs) {
            if (!getStoragePath(cdrMessageDTO.getServiceId(), subCdr, LOG_TAG)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 向字典配置的企业推送话单
     *
     * @param LOG_TAG
     * @param companyCode
     * @param month
     * @param rees
     * @param dictItem
     * @return
     */
    public void handleCDR(String LOG_TAG, String companyCode, String month, ArrayList<CdrDatapushPushEntity> rees, DictItem dictItem) {
        RemoteCdrVO remoteCdrVO;
        boolean flag = true;
        RemoteCdrDTO remoteCdrDTO = getRemoteCdrDTO(rees);
        remoteCdrVO = sendCdr(dictItem, remoteCdrDTO, LOG_TAG);
        if (remoteCdrVO == null || !remoteCdrVO.getCode().equals("200")) {
            remoteCdrVO = resendCdr(dictItem, remoteCdrDTO, LOG_TAG);
            if (remoteCdrVO != null && remoteCdrVO.getCode().equals("200")) {
                flag = false;
            }
        } else {
            flag = false;
        }
        if (flag) {
            String json = JSONObject.toJSONString(rees);
            log.info(LOG_TAG + "消息推送失败，推送信息：{}，存入数据库", json);
            pushErrService.errIntoErrTable(json, dictItem.getItemValue(), "1", month, companyCode, LOG_TAG);
        }
    }
}
