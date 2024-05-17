package com.cqt.broadnet.web.x.service.release;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.broadnet.common.model.x.dto.CallReleaseDTO;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.web.x.service.PrivateCorpBusinessInfoService;
import com.cqt.broadnet.web.x.service.TelCodeService;
import com.cqt.broadnet.web.x.service.retry.CallBillPushRetryImpl;
import com.cqt.common.constants.RabbitMqConstant;
import com.cqt.common.enums.CallEventEnum;
import com.cqt.common.enums.CdrTypeCodeEnum;
import com.cqt.common.enums.ResultCodeEnum;
import com.cqt.common.enums.ServiceCodeEnum;
import com.cqt.common.util.AuthUtil;
import com.cqt.common.util.ThirdUtils;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.push.entity.Callstat;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.rabbitmq.retry.model.PushRetryDataDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.collection.IntObjectHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author linshiqiang
 * date:  2023-02-16 14:50
 * 通话话单入库推送处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallBillStorePushService extends AbstractStorePushService {

    private final TelCodeService TelCodeService;

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    private final CallBillPushRetryImpl callBillPushRetryImpl;

    private final PrivateCorpBusinessInfoService privateCorpBusinessInfoService;

    private static final IntObjectHashMap<Integer> RELEASE_CAUSE = new IntObjectHashMap<>(32);

    static {
        // TODO 内部call_result和广电release_cause对应关系
        RELEASE_CAUSE.put(31, Integer.valueOf(1));
        RELEASE_CAUSE.put(17, Integer.valueOf(2));
        RELEASE_CAUSE.put(18, Integer.valueOf(4));
        RELEASE_CAUSE.put(19, Integer.valueOf(4));
        RELEASE_CAUSE.put(1, Integer.valueOf(6));
        RELEASE_CAUSE.put(4, Integer.valueOf(7));
        RELEASE_CAUSE.put(21, Integer.valueOf(11));

        /*
         *  本平台通话结束原因码call_result：
         *    1、正常接通
         *    2、呼叫遇忙；
         *    3、用户不在服务区；
         *    4、用户无应答；空号识别没有到，根据结束时间-振铃时间>50s   判断为无应答
         *    5、用户关机；
         *    6、空号；
         *    7、停机；
         *    8、号码过期
         *    9、主叫应答，被叫应答前挂机(振铃后挂机) 有振铃时间
         *    91、主叫应答，被叫应答前挂机(振铃前挂机) 无振铃时间
         *    10 、正在通话中
         *    11、 拒接
         *       (1).空号识别为(呼叫遇忙或者正在通话中，根据结束时间-开始时间>16s,判断为拒接
         *       (2).空号识别没识别到，22<结束时间-振铃时间<50,判断为拒接
         *    12、请不要挂机
         *    99、其他
         *    20：主动取消呼叫
         */

        /*
         * 广电 release_cause
         * 呼叫释放原因，Q.850。
         * 取值样例（包括但不限于如下取值）
         * 1：未分配的号码（空号）
         * 3：无至目的地的路由
         * 4：停机
         * 6：不可接受的信道
         * 16：正常清除
         * 17：用户忙
         * 18：无用户响应
         * 19：已有用户提醒，但无应答
         * 21：呼叫拒绝
         * 22：号码改变
         * 26：清除未选择的用户
         * 27：终点故障
         * 28：无效号码格式（不完全的号码）
         * 29：设施被拒绝
         * 30：对状态询问的响应
         * 31：正常--未规定
         * 34：无电路/信道可用
         * 38：网络故障
         * 41：临时故障
         * 42：交换设备拥塞
         * 43：接入信息被丢弃
         * 44：请求的电路/信道不可用
         * 47：资源不可用--未规定
         * 49：服务质量不可用
         * 50：未预订所请求的设施
         * 55：IncomingcallsbarredwithintheCUG
         * 57：承载能力未认可(未开通通话功能）
         * 58：承载能力目前不可用
         * 63：无适用的业务或任选项目-未规定
         * 65：承载业务不能实现
         * 68：ACMequaltoorgreaterthanACMmax
         * 69：所请求的设施不能实现
         * 70：仅能获得受限数字信息承载能力
         * 79：业务不能实现-未规定)
         * 81：无效处理识别码
         * 87：UsernotmemberofCUG
         * 88：非兼容目的地址
         * 91：无效过渡网选择
         * 95：无效消息-未规定
         * 96：必选消息单元差错
         * 97：消息类型不存在或不能实现
         * 98：消息与控制状态不兼容-消息类型不存在或不能实现
         * 99：信息单元不存在或不能实现
         * 100：无效信息单元内容
         * 101：消息与呼叫状态不兼容
         * 102：定时器超时恢复
         * 111：协议差错-未规定
         * 127：互通-未规定
         * 短信流程无释放原因值
         * [缺省值]
         * 16-正常清除。
         */
    }

    /**
     * 通话话单处理入口
     */
    @Async("saveExecutor")
    public void storeCallBill(CallReleaseDTO.EndCallRequest endCallRequest,
                              PrivateCorpBusinessInfoDTO businessInfoDTO) throws JsonProcessingException {
        Optional<BindInfoApiVO> bindInfoVoOptional = privateCorpBusinessInfoService.getBindInfoVO(endCallRequest.getCallId());
        String vccId = businessInfoDTO.getVccId();
        // 构造
        PrivateBillInfo privateBillInfo = getPrivateBillInfo(bindInfoVoOptional, endCallRequest);

        // 发mq入库
        Callstat callstat = getCallstat(privateBillInfo, vccId);
        rabbitTemplate.convertAndSend(RabbitMqConstant.ICCP_CDR_SAVE_EXCHANGE, RabbitMqConstant.ICCP_CDR_SAVE_ROUTE_KEY, callstat);
        log.info("callId: {}, vccId: {}, 通话话单推送mq入库finish", endCallRequest.getCallId(), vccId);
        // 设置ts和sign
        setSign(privateBillInfo, businessInfoDTO);
        // 推送接口
        pushToCustomer(privateBillInfo.getRecordId(),
                privateBillInfo,
                vccId,
                businessInfoDTO.getCdrPushFlag(),
                businessInfoDTO.getBillPushUrl(),
                businessInfoDTO.getPushRetryNum(),
                Duration.ofMinutes(businessInfoDTO.getPushRetryMin())
        );

        // 挂断事件 hangup
        PrivateStatusInfo privateStatusInfo = buildStatusInfo(bindInfoVoOptional, endCallRequest);
        pushToCustomer(privateBillInfo.getRecordId(),
                privateStatusInfo,
                vccId,
                businessInfoDTO.getStatusPushFlag(),
                businessInfoDTO.getStatusPushUrl(),
                businessInfoDTO.getPushRetryNum(),
                Duration.ofMinutes(businessInfoDTO.getPushRetryMin()));
    }

    public PrivateStatusInfo buildStatusInfo(Optional<BindInfoApiVO> bindInfoVoOptional, CallReleaseDTO.EndCallRequest endCallRequest) {
        PrivateStatusInfo statusInfo = new PrivateStatusInfo();
        statusInfo.setEvent(CallEventEnum.hangup.name());
        statusInfo.setRecordId(endCallRequest.getCallId());
        // 未提供, 自己存
        statusInfo.setBindId(bindInfoVoOptional.map(BindInfoApiVO::getBindId).orElse(""));
        statusInfo.setCaller(endCallRequest.getCallerNum());
        statusInfo.setCalled(endCallRequest.getCalleeNum());
        statusInfo.setTelX(endCallRequest.getSecretNo());
        statusInfo.setCurrentTime(endCallRequest.getReleaseTime());
        // 需转化
        statusInfo.setCallResult(RELEASE_CAUSE.get(endCallRequest.getReleaseCause()));
        statusInfo.setExt(endCallRequest.getExtensionNo());
        return statusInfo;
    }

    private void setSign(PrivateBillInfo privateBillInfo, PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO) {
        privateBillInfo.setTs(System.currentTimeMillis());
        TreeMap<String, Object> treeMap = objectMapper.convertValue(privateBillInfo, new TypeReference<TreeMap<String, Object>>() {
        });
        String sign = AuthUtil.createSign(treeMap, privateCorpBusinessInfoDTO.getVccId(), privateCorpBusinessInfoDTO.getSecretKey());
        privateBillInfo.setSign(sign);
    }

    /**
     * 获取本平台通用 通话结束后话单
     *
     * @param endCallRequest 广电传的通话结束后话单
     */
    private PrivateBillInfo getPrivateBillInfo(Optional<BindInfoApiVO> bindInfoVoOptional, CallReleaseDTO.EndCallRequest endCallRequest) throws JsonProcessingException {
        return PrivateBillInfo.builder()
                .ts(System.currentTimeMillis())
                .recordId(endCallRequest.getCallId())
                .bindId(bindInfoVoOptional.map(BindInfoApiVO::getBindId).orElse(""))
                // TODO 未提供
                .serviceCode(StrUtil.isEmpty(endCallRequest.getExtensionNo()) ? ServiceCodeEnum.AXB.getCode() : ServiceCodeEnum.AXE.getCode())
                // 可能是固话?
                .areaCode(bindInfoVoOptional.map(BindInfoApiVO::getAreaCode).orElse(""))
                .telA(endCallRequest.getCallerNum())
                .telB(endCallRequest.getCalleeNum())
                .telX(endCallRequest.getSecretNo())
                .bindTime("")
                .beginTime(endCallRequest.getStartTimeA())
                .calloutTime(endCallRequest.getCallOutTime())
                .connectTime(endCallRequest.getStartTime())
                .connectTime(endCallRequest.getRingTime())
                .releaseTime(endCallRequest.getReleaseTime())
                // TODO 未提供
                .callDuration(0)
                // TODO 需转化
                .callResult(getCallResult(endCallRequest.getReleaseDir(), endCallRequest.getReleaseCause()))
                .requestId("")
                // TODO 未提供
                .recordStartTime("")
                // TODO 未提供
                .recordFlag(StrUtil.isEmpty(endCallRequest.getRecordUrl()) ? 0 : 1)
                .recordFileUrl(StrUtil.isNotBlank(endCallRequest.getRecordUrl()) ? endCallRequest.getRecordUrl() : "")
                .ext(endCallRequest.getExtensionNo())
                .build();
    }

    /**
     * 获取本平台话单入库需要的数据格式
     */
    private Callstat getCallstat(PrivateBillInfo billInfo, String vccId) {
        String chargeType = TelCodeService.getChargeType(billInfo.getTelB(), billInfo.getTelX());
        ResultCodeEnum callResultCodeEnum = ThirdUtils.cnResultCode(billInfo.getCallResult());
        return Callstat.builder()
                .streamnumber(ThirdUtils.convert(billInfo.getCalloutTime()) + IdUtil.fastSimpleUUID().substring(0, 16))
                .serviceid("")
                .servicekey("900007")
                .callersubgroup("")
                .calleesubgroup("")
                .callerpnp("")
                .calleepnp("")
                .msserver("")
                .areanumber("")
                .dtmfkey("")
                .recordPush("")
                .calltype("0")
                .callcost(0)
                .calledpartynumber(billInfo.getTelX())
                .callingpartynumber(billInfo.getTelA())
                .chargemode("0")
                .specificchargedpar(billInfo.getTelX())
                .translatednumber(billInfo.getTelB())
                .startdateandtime(ThirdUtils.convert(billInfo.getBeginTime()))
                .stopdateandtime(ThirdUtils.convert(billInfo.getReleaseTime()))
                .duration(String.valueOf(billInfo.getCallDuration()))
                .chargeclass("102")
                .transparentparamet(billInfo.getBindId())
                .acrcallid(ThirdUtils.acrCallId(ThirdUtils.convert(billInfo.getBeginTime())))
                .oricallednumber(billInfo.getTelA())
                .oricallingnumber(billInfo.getTelB())
                .reroute("1")
                .groupnumber(vccId)
                .callcategory("1")
                .chargetype(chargeType)
                .acrtype("1")
                .videocallflag(ThirdUtils.videoCallFlag(billInfo.getRecordFileUrl(), billInfo.getCallDuration()))
                .forwardnumber(billInfo.getRecordId())
                .extforwardnumber(StrUtil.isBlank(billInfo.getAlertingTime()) ? "" : ThirdUtils.convert(billInfo.getAlertingTime()))
                .srfmsgid(StrUtil.isBlank(billInfo.getRecordFileUrl()) ? "" : billInfo.getRecordFileUrl())
                .begintime(ThirdUtils.convert(billInfo.getBeginTime()))
                .releasecause(String.valueOf(callResultCodeEnum.getCode()))
                .releasereason(callResultCodeEnum.getDesc())
                .key5(CdrTypeCodeEnum.supplier.getCode())
                .userpin(privateNumberBindProperties.getSupplierId())
                .bNumFail("")
                .key3(billInfo.getAreaCode())
                .key2(ThirdUtils.convert(billInfo.getBeginTime()))
                .key1("")
                .key4("")
                .build();
    }

    /**
     * release_dir
     * 0：平台释放   -
     * 1：主叫释放   -   20
     * 2：被叫释放   -   9
     * 短信流程无此值，固定取值为0-平台释放
     */
    private Integer getCallResult(Integer releaseDir, Integer releaseCause) {
        Integer code = RELEASE_CAUSE.get(releaseCause);
        if (ObjectUtil.isNotEmpty(code)) {
            return code;
        }

        // 其他
        return 99;
    }

    @Override
    public void pushDataToMq(PushRetryDataDTO pushRetryDataDTO) {
        callBillPushRetryImpl.pushDataToMq(pushRetryDataDTO);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public Integer getPushTimeout() {
        return privateNumberBindProperties.getPushTimeout();
    }

    @Override
    public String type() {
        return "通话话单";
    }
}
