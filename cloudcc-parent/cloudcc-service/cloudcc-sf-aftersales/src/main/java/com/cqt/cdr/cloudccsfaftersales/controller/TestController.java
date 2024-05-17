package com.cqt.cdr.cloudccsfaftersales.controller;


import com.alibaba.fastjson.JSONObject;
import com.cqt.cdr.cloudccsfaftersales.entity.CallStateDetails;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.feign.freeswitch.FreeswitchApiFeignClient;
import com.cqt.model.cdr.dto.RemoteCdrDTO;
import com.cqt.model.cdr.dto.RemoteQualityCdrDTO;
import com.cqt.model.cdr.entity.PushMisscallDataEntity;
import com.cqt.model.cdr.entity.RemoteQualityCdr;
import com.cqt.model.cdr.vo.RemoteCdrVO;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.freeswitch.dto.api.PlayRecordDTO;
import com.cqt.model.freeswitch.vo.PlayRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.util.UUID;

@RestController
@RequestMapping
@Slf4j
public class TestController {
    private static final Logger BUSYCDRLOGGER = LoggerFactory.getLogger("busyCdrLogger");
    private static final Logger QUALITYCDRLOGGER = LoggerFactory.getLogger("qualityCdrLogger");
    private static final Logger AFTERSALECDRLOGGER = LoggerFactory.getLogger("aftersaleCdrLogger");

    @PostMapping("busy")
    public RemoteCdrVO busy(@RequestBody PushMisscallDataEntity remoteCdrDTO) {
        BUSYCDRLOGGER.info(JSONObject.toJSONString(remoteCdrDTO));
        RemoteCdrVO remoteCdrVO = new RemoteCdrVO();
        remoteCdrVO.setCode("200");
        return remoteCdrVO;
    }

    @PostMapping("quality")
    public RemoteCdrVO quality(@RequestBody RemoteQualityCdrDTO remoteCdrDTO) {
        QUALITYCDRLOGGER.info(JSONObject.toJSONString(remoteCdrDTO));
        RemoteCdrVO remoteCdrVO = new RemoteCdrVO();
        remoteCdrVO.setCode("200");
        return remoteCdrVO;
    }

    @PostMapping("aftersale")
    public RemoteCdrVO aftersale(@RequestBody CallStateDetails remoteCdrDTO) {
        AFTERSALECDRLOGGER.info(JSONObject.toJSONString(remoteCdrDTO));
        RemoteCdrVO remoteCdrVO = new RemoteCdrVO();
        remoteCdrVO.setCode("200");
        return remoteCdrVO;
    }




}
