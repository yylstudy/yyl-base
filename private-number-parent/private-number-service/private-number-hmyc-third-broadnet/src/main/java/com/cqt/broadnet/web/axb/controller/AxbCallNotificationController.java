package com.cqt.broadnet.web.axb.controller;

import com.alibaba.fastjson.JSONObject;
import com.cqt.broadnet.common.model.axb.dto.*;
import com.cqt.broadnet.web.axb.job.UploadSmsJob;
import com.cqt.broadnet.web.axb.service.AxbCallNotificationService;
import com.cqt.model.common.ResultVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author linshiqiang
 * date:  2023-05-26 11:46
 */
@Slf4j
@RestController
@RequestMapping("/axb/call-notification")
@RequiredArgsConstructor
public class AxbCallNotificationController {

    private final AxbCallNotificationService axbCallNotificationService;

    private final UploadSmsJob uploadSmsJob;

    @PostMapping("start")
    public ResultVO<Void> start(@RequestBody AxbCallStartDTO startDTO) throws JsonProcessingException {
        axbCallNotificationService.start(startDTO);
        return ResultVO.ok();
    }

    @PostMapping("nobind")
    public ResultVO<Void> noBind(@RequestBody AxbCallNoBindDTO noBindDTO) throws Exception {
        axbCallNotificationService.noBind(noBindDTO);
        return ResultVO.ok();
    }

    @PostMapping("finish")
    public ResultVO<Void> finish(@RequestBody AxbCallFinishDTO finishDTO) throws Exception {
        axbCallNotificationService.finish(finishDTO);
        return ResultVO.ok();
    }

    @PostMapping("record")
    public ResultVO<Void> record(@RequestBody AxbCallRecordDTO recordDTO) throws Exception {
        axbCallNotificationService.record(recordDTO);
        return ResultVO.ok();
    }

    @PostMapping("smsBack")
    public ResultVO<Void> smsBack(@RequestBody SmsBack smsBackDTO)  {
        log.info("短信回执接口参数："+ JSONObject.toJSONString(smsBackDTO));
        axbCallNotificationService.smsBack(smsBackDTO);
        return ResultVO.ok();
    }

    @PostMapping("test")
    public void test()  {
        axbCallNotificationService.test();

    }

    @PostMapping("test1")
    public void test1() throws InterruptedException {
        uploadSmsJob.smsJob();

    }
}
