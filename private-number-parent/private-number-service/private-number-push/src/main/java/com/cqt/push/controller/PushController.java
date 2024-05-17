package com.cqt.push.controller;

import com.alibaba.fastjson.JSONObject;
import com.cqt.common.enums.CallEventEnum;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.model.common.Result;
import com.cqt.model.push.dto.AybBindPushDTO;
import com.cqt.model.push.dto.UnbindPushDTO;
import com.cqt.model.push.entity.*;
import com.cqt.push.config.StartRunner;
import com.cqt.push.entity.Bill;
import com.cqt.push.enums.UrlTypeEnum;
import com.cqt.push.service.PushService;
import com.cqt.push.utils.MeituanUtil;
import com.cqt.push.utils.PushUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author hlx
 * @date 2021-09-14
 */
@RestController
@Slf4j
public class PushController {

    @Autowired
    private PushService pushService;

    @Autowired
    private StartRunner startRunner;

    @Autowired
    private MeituanUtil meituanUtil;


    /**
     * 话单接收接口
     *
     * @param acrJson 内部话单json字符串
     * @return 结果
     */
    @PostMapping("/bill/receiver")
    public CdrResult billReceiver(@RequestBody String acrJson) {
        JSONObject jsonObject = JSONObject.parseObject(acrJson);
        AcrRecordOrg acr = jsonObject.toJavaObject(AcrRecordOrg.class);
        log.info("接收话单，uuid=>{}", acr.getUuId());

        PrivateFailMessage failMessage = PushUtil.buildBillMessage(PushUtil.buildPrivateBill(acr));
        failMessage.setType(PushTypeEnum.BILL.name());
        failMessage.setVccid(acr.getVccId());
        pushService.pushBillStart(failMessage);

        // 挂机状态
        PrivateFailMessage statusFailMessage =
                PushUtil.buildStatusMessage(PushUtil.buildStatusByBill(acr));
        statusFailMessage.setType(PushTypeEnum.STATUS.name());
        statusFailMessage.setVccid(acr.getVccId());
        pushService.pushStatusStart(statusFailMessage);

        return CdrResult.ok(acr.getAcrCallId());
    }

    /**
     * third话单接收接口
     *
     * @param acrJson 内部话单json字符串
     * @return 结果
     */
    @PostMapping("/third/bill/receiver")
    public CdrResult thirdBillReceiver(@RequestBody String acrJson) {
        log.info("收到话单=>{}", acrJson);
        JSONObject jsonObject = JSONObject.parseObject(acrJson);
        PrivateBillInfo privateBillInfo = jsonObject.toJavaObject(PrivateBillInfo.class);
        log.info("接收话单，uuid=>{}", privateBillInfo.getRecordId());
        String userData=privateBillInfo.getUserData();
        JSONObject userDateParam=JSONObject.parseObject(userData);

        String vccId= String.valueOf(userDateParam.get("vccId")) ;
        if (StringUtils.isEmpty(vccId)){
            vccId = privateBillInfo.getAppKey();
        }
        PrivateFailMessage failMessage = PushUtil.buildBillMessage(privateBillInfo);
        failMessage.setType(PushTypeEnum.BILL.name());
        failMessage.setVccid(vccId);
        pushService.pushBillStart(failMessage);
        return CdrResult.ok();
    }



    @PostMapping("/accept")
    public CdrResult billAccept(@RequestBody String acrJson) {
        JSONObject jsonObject = JSONObject.parseObject(acrJson);
        AcrRecordOrg acr = jsonObject.toJavaObject(AcrRecordOrg.class);
        log.info("接收话单，uuid=>{}", acr.getUuId());
        Bill bill = meituanUtil.buildMeituanBill(acr);
        PrivateFailMessage failMessage = PushUtil.buildBillMessage(bill);
        failMessage.setType(PushTypeEnum.BILL.name());
        failMessage.setVccid(acr.getVccId());
        pushService.pushBillMeituan(failMessage);
        return CdrResult.ok(acr.getAcrCallId());
    }

