package com.cqt.hmyc.web.bind.service.hdh;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.cloud.api.push.BindPushFeignClient;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.common.enums.CallResultCodeEnum;
import com.cqt.common.enums.CdrTypeCodeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.common.util.ThirdUtils;
import com.cqt.hmyc.config.properties.HdhProperties;
import com.cqt.hmyc.config.rabbitmq.DelayedPushRabbitConfig;
import com.cqt.hmyc.web.bind.manager.MqSender;
import com.cqt.hmyc.web.bind.service.LocalOrLongService;
import com.cqt.hmyc.web.corpinfo.mapper.PrivateNumberInfoMapper;
import com.cqt.hmyc.web.model.hdh.push.HdhPushIccpDTO;
import com.cqt.hmyc.web.model.hdh.push.HdhXPushIccpDTO;
import com.cqt.model.common.ThirdPushResult;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.push.entity.Callstat;
import com.cqt.model.push.entity.CdrResult;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 接收话单推送 处理
 *
 * @author dingsh
 * @date 2022/07/28
 */
@Service
@Slf4j
public class HdhPushService extends HdhBaseService {

    private final MqSender mqSender;

    private final BindPushFeignClient bindPushFeignClient;

    private final LocalOrLongService localOrLongService;

    private final ThreadPoolTaskExecutor saveExecutor;


    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    public HdhPushService(HdhProperties hdhProperties, RedissonUtil redissonUtil, MqSender mqSender,
                          BindPushFeignClient bindPushFeignClient,
                          LocalOrLongService localOrLongService,
                          ThreadPoolTaskExecutor saveExecutor, PrivateNumberInfoMapper privateNumberInfoMapper) {
        super(hdhProperties, redissonUtil);
        this.mqSender = mqSender;
        this.bindPushFeignClient = bindPushFeignClient;
        this.localOrLongService = localOrLongService;
        this.saveExecutor = saveExecutor;
        this.privateNumberInfoMapper = privateNumberInfoMapper;
    }

