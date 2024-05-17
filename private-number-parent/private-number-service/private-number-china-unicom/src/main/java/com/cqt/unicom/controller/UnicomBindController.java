package com.cqt.unicom.controller;

import com.alibaba.fastjson.JSONObject;
import com.cqt.common.enums.PushTypeEnum;
import com.cqt.model.push.entity.PrivateFailMessage;
import com.cqt.unicom.PrivateNumberChinaUnicomApplication;
import com.cqt.unicom.dto.QueryAxeBindDTO;
import com.cqt.unicom.dto.QueryBindDTO;
import com.cqt.unicom.dto.UnicomCdrDTO;
import com.cqt.unicom.dto.UnicomEventDTO;
import com.cqt.unicom.service.UnicomBindInfoService;
import com.cqt.unicom.vo.ResultErrVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;

/**
 * @author huweizhong
 * date  2023/10/24 15:26
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Api(tags = "联通绑定关系查询")
@RequestMapping("/unicom")
public class UnicomBindController {

    private final UnicomBindInfoService bindInfoService;

    @ApiOperation("AXB绑定关系查询")
    @PostMapping("/free/v2.0/voice_inactive")
    public Object queryBindInfoAxb(@RequestBody QueryBindDTO queryBindDTO)  {
        return bindInfoService.queryAxbBindInfo(queryBindDTO);
    }


    @ApiOperation("AXE绑定关系第二次查询")
    @PostMapping("/free/v2.0/dtmf_2nd_inactive")
    public Object queryAxeSecond(@RequestBody QueryAxeBindDTO queryBindDTO)  {
        return bindInfoService.queryAxeSecond(queryBindDTO);
    }

    @ApiOperation("话单接收")
    @PostMapping("/cdrReceiver/free/v2.0/cdr_pull")
    public ResultErrVO cdrReceiver(@RequestBody UnicomCdrDTO cdrDTO)  {
        log.info("联通原始话单：" + JSONObject.toJSONString(cdrDTO));
        bindInfoService.unicomBill(cdrDTO);
        return ResultErrVO.ok();
    }

    @ApiOperation("加密话单接收")
    @PostMapping("/encryptReceiver/free/v2.0/cdr_pull")
    public ResultErrVO encryptReceiver(@RequestBody String reqMsg)  {
        bindInfoService.encrypt(reqMsg);
        return ResultErrVO.ok();
    }

    @ApiOperation("通话状态接收")
    @PostMapping("/cdrReceiver/free/v2.0/pull")
    public ResultErrVO eventReceiver(@RequestBody UnicomEventDTO eventDTO)  {
        PrivateFailMessage privateFailMessage = new PrivateFailMessage();
        privateFailMessage.setIp(PrivateNumberChinaUnicomApplication.ip);
        privateFailMessage.setBody(JSONObject.toJSONString(eventDTO));
        privateFailMessage.setNum(0);
        privateFailMessage.setId(eventDTO.getExtention().getId());
        bindInfoService.pushEvent(privateFailMessage);
        return ResultErrVO.ok();
    }


}