    /**
     * 美团拨测话单推送
     */
    @PostMapping("/receiver-check")
    public Result checkReceiver(@RequestBody String acrJson) {
        JSONObject jsonObject = JSONObject.parseObject(acrJson);
        AcrRecordOrg acr = jsonObject.toJavaObject(AcrRecordOrg.class);
        log.info("接收拨测话单，uuid=>{}", acr.getUuId());
        pushService.checkPush(pushService.buildMtCheckBill(acr), acr.getVccId());
        return Result.ok();
    }

    /**
     * 通话状态接收接口
     *
     * @param privateStatusInfo 状态信息实体类
     * @return 结果
     */
    @GetMapping("/status/receiver")
    public Result statusReceiver( PrivateStatusInfo privateStatusInfo) {
        log.info("接收{}通话状态事件 =>{}", privateStatusInfo.getEvent(), privateStatusInfo);

        if (privateStatusInfo.getEvent().equals(CallEventEnum.hangup.name())) {
            return Result.ok();
        }
        PrivateFailMessage failMessage = PushUtil.buildStatusMessage(PushUtil.buildPrivateStatus(privateStatusInfo));
        failMessage.setVccid(privateStatusInfo.getVccId());
        failMessage.setType(PushTypeEnum.STATUS.name());

        pushService.pushStatusStart(failMessage);
        return Result.ok();
    }


    @GetMapping("/eventInfo")
    public Result getFreeswitch(@Validated PrivateStatusInfo privateStatusInfo) {
        log.info("接收{}通话状态事件 =>{}", privateStatusInfo.getEvent(), privateStatusInfo);

        if (privateStatusInfo.getEvent().equals(CallEventEnum.hangup.name())) {
            return Result.ok();
        }
        PrivateFailMessage failMessage = PushUtil.buildStatusMessage(PushUtil.buildPrivateStatus(privateStatusInfo));
        failMessage.setVccid(privateStatusInfo.getVccId());
        failMessage.setType(PushTypeEnum.STATUS.name());

        pushService.pushStatusStart(failMessage);
        return Result.ok();
    }

    /**
     * 解绑事件接收接口
     *
     * @param unbindPushDTO 解绑事件dto
     * @return 结果
     */
    @PostMapping("/unbind/receiver")
    public Result unbindReceiver(@RequestBody UnbindPushDTO unbindPushDTO) {
        log.info("接收解绑事件 =>" + unbindPushDTO.toString());

        PrivateFailMessage failMessage = PushUtil.buildUnbindMessage(unbindPushDTO);
        failMessage.setVccid(unbindPushDTO.getVccId());
        failMessage.setType(PushTypeEnum.UNBIND.name());

        pushService.pushUnbindStart(failMessage);
        return Result.ok();
    }

    /**
     * ayb绑定事件接收接口
     *
     * @param aybBindPushDTO ayb绑定事件dto
     * @return 结果
     */
    @PostMapping("/aybBind/receiver")
    public Result aybBindReceiver(@RequestBody AybBindPushDTO aybBindPushDTO) {
        log.info("接收ayb绑定事件 =>" + aybBindPushDTO.toString());

        PrivateFailMessage failMessage = PushUtil.buildUnbindMessage(aybBindPushDTO);
        failMessage.setVccid(aybBindPushDTO.getVccId());
        failMessage.setType(PushTypeEnum.AYB_BIND.name());

        pushService.pushAybBindStart(failMessage);
        return Result.ok();
    }

    /**
     * 重新企业数据
     *
     * @return ok
     */
    @GetMapping("/config/refresh")
    public Result refreshConfig() {
        // 刷新缓存
        startRunner.run();
        return Result.ok();
    }

    @PostMapping("/test")
    public void test(@RequestParam("file") MultipartFile file) throws IOException {
        pushService.test(file);

    }


}
