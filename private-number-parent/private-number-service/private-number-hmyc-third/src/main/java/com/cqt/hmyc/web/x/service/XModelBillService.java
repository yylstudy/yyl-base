package com.cqt.hmyc.web.x.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.service.hdh.HdhPushService;
import com.cqt.hmyc.web.corpinfo.mapper.PrivateNumberInfoMapper;
import com.cqt.hmyc.web.model.hdh.push.HdhPushIccpDTO;
import com.cqt.hmyc.web.model.hdh.push.HdhXPushIccpDTO;
import com.cqt.hmyc.web.x.model.XModelBillDTO;
import com.cqt.hmyc.web.x.model.XModelRecordDTO;
import com.cqt.hmyc.web.x.model.XModelStatusDTO;
import com.cqt.hmyc.web.x.rabbitmq.DelayProducer;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.model.common.ThirdPushResult;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

/**
 * @author huweizhong
 * date  2023/6/7 16:42
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class XModelBillService {

    private final HdhPushService pushService;

    private final DelayProducer delayProducer;

    private final RedissonUtil redissonUtil;

    private final ObjectMapper objectMapper;

    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    public ThirdPushResult bill(XModelBillDTO xmodelBillDTO) throws JsonProcessingException {
        if (log.isInfoEnabled()) {
            log.info("和多号, x模式话单报文: {}", objectMapper.writeValueAsString(xmodelBillDTO));
        }
        if (StringUtils.isEmpty(xmodelBillDTO.getSecretNo())){
            return ThirdPushResult.ok();
        }
        if (StringUtils.isEmpty(xmodelBillDTO.getBindId())){
            HdhXPushIccpDTO hdhPushIccp= toHdhBillNoBind(xmodelBillDTO);
            return pushService.hdhXPush(hdhPushIccp);
        }

        HdhXPushIccpDTO hdhPushIccpDTO = toHdhBill(xmodelBillDTO);
        String bindId = redissonUtil.getString("bindId_" + xmodelBillDTO.getBindId());
        if (StringUtils.isNotEmpty(bindId)){
            hdhPushIccpDTO.setBindId(bindId);
        }
        log.info("转化后的话单：{}",hdhPushIccpDTO);
        Integer recordFlag = getRecordFlag(xmodelBillDTO);

        if (hdhPushIccpDTO.getCallDuration() == null ||hdhPushIccpDTO.getCallDuration() == 0 || recordFlag != 1){
            return pushService.hdhXPush(hdhPushIccpDTO);
        }
        delayProducer.send(hdhPushIccpDTO);
        return ThirdPushResult.ok();
    }

    private PrivateNumberInfo getPrivateNumberInfo(String number){
        String numberInfoKey = PrivateCacheUtil.getNumberInfo(number);
        PrivateNumberInfo privateNumberInfo = null;
        try {
            String numberInfo = redissonUtil.getStringX(numberInfoKey);
            privateNumberInfo = JSONObject.parseObject(numberInfo, PrivateNumberInfo.class);
            if (ObjectUtil.isEmpty(privateNumberInfo)){
                privateNumberInfo = privateNumberInfoMapper.selectById(number);
                redissonUtil.setStringX(numberInfoKey,JSONObject.toJSONString(privateNumberInfo));
            }
        }catch (Exception e){
            log.error("查询平台号码信息异常："+e);
        }
        return privateNumberInfo;
    }

    private Integer getRecordFlag(XModelBillDTO xmodelBillDTO){
        try {
            PrivateNumberInfo privateNumberInfo = getPrivateNumberInfo(clear86(xmodelBillDTO.getSecretNo()));
            if (ObjectUtil.isEmpty(privateNumberInfo)){
                return 3;
            }
            String bindInfo;
            assert privateNumberInfo != null;
            if ( "AXE".equals(privateNumberInfo.getBusinessType())){
                if (xmodelBillDTO.getExtensionNo()==null){
                    return 3;
                }
                String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(privateNumberInfo.getVccId(), privateNumberInfo.getBusinessType(), privateNumberInfo.getNumber(), xmodelBillDTO.getExtensionNo());
                bindInfo = redissonUtil.getString(extBindInfoKey);
                if (bindInfo == null){
                    //输错分机号
                    return 0;
                }
                PrivateBindInfoAxe bindInfoVO = JSONObject.parseObject(bindInfo, PrivateBindInfoAxe.class);

                return bindInfoVO.getEnableRecord();

            }
            String bindInfoKey = PrivateCacheUtil.getBindInfoKey(privateNumberInfo.getVccId(), privateNumberInfo.getBusinessType(), clear86(xmodelBillDTO.getCallNo()), privateNumberInfo.getNumber());
            bindInfo = redissonUtil.getString(bindInfoKey);
            PrivateBindInfoAxb bindInfoVO = JSONObject.parseObject(bindInfo, PrivateBindInfoAxb.class);
            return bindInfoVO.getEnableRecord();
        }catch (Exception e){
            log.info("查询绑定关系异常");
        }
      return 3;
    }

    private HdhXPushIccpDTO toHdhBill(XModelBillDTO xmodelBillDTO){
        PrivateNumberInfo privateNumberInfo = getPrivateNumberInfo(clear86(xmodelBillDTO.getSecretNo()));
        String bindTime = null;
        String requestId = null;
        String vccId = null;
        String s = null;
        if (ObjectUtil.isNotEmpty(privateNumberInfo)){
             vccId = privateNumberInfo.getVccId();
            if ("AXE".equals(privateNumberInfo.getBusinessType())){
                String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(privateNumberInfo.getVccId(), privateNumberInfo.getBusinessType(), privateNumberInfo.getNumber(), xmodelBillDTO.getExtensionNo());
                String bindInfo = redissonUtil.getString(extBindInfoKey);
                PrivateBindInfoAxe bindInfoVO = JSONObject.parseObject(bindInfo, PrivateBindInfoAxe.class);
                if (ObjectUtil.isNotEmpty(bindInfoVO)){
                    bindTime = DateUtil.format(bindInfoVO.getCreateTime(), ThirdConstant.yyyy_MM_dd_HH_mm_ss);
                    requestId = bindInfoVO.getRequestId();
                }
            }else {
                String bindInfoKey = PrivateCacheUtil.getBindInfoKey(privateNumberInfo.getVccId(), privateNumberInfo.getBusinessType(), clear86(xmodelBillDTO.getCallNo()), privateNumberInfo.getNumber());
                String bindInfo = redissonUtil.getString(bindInfoKey);
                PrivateBindInfoAxb bindInfoVO = JSONObject.parseObject(bindInfo, PrivateBindInfoAxb.class);
                if (ObjectUtil.isNotEmpty(bindInfoVO)){
                    bindTime = DateUtil.format(bindInfoVO.getCreateTime(), ThirdConstant.yyyy_MM_dd_HH_mm_ss);
                    requestId = bindInfoVO.getRequestId();
                }
            }
            HashMap<String, String> map = new HashMap<>();
            map.put("vccId",vccId);
             s = JSONUtil.toJsonStr(map);
        }
        return HdhXPushIccpDTO.builder().bindId(xmodelBillDTO.getBindId())
                .callId(xmodelBillDTO.getCallId())
                .callNo(xmodelBillDTO.getCallNo())
                .peerNo(xmodelBillDTO.getPeerNo())
                .x(xmodelBillDTO.getSecretNo())
                .callTime(xmodelBillDTO.getCallTime())
                .startTime(xmodelBillDTO.getStartTime())
                .finishTime(xmodelBillDTO.getFinishTime())
                .callDuration(xmodelBillDTO.getCallDuration())
                .finishType(xmodelBillDTO.getFinishType())
                .finishState(xmodelBillDTO.getFinishState())
                .userData(xmodelBillDTO.getData())
                .ringTime(xmodelBillDTO.getRingTime())
                .bindTime(bindTime)
                .requestId(requestId)
                .vccId(vccId)
                .ext(xmodelBillDTO.getExtensionNo())
                .userData(s)
                .build();

    }

    private HdhXPushIccpDTO toHdhBillNoBind(XModelBillDTO xmodelBillDTO){
        PrivateNumberInfo privateNumberInfo = getPrivateNumberInfo(clear86(xmodelBillDTO.getSecretNo()));
        String s = null;
        String vccId = null;
        if (ObjectUtil.isNotEmpty(privateNumberInfo)){
            HashMap<String, String> map = new HashMap<>();
            map.put("vccId",vccId);
            s = JSONUtil.toJsonStr(map);
        }
        return HdhXPushIccpDTO.builder()
                .vccId(vccId)
                .callId(xmodelBillDTO.getCallId())
                .callNo(xmodelBillDTO.getCallNo())
                .peerNo(xmodelBillDTO.getPeerNo())
                .x(xmodelBillDTO.getSecretNo())
                .callTime(xmodelBillDTO.getCallTime())
                .startTime(xmodelBillDTO.getStartTime())
                .finishTime(xmodelBillDTO.getFinishTime())
                .callDuration(xmodelBillDTO.getCallDuration())
                .finishType(xmodelBillDTO.getFinishType())
                .finishState(xmodelBillDTO.getFinishState())
                .userData(xmodelBillDTO.getData())
                .ringTime(xmodelBillDTO.getRingTime())
                .ext(xmodelBillDTO.getExtensionNo())
                .userData(s)
                .build();

    }


    public static String clear86(String number){
        if (StringUtils.isEmpty(number)){
            return "";
        }
        if (number.startsWith("86")){
            number = number.substring(2);
        }
        return number;
    }

    public ThirdPushResult record(XModelRecordDTO recordDTO) throws JsonProcessingException {
        if (log.isInfoEnabled()) {
            log.info("和多号, x模式录音报文: {}", objectMapper.writeValueAsString(recordDTO));
        }
        String thirdRecordUrlKey = PrivateCacheUtil.getThirdRecordUrlKey(recordDTO.getCallId());
        redissonUtil.setString(thirdRecordUrlKey,recordDTO.getRecordUrl());
        return ThirdPushResult.ok();
    }


}