    /**
     * (hdh)接收话单推送 处理
     */
    public ThirdPushResult hdhPush(HdhPushIccpDTO hdhPushIccpDTO) {
        log.info("接收到通话话单  hdhPushIccpDTO： {}", JSONUtil.toJsonStr(hdhPushIccpDTO));
        //判断是否无绑定关系
//        if(CDR_UNBIND_CODE.equals(hdhPushIccpDTO.getFinishState())){
//           log.info("当前通话话单为无绑定话单 暂不推送：body: {} ",JSONUtil.toJsonStr(hdhPushIccpDTO));
//            return  ThirdPushResult.ok(ThirdConstant.HDH_SUCCESS_CODE,"成功");
//        }
        //根据bindId 获取本平台 信息缓存
        hdhPushIccpDTO=addCqtInfo(hdhPushIccpDTO);
        if (StringUtils.isEmpty(hdhPushIccpDTO.getVccId())){
            hdhPushIccpDTO.setVccId(getVccIdBySecretNo(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getX())));
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("vccId",hdhPushIccpDTO.getVccId());
            String s = JSONObject.toJSONString(hashMap);
            hdhPushIccpDTO.setUserData(s);
        }
        //转换话单实体 提交话单入库mq
        String xAreaCode= localOrLongService.checkMobilePhone(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getX()));
        Callstat callstat= buildCallStat(hdhPushIccpDTO,xAreaCode);
        mqSender.send(callstat, ThirdConstant.ICCPCDRSAVEEXCHANGE,ThirdConstant.ICCPCDRSAVEROUTEKEY,0);
        //调用push 服务
        PrivateBillInfo privateBillInfo=buildBillInfo(hdhPushIccpDTO,xAreaCode);
        String msgRequest= JSONObject.toJSONString(privateBillInfo);
        saveExecutor.execute(() -> {
            try {
                CdrResult cdrResult;
                cdrResult = bindPushFeignClient.thirdBillReceiver(msgRequest);
                if (ThirdConstant.HDH_SUCCESS_CODE.equals(cdrResult.getResult())) {
                    log.info("调用 push 成功，body: {}", msgRequest);
                }else {
                    mqSender.send(privateBillInfo, DelayedPushRabbitConfig.CDR_PUSH_DELAYED_EXCHANGE, DelayedPushRabbitConfig.CDR_PUSH_DELAYED_ROUTING, 0);
                }
            } catch (Exception e) {
                mqSender.send(privateBillInfo, DelayedPushRabbitConfig.CDR_PUSH_DELAYED_EXCHANGE, DelayedPushRabbitConfig.CDR_PUSH_DELAYED_ROUTING, 0);
                log.info("调用push 服务异常 请求消息：{} ，e: {}", privateBillInfo, e);
            }
        });
        log.info("##########################通话 话单返回成功");
        return  ThirdPushResult.ok(ThirdConstant.HDH_SUCCESS_CODE,"成功");
    }

    public ThirdPushResult hdhXPush(HdhXPushIccpDTO hdhXPushIccpDTO) {
        log.info("接收到通话话单  hdhPushIccpDTO： {}", JSONUtil.toJsonStr(hdhXPushIccpDTO));
        //转换话单实体 提交话单入库mq
        String xAreaCode= localOrLongService.checkMobilePhone(ThirdUtils.getNumberUn86(hdhXPushIccpDTO.getX()));
        HdhPushIccpDTO hdhPushIccpDTO1 = new HdhPushIccpDTO();
        BeanUtil.copyProperties(hdhXPushIccpDTO,hdhPushIccpDTO1);
        if (StringUtils.isEmpty(hdhPushIccpDTO1.getVccId())){
            hdhPushIccpDTO1.setVccId(getVccIdBySecretNo(ThirdUtils.getNumberUn86(hdhPushIccpDTO1.getX())));
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("vccId",hdhPushIccpDTO1.getVccId());
            String s = JSONObject.toJSONString(hashMap);
            hdhXPushIccpDTO.setUserData(s);
        }
        Callstat callstat= buildCallStat(hdhPushIccpDTO1,xAreaCode);
        mqSender.send(callstat, ThirdConstant.ICCPCDRSAVEEXCHANGE,ThirdConstant.ICCPCDRSAVEROUTEKEY,0);
        //判断是否无绑定关系
//        if(CDR_UNBIND_CODE.equals(hdhXPushIccpDTO.getFinishState())||StringUtils.isEmpty(hdhXPushIccpDTO.getBindId())){
//            log.info("当前通话话单为无绑定话单 暂不推送：body: {} ",JSONUtil.toJsonStr(hdhXPushIccpDTO));
//            return  ThirdPushResult.ok(ThirdConstant.HDH_SUCCESS_CODE,"成功");
//        }

        //调用push 服务
        PrivateBillInfo privateBillInfo=buildXBillInfo(hdhXPushIccpDTO,xAreaCode);
        String msgRequest= JSONObject.toJSONString(privateBillInfo);
        saveExecutor.execute(() -> {
            try {
                CdrResult cdrResult;
                cdrResult = bindPushFeignClient.thirdBillReceiver(msgRequest);
                if (ThirdConstant.HDH_SUCCESS_CODE.equals(cdrResult.getResult())) {
                    log.info("调用 push 成功，body: {}", msgRequest);
                }else {
                    mqSender.send(privateBillInfo, DelayedPushRabbitConfig.CDR_PUSH_DELAYED_EXCHANGE, DelayedPushRabbitConfig.CDR_PUSH_DELAYED_ROUTING, 0);
                }
            } catch (Exception e) {
                mqSender.send(privateBillInfo, DelayedPushRabbitConfig.CDR_PUSH_DELAYED_EXCHANGE, DelayedPushRabbitConfig.CDR_PUSH_DELAYED_ROUTING, 0);
                log.info("调用push 服务异常 请求消息：{} ，e: {}", privateBillInfo, e);
            }
        });
        log.info("##########################通话 话单返回成功");
        return  ThirdPushResult.ok(ThirdConstant.HDH_SUCCESS_CODE,"成功");
    }

    /**
     *  构造话单实体
     */
    private Callstat buildCallStat(HdhPushIccpDTO hdhPushIccpDTO,String xAreaCode){
        String chargeType=localOrLongService.getChargeType(hdhPushIccpDTO.getPeerNo(),hdhPushIccpDTO.getX());

        CallResultCodeEnum callResultCodeEnum=ThirdUtils.callResultCode(hdhPushIccpDTO.getFinishState());
        //2022-09-29 补充userData businessId解析入库
        String paraMap= hdhPushIccpDTO.getUserData();
        String businessId;
        //判断是否为json
        try {
            JSONObject jsonObject = JSONObject.parseObject(paraMap);
            businessId=jsonObject.getString("businessId");
        } catch (Exception e) {
            businessId="";
        }
        return Callstat.builder()
                .streamnumber(ThirdUtils.parseTime(hdhPushIccpDTO.getStartTime(),hdhPushIccpDTO.getCallTime()))
                .serviceid(StringUtils.isBlank(businessId) ? "" :businessId)
                .servicekey("900007")
                .startdateandtime("")
                .stopdateandtime("")
                .duration("")
                .callersubgroup("")
                .calleesubgroup("")
                .callerpnp("")
                .calleepnp("")
                .msserver("")
                .areanumber("")
                .dtmfkey(StringUtils.isBlank(hdhPushIccpDTO.getExtNumber()) ? "" : hdhPushIccpDTO.getExtNumber())
                .recordPush("")
                .calltype("0")
                .callcost(0)
                .calledpartynumber(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getX()))
                .callingpartynumber(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getCallNo()))
                .chargemode("0")
                .specificchargedpar(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getX()))
                .translatednumber(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getPeerNo()))
                .startdateandtime(ThirdUtils.getTime(hdhPushIccpDTO.getStartTime(),hdhPushIccpDTO.getCallTime()))
                .stopdateandtime(ThirdUtils.getTime(hdhPushIccpDTO.getFinishTime(),hdhPushIccpDTO.getCallTime()))
                .duration(String.valueOf(hdhPushIccpDTO.getCallDuration()))
                .chargeclass("102")
                .transparentparamet(hdhPushIccpDTO.getBindId())
                .acrcallid(ThirdUtils.acrCallId(hdhPushIccpDTO.getCallTime()))
                .oricallednumber(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getCallNo()))
                .oricallingnumber(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getPeerNo()))
                .reroute("1")
                .groupnumber(hdhPushIccpDTO.getVccId())
                .callcategory("1")
                .chargetype(chargeType)
                .acrtype("1")
                .videocallflag(ThirdUtils.videoCallFlag(hdhPushIccpDTO.getRecordUrl(), hdhPushIccpDTO.getCallDuration()))
                .forwardnumber(hdhPushIccpDTO.getCallId())
                .extforwardnumber(StringUtils.isBlank(hdhPushIccpDTO.getRingTime()) ? "" : hdhPushIccpDTO.getRingTime())
                .srfmsgid(StringUtils.isBlank(hdhPushIccpDTO.getRecordUrl()) ? "" : hdhPushIccpDTO.getRecordUrl())
                .begintime(hdhPushIccpDTO.getCallTime())
                .releasecause(hdhPushIccpDTO.getCallDuration() > 0 ? String.valueOf(CallResultCodeEnum.one.getCode()) :String.valueOf(callResultCodeEnum.getCode()))
                .releasereason(hdhPushIccpDTO.getCallDuration() > 0 ? CallResultCodeEnum.one.getDesc() : callResultCodeEnum.getDesc())
                .key5(CdrTypeCodeEnum.supplier.getCode())
                .userpin(hdhPushIccpDTO.getSupplierId())
                .key3(xAreaCode)
                .key2(hdhPushIccpDTO.getCallTime())
                .key1("")
                .key4("")
                .build();
    }

    /**
     * 构造话单实体
     */
    private PrivateBillInfo buildBillInfo(HdhPushIccpDTO hdhPushIccpDTO, String xAreaCode) {
        //获取 失败码
        CallResultCodeEnum callResultCodeEnum = ThirdUtils.callResultCode(hdhPushIccpDTO.getFinishState());
        return PrivateBillInfo.builder()
                .requestId(hdhPushIccpDTO.getRequestId())
                .areaCode(xAreaCode)
                .bindTime(hdhPushIccpDTO.getBindTime())
                .telY("")
                .appKey(hdhPushIccpDTO.getVccId())
                .recordId(hdhPushIccpDTO.getCallId())
                .bindId(hdhPushIccpDTO.getBindId())
                .serviceCode(StringUtils.isEmpty(hdhPushIccpDTO.getAppId())?10:22)
                .ext(StringUtils.isEmpty(hdhPushIccpDTO.getExtNumber())?"": hdhPushIccpDTO.getExtNumber())
                .telA(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getCallNo()))
                .telB(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getPeerNo()))
                .telX(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getX()))
                .beginTime(StringUtils.isBlank(hdhPushIccpDTO.getStartTime()) ? "":ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getStartTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss))
                .connectTime(StringUtils.isBlank(hdhPushIccpDTO.getStartTime()) ? "":ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getStartTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss))
                .alertingTime(StringUtils.isBlank(hdhPushIccpDTO.getRingTime()) ? "":ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getRingTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss))
                .releaseTime(StringUtils.isBlank(hdhPushIccpDTO.getFinishTime()) ? "":ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getFinishTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss))
                .callDuration(hdhPushIccpDTO.getCallDuration())
                .callResult(callResultCodeEnum.getCode())
                .recordFileUrl(StringUtils.isBlank(hdhPushIccpDTO.getRecordUrl()) ? "":hdhPushIccpDTO.getRecordUrl())
                .recordStartTime(StringUtils.isBlank(hdhPushIccpDTO.getStartTime()) || StringUtils.isEmpty(hdhPushIccpDTO.getBindId()) ? "": ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getStartTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss))
                .userData(hdhPushIccpDTO.getUserData())
                .recordFlag(1)
                .build();
    }

    private PrivateBillInfo buildXBillInfo(HdhXPushIccpDTO hdhPushIccpDTO, String xAreaCode) {
        //获取 失败码
        CallResultCodeEnum callResultCodeEnum = ThirdUtils.callResultCode(hdhPushIccpDTO.getFinishState());
        return PrivateBillInfo.builder()
                .requestId(hdhPushIccpDTO.getRequestId())
                .areaCode(xAreaCode)
                .bindTime(hdhPushIccpDTO.getBindTime())
                .telY("")
                .appKey(hdhPushIccpDTO.getVccId())
                .recordId(hdhPushIccpDTO.getCallId())
                .bindId(hdhPushIccpDTO.getBindId())
                .ext(StringUtils.isEmpty(hdhPushIccpDTO.getExt())?"":hdhPushIccpDTO.getExt())
                .telA(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getCallNo()))
                .telB(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getPeerNo()))
                .telX(ThirdUtils.getNumberUn86(hdhPushIccpDTO.getX()))
                .beginTime(StringUtils.isBlank(hdhPushIccpDTO.getStartTime()) ? "":ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getStartTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss))
                .connectTime(StringUtils.isBlank(hdhPushIccpDTO.getStartTime()) ? "":ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getStartTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss))
                .alertingTime(StringUtils.isBlank(hdhPushIccpDTO.getRingTime()) ? "":ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getRingTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss))
                .releaseTime(StringUtils.isBlank(hdhPushIccpDTO.getFinishTime()) ? "":ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getFinishTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss))
                .callDuration(hdhPushIccpDTO.getCallDuration() == null ? 0 : hdhPushIccpDTO.getCallDuration())
                .callResult(callResultCodeEnum.getCode())
                .recordFileUrl(StringUtils.isBlank(hdhPushIccpDTO.getRecordUrl()) ? "":hdhPushIccpDTO.getRecordUrl())
                .recordStartTime(StringUtils.isBlank(hdhPushIccpDTO.getStartTime()) || StringUtils.isEmpty(hdhPushIccpDTO.getBindId()) ? "": ThirdUtils.timeStampTranfer(hdhPushIccpDTO.getStartTime(),ThirdConstant.yyyyMMddHHmmss,ThirdConstant.yyyy_MM_dd_HH_mm_ss))
                .userData(hdhPushIccpDTO.getUserData())
                .recordFlag(1)
                .build();
    }

    public String getVccIdBySecretNo(String secretNo) {
        if (StrUtil.isEmpty(secretNo)) {
            return "";
        }
        String vccId = redissonUtil.getString(PrivateCacheUtil.getVccIdByNumberKey(secretNo));
        if (StrUtil.isEmpty(vccId)) {
            PrivateNumberInfo privateNumberInfo = privateNumberInfoMapper.selectById(secretNo);
            if (ObjectUtil.isNotEmpty(privateNumberInfo)) {
                vccId =  privateNumberInfo.getVccId();
            }
        }
        log.info("中间号: {}, 归属企业: {}", secretNo, vccId);
        return vccId;
    }
}
