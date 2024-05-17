package com.cqt.unicom.controller;


import com.alibaba.fastjson.JSONObject;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.model.unicom.dto.*;
import com.cqt.model.unicom.entity.CustomerReceivesDataInfo;
import com.cqt.model.unicom.entity.MeituanSmsStatePush;
import com.cqt.model.unicom.entity.SmsRequest;
import com.cqt.model.unicom.vo.GeneralMessageVO;
import com.cqt.unicom.service.UnicomCallListPushService;
import com.cqt.unicom.service.UnicomCallStatusPushService;
import com.cqt.unicom.service.UnicomNumberBindService;
import com.cqt.unicom.service.UnicomSmsPushService;
import com.cqt.unicom.util.BankCallChannelUtil;
import com.cqt.unicom.util.UnicomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author zhengsuhao
 * @date 2022/12/5
 */
@Api(tags = "联通集团总部(江苏)能力对接")
@RestController
@Slf4j
@RequestMapping("unicom")
public class UnicomCapacityDockingController {


    @Autowired
    @Qualifier("unicomNumberBindServiceImpl")
    private UnicomNumberBindService unicomNumberBindService;

    @Autowired
    @Qualifier("unicomCallListPushServiceImpl")
    private UnicomCallListPushService unicomCallListPushService;

    @Autowired
    @Qualifier("unicomCallStatusPushServiceImpl")
    private UnicomCallStatusPushService unicomCallStatusPushService;

    @Autowired
    @Qualifier("unicomSmsPushServiceImpl")
    private UnicomSmsPushService unicomSmsPushService;


    @Autowired
    private BankCallChannelUtil bankCallChannelUtil;


    /**
     * @param numberBindingQueryDTO 联通集团总部(江苏)号码绑定查询入参
     * @return NumberBindingQueryVO
     */
    @ApiOperation("查询号码绑定关系接口")
    @PostMapping("query")
    public GeneralMessageVO queryBindInfo(@Valid @RequestBody NumberBindingQueryDTO numberBindingQueryDTO) throws JsonProcessingException {
        //查询返回绑定关系
        log.info("接收到callID：{},主叫号码：{}--开始查询绑定关系", numberBindingQueryDTO.getCallId(), numberBindingQueryDTO.getPhoneNumberA());
        return unicomNumberBindService.getNumberBindingQuery(numberBindingQueryDTO);
    }

    @ApiOperation("集团加密话单推送接口")
    @PostMapping("call/encrypt/push")
    public void callEncryptPush(@Valid @Validated @RequestBody String reqMsg, HttpServletResponse response) {

        //解密话单
        String s = bankCallChannelUtil.testBy1024(reqMsg);
        if (StringUtils.isNotEmpty(s)){
            CallListPushDTO callListPushDTO = JSONObject.parseObject(s, CallListPushDTO.class);
            //异步返回集团报文
            GeneralMessageVO generalMessageVO = GeneralMessageVO.ok("通话话单报文推送成功");
            UnicomUtil.responToClient(response, generalMessageVO);
            pushToCustomer(callListPushDTO);
            return;
        }
        //异步返回集团报文
        GeneralMessageVO generalMessageVO = GeneralMessageVO.fail("500","话单解密失败");
        UnicomUtil.responToClient(response, generalMessageVO);
        log.info("话单解密失败");
    }


    /**
     * @param callListPushDTO 联通集团总部(江苏)话单推送入参
     * @param response        异步响应
     */
    @ApiOperation("集团话单推送接口")
    @PostMapping("call/list/push")
    public void callListPush(@Valid @Validated @RequestBody CallListPushDTO callListPushDTO, HttpServletResponse response) {
        //异步返回集团报文
        GeneralMessageVO generalMessageVO = GeneralMessageVO.ok("通话话单报文推送成功");
        UnicomUtil.responToClient(response, generalMessageVO);
        pushToCustomer(callListPushDTO);
    }

    /**
     * @param callConnectionStatusDTO 联通集团总部(江苏)业务开始推送入参
     * @param response 异步响应
     */
    @ApiOperation("集团通话状态事务开始推送接口")
    @PostMapping("callStatus/callOut/push")
    public void callStatusFirstPush(@Valid @RequestBody CallConnectionStatusDTO callConnectionStatusDTO, HttpServletResponse response) {
        log.info("集团通话状态接收到callID：{},主叫号码：{}--开始通话状态推送", callConnectionStatusDTO.getCallId(), callConnectionStatusDTO.getPhoneNumberA());
        //将集团报文转为客户报文
        PrivateStatusInfo privateStatusInfo = unicomCallStatusPushService.getCustomerCallStatus(callConnectionStatusDTO);
        //异步返回集团成功报文
        GeneralMessageVO generalMessageVO = GeneralMessageVO.ok();
        UnicomUtil.responToClient(response, generalMessageVO);
        //调用private-num-push服务
        String feignResult = unicomCallStatusPushService.putPrivateNumPush(privateStatusInfo);
        log.info("集团通话状态feign调用结果{},接收到callID：{},主叫号码：{}--结束通话状态推送", feignResult, callConnectionStatusDTO.getCallId(), callConnectionStatusDTO.getPhoneNumberA());

    }


