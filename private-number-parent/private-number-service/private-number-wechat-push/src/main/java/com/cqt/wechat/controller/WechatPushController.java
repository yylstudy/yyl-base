package com.cqt.wechat.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.model.common.Result;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.cqt.model.sms.dto.CommonSmsBillPushDTO;
import com.cqt.wechat.service.PushService;
import com.cqt.wechat.utils.PushUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;

/**
 * @author hlx
 * @date 2021-09-14
 */
@RestController
@Slf4j
public class WechatPushController {

    @Autowired
    private PushService pushService;


    /**
     * 话单接收接口
     *
     * @param acrJson 通用话单实体
     * @return 结果
     */
    @PostMapping("/cdr/receiver")
    public Result billReceiver(@RequestBody String acrJson) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(acrJson);
        PrivateBillInfo privateBillInfo = jsonObject.toJavaObject(PrivateBillInfo.class);
        log.info("接收话单 => {},", acrJson);

        String vccId = privateBillInfo.getAppKey();
        if (privateBillInfo.getCallResult() == 99) {
            log.info("无绑定关系，不推送|RecordId=>{}", privateBillInfo.getRecordId());
            return Result.ok();
        }
        PrivateFailMessage failMessage = PushUtil.buildBillMessage(privateBillInfo);
        failMessage.setType(PushTypeEnum.BILL.name());
        failMessage.setVccid(vccId);
        pushService.pushBillStart(failMessage);
        return Result.ok();
    }

    /**
     * 通话状态接收接口
     *
     * @param acrJson 通用话单实体
     * @return 结果
     */
    @PostMapping("/status/receiver")
    public Result statusReceiver(@RequestBody String acrJson) throws ParseException, JsonProcessingException {

        JSONObject jsonObject = JSONObject.parseObject(acrJson);
        PrivateStatusInfo privateStatusInfo = jsonObject.toJavaObject(PrivateStatusInfo.class);
        log.info("接收{}通话状态事件 =>{}", privateStatusInfo.getEvent(), acrJson);

        PrivateFailMessage failMessage = PushUtil.buildStatusMessage(privateStatusInfo);
        failMessage.setVccid(privateStatusInfo.getAppKey());
        failMessage.setType(PushTypeEnum.STATUS.name());
        pushService.pushStatusStart(failMessage);
        return Result.ok();
    }

    /**
     * 短信话单接收接口
     *
     * @param commonSmsBillPushDTO 短信话单实体
     * @return 结果
     */
    @PostMapping("/msg/receiver")
    public Result thirdBillReceiver(@RequestBody CommonSmsBillPushDTO commonSmsBillPushDTO) throws ParseException, JsonProcessingException {

        log.info("接收短信话单=>{}", JSONObject.toJSONString(commonSmsBillPushDTO));
        PrivateFailMessage failMessage = PushUtil.buildBillMessage(commonSmsBillPushDTO);
        failMessage.setType(PushTypeEnum.SMS.name());
        pushService.pushMsg(failMessage);
        return Result.ok();
    }

    @PostMapping("test")
    public Result test() {
        return Result.ok();
    }

    @PostMapping("/number-pool/notify")
    public Result notify(@RequestBody List<String> areaCodes) {
        pushService.notifyWechat(areaCodes);
        return Result.ok();
    }

    @PostMapping( "/file")
    public Result file(@RequestParam("file") MultipartFile file) throws IOException, ParseException {

        String xml = IoUtil.read(file.getInputStream(), StandardCharsets.UTF_8);
        String[] split = xml.split(StrUtil.CRLF);
        for (String s : split) {
            JSONObject jsonObject = JSONObject.parseObject(s);
            PrivateBillInfo privateBillInfo = jsonObject.toJavaObject(PrivateBillInfo.class);
            String vccId = privateBillInfo.getAppKey();
            if (privateBillInfo.getCallResult() == 99) {
                log.info("无绑定关系，不推送|RecordId=>{}", privateBillInfo.getRecordId());
                return Result.ok();
            }
            PrivateFailMessage failMessage = PushUtil.buildBillMessage(privateBillInfo);
            failMessage.setType(PushTypeEnum.BILL.name());
            failMessage.setVccid(vccId);
            pushService.pushBillStart(failMessage);
        }
        return Result.ok();
    }
}
