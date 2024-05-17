package com.cqt.cdr.cloudccsfaftersales.util;

import com.alibaba.nacos.common.utils.ThreadUtils;
import com.cqt.base.util.CacheUtil;
import com.cqt.cdr.cloudccsfaftersales.conf.DynamicConfig;
import com.cqt.cdr.cloudccsfaftersales.entity.CallStateDetails;
import com.cqt.cdr.cloudccsfaftersales.entity.DictItem;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.feign.client.CdrClient;
import com.cqt.feign.freeswitch.FreeswitchApiFeignClient;
import com.cqt.model.cdr.dto.CdrMessageDTO;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import com.cqt.model.cdr.entity.PushMisscallDataEntity;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.freeswitch.dto.api.PlayRecordDTO;
import com.cqt.model.cdr.dto.RemoteCdrDTO;
import com.cqt.model.cdr.dto.RemoteQualityCdrDTO;
import com.cqt.model.cdr.entity.CdrDatapushPushEntity;
import com.cqt.model.cdr.entity.RemoteQualityCdr;
import com.cqt.model.freeswitch.vo.PlayRecordVO;
import com.cqt.model.cdr.vo.RemoteCdrVO;
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

import static com.cqt.cdr.cloudccsfaftersales.util.CommonUtils.date2Str;

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
    private CommonDataOperateService commonDataOperateService;

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
     * @param LOG_TAG
     * @return
     */
    public <T> RemoteCdrVO sendQualityCdr(DictItem dictItem, T t, String LOG_TAG) {
        RemoteCdrVO remoteCdrVO = null;
        try {
            log.info(LOG_TAG + "推送质检cdr，请求路径：{}，请求参数：{}", dictItem.getItemValue(), t);
            remoteCdrVO = cdrClient.sendQualityCdr(new URI(dictItem.getItemValue()), t);
        } catch (URISyntaxException e) {
            log.error(LOG_TAG + "字典配置的url[{}]有误", dictItem.getItemValue(), e);
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
     * @param LOG_TAG
     * @return
     */
    public <T> RemoteCdrVO resendQualityCdr(DictItem dictItem, T pushInfo, String LOG_TAG) {
        RemoteCdrVO remoteCdrVO = null;
        remoteCdrVO = sendQualityCdr(dictItem, pushInfo, LOG_TAG);
        if (remoteCdrVO != null || remoteCdrVO.getCode() == "200") {
            return remoteCdrVO;
        }
        for (int i = 1; i <= dynamicConfig.getPushnum(); i++) {
            log.info(LOG_TAG + "推送失败，开始第{}次重试", i);
            remoteCdrVO = sendQualityCdr(dictItem, pushInfo, LOG_TAG);
            if (remoteCdrVO != null && remoteCdrVO.getCode() == "200") {
                break;
            }
        }
        return remoteCdrVO;
    }

    /**
     * 获取质检推送对象
     *
     * @param cdrMessageDTO
     * @param companyCode
     * @return
     */
    public Map<String, Object> getRemoteCdrDTO(CdrMessageDTO cdrMessageDTO, String companyCode) {
        Map<String, Object> map = new HashMap<>();
        List<CallCenterSubCdr> subCdrs = cdrMessageDTO.getSubCdr();
        List<RemoteQualityCdr> qualitylist = new ArrayList<>();
        List<CallStateDetails> aftersafelist = new ArrayList<>();
        List<PushMisscallDataEntity> pushMisscallDataEntities = new ArrayList<>();
        for (CallCenterSubCdr subCdr : subCdrs) {
            getRemoteQuatity(cdrMessageDTO, companyCode, qualitylist, subCdr);
            getAfterSafe(cdrMessageDTO, companyCode, aftersafelist, subCdr);
            getPushMisscallDataEntity(pushMisscallDataEntities, subCdr);
        }

        RemoteQualityCdrDTO remoteQualityCdrDTO = new RemoteQualityCdrDTO(companyCode, "1", qualitylist);
        map.put("quality", remoteQualityCdrDTO);
        map.put("aftersale", aftersafelist);
        map.put("busy", pushMisscallDataEntities);
        return map;
    }

    private void getPushMisscallDataEntity(List<PushMisscallDataEntity> list, CallCenterSubCdr subCdr) {
        PushMisscallDataEntity pushMisscallDataEntity = new PushMisscallDataEntity();
        pushMisscallDataEntity.setAgentid(subCdr.getAgentId());
        // 与计费开始时间保持一致
        pushMisscallDataEntity.setCalltime(date2Str(subCdr.getACallStartTime()));
        pushMisscallDataEntity.setSystemcode("SDTF-SYSTEM");
        pushMisscallDataEntity.setPhone(subCdr.getCallingPartyNumber());
        // 根据缓存获取公司的vcc_id
        CompanyInfo companyInfoDTO = commonDataOperateService.getCompanyInfoDTO(subCdr.getCompanyCode());
        pushMisscallDataEntity.setVccid(companyInfoDTO.getVccId());
        list.add(pushMisscallDataEntity);
    }

    private void getRemoteQuatity(CdrMessageDTO cdrMessageDTO, String companyCode, List<RemoteQualityCdr> list, CallCenterSubCdr subCdr) {
        RemoteQualityCdr remoteQualityCdr = new RemoteQualityCdr();
        remoteQualityCdr.setCallId(subCdr.getCallId());
        // 呼入方向
        remoteQualityCdr.setDirection(subCdr.getDirection() == 0 ? 2 : 1);
        // 呼叫类型 1.IVR，3：坐席（坐席为空的为ivr）
        remoteQualityCdr.setCalltype(StringUtils.isEmpty(subCdr.getAgentId()) ? "1" : "3");
        // 坐席号码
        remoteQualityCdr.setCno(subCdr.getAgentId());
        // 原始主叫号码 计费oricallingnumber
        remoteQualityCdr.setOrigcalling(CommonUtils.replaceNumPerfix(subCdr.getCallerNumber()));
        // 原始被叫号码 计费oricallednumber
        remoteQualityCdr.setOrigcalled(CommonUtils.replaceNumPerfix(subCdr.getCalleeNumber()));
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
        remoteQualityCdr.setCallingnumber(CommonUtils.replaceNumPerfix(subCdr.getCallerNumber()));
        // 被叫号码 计费calledpartynumber
        remoteQualityCdr.setCallednumber(CommonUtils.replaceNumPerfix(subCdr.getCalleeNumber()));
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
        remoteQualityCdr.setCalldata(cdrMessageDTO.getCdrChanneldata() != null && cdrMessageDTO.getCdrChanneldata().getClientUuid() != null ? redissonUtil.get("cc_ivr_track_data_" + cdrMessageDTO.getCdrChanneldata().getClientUuid()) + "" : null);
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
        try {
            remoteQualityCdr.setTelthree(telephone == null ? null : telephone.substring(0, 3));
        } catch (Exception e) {
            remoteQualityCdr.setTelthree(telephone);
        }
        try {
            remoteQualityCdr.setTelseven(telephone == null ? null : telephone.substring(0, 7));
        } catch (Exception e) {
            remoteQualityCdr.setTelseven(telephone);
        }
        remoteQualityCdr.setAgentNumber(subCdr.getAgentId());
        remoteQualityCdr.setCustomerNumber(telephone);
        list.add(remoteQualityCdr);
    }


    private void getAfterSafe(CdrMessageDTO cdrMessageDTO, String companyCode, List<CallStateDetails> list, CallCenterSubCdr subCdr) {
        CallStateDetails remoteQualityCdr = new CallStateDetails();
        remoteQualityCdr.setCallId(subCdr.getCallId());
        // 呼入方向
        remoteQualityCdr.setDirection(subCdr.getDirection() == 0 ? 2 : 1);
        // 呼叫类型 1.IVR，3：坐席（坐席为空的为ivr）
        remoteQualityCdr.setCalltype(StringUtils.isEmpty(subCdr.getAgentId()) ? "1" : "3");
        // 坐席号码
        remoteQualityCdr.setCno(subCdr.getAgentId());
        // 原始主叫号码 计费oricallingnumber
        remoteQualityCdr.setOrigcalling(CommonUtils.replaceNumPerfix(subCdr.getCallerNumber()));
        // 原始被叫号码 计费oricallednumber
        remoteQualityCdr.setOrigcalled(CommonUtils.replaceNumPerfix(subCdr.getCalleeNumber()));
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
        remoteQualityCdr.setOrigcalling(CommonUtils.replaceNumPerfix(subCdr.getCallerNumber()));
        // 被叫号码 计费calledpartynumber
        remoteQualityCdr.setOrigcalled(CommonUtils.replaceNumPerfix(subCdr.getCalleeNumber()));
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
        remoteQualityCdr.setCalldata(cdrMessageDTO.getCdrChanneldata() != null && cdrMessageDTO.getCdrChanneldata().getClientUuid() != null ? redissonUtil.get("cc_ivr_track_data_" + cdrMessageDTO.getCdrChanneldata().getClientUuid()) + "" : null);
        // 单号/流水号
        remoteQualityCdr.setOrderid("");
        // 部门（班组号）
        String departments = null;
        if (subCdr.getAgentId() != null) {
            Map<String, String> map = redissonUtil.getStringMap(companyCode + ":agent_department_relationship");
            departments = map.get(subCdr.getAgentId());
        }
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
        try {
            remoteQualityCdr.setTelthree(telephone == null ? null : telephone.substring(0, 3));
        } catch (StringIndexOutOfBoundsException e) {
            remoteQualityCdr.setTelthree(telephone);
        }
        try {
            remoteQualityCdr.setTelseven(telephone == null ? null : telephone.substring(0, 7));
        } catch (StringIndexOutOfBoundsException e) {
            remoteQualityCdr.setTelseven(telephone);
        }
        remoteQualityCdr.setAgentNumber(subCdr.getAgentId());
        remoteQualityCdr.setCustomerNumber(telephone);
        list.add(remoteQualityCdr);
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
}