    /**
     * @param callBusinessEventDTO 联通集团总部(江苏)业务事件通话状态推送入参
     * @param response 异步响应
     */
    @ApiOperation("集团通话状态业务事件推送接口")
    @PostMapping("callStatus/ringing/answer/push")
    public void callStatusSecondPush(@Valid @RequestBody CallBusinessEventDTO callBusinessEventDTO, HttpServletResponse response) {
        log.info("集团通话状态接收到callID：{},主叫号码：{}--开始通话状态推送", callBusinessEventDTO.getCallId(), callBusinessEventDTO.getPhoneNumberA());
        //将集团报文转为客户报文
        PrivateStatusInfo privateStatusInfo = unicomCallStatusPushService.getCustomerCallStatus(callBusinessEventDTO);
        //异步返回集团成功报文
        GeneralMessageVO generalMessageVO = GeneralMessageVO.ok();
        UnicomUtil.responToClient(response, generalMessageVO);
        //调用private-num-push服务
        String feignResult = unicomCallStatusPushService.putPrivateNumPush(privateStatusInfo);
        log.info("集团通话状态feign调用结果{},接收到callID：{},主叫号码：{}--结束通话状态推送", feignResult, callBusinessEventDTO.getCallId(), callBusinessEventDTO.getPhoneNumberA());
    }


    /**
     * @param smsStatusDTO 联通集团总部(江苏)短信状态推送入参
     * @param response 异步响应
     */
    @ApiOperation("集团短信状态推送接口")
    @PostMapping("smsStatus/push")
    public void smsStatusPush(@Valid @Validated @RequestBody SmsStatusDTO smsStatusDTO, HttpServletResponse response) {
        log.info("收到集团短信状态报文{}", JSONObject.toJSONString (smsStatusDTO));
        //将集团报文转为客户报文
        SmsRequest smsRequest = unicomSmsPushService.getSmsStatus(smsStatusDTO);
        //异步返回集团成功报文
        GeneralMessageVO generalMessageVO = GeneralMessageVO.ok();
        UnicomUtil.responToClient(response, generalMessageVO);

        if(StringUtils.isBlank (smsRequest.getBody ().getInPhoneNumber ())){
            return;
        }
        //将短信放入MQ
        String mqResult = unicomSmsPushService.setMessageQueue(smsRequest);
        //将短信实体转换为第三方短信实体

        MeituanSmsStatePush meituanSmsStatePush = unicomSmsPushService.getMeituanSmsStatePush(smsRequest);
        if(StringUtils.isBlank (meituanSmsStatePush.getSenderShow ())){
            return;
        }
        if(StringUtils.isBlank (meituanSmsStatePush.getAppkey ())){
            return;
        }
        //调用private-num-sms服务
        String smsResult = unicomSmsPushService.putPrivateNumSms(meituanSmsStatePush);
        log.info("集团短信状态队列调用结果：{}，feign调用结果:{},接收到callID：{},主叫号码：{}--结束短信状态推送", mqResult, smsResult, smsStatusDTO.getCallId(), smsStatusDTO.getPhoneNumberA());
    }

    public void pushToCustomer(CallListPushDTO callListPushDTO){
        log.info("集团话单接收到callID：{},主叫号码：{},释放原因:{}--开始话单推送", callListPushDTO.getCallId(), callListPushDTO.getPhoneNumberA(), callListPushDTO.getReleaseCause());
        //将集团报文转为客户报文
        CustomerReceivesDataInfo customerReceivesDataInfo = unicomCallListPushService.getCustomerReceivesDataInfo(callListPushDTO);
        //将json放入MQ
        String mqResult = unicomCallListPushService.setMessageQueue(customerReceivesDataInfo);
        //调用话单推送服务
        String pushResult = unicomCallListPushService.pushCommCdrPushService(customerReceivesDataInfo);
        log.info("队列调用执行：{}，comm服务调用执行：{};接收到callID：{},主叫号码：{}--话单推送结束", mqResult, pushResult, callListPushDTO.getCallId(), callListPushDTO.getPhoneNumberA());
    }

}
