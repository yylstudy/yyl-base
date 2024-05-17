package com.cqt.cdr.controller;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.feign.freeswitch.FreeswitchApiFeignClient;
import com.cqt.model.freeswitch.dto.api.PlayRecordDTO;
import com.cqt.model.cdr.dto.RemoteCdrDTO;
import com.cqt.model.freeswitch.vo.PlayRecordVO;
import com.cqt.model.cdr.vo.RemoteCdrVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageAccessor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@Slf4j
public class TestController {
    private static final Logger LOGGER = LoggerFactory.getLogger("reiveCdrLogger");

    @Resource
    FreeswitchApiFeignClient cdrClient;

    @Resource
    RocketMQTemplate rocketMQTemplate;



    @PostMapping("/test/outCdrReceive")
    public RemoteCdrVO outCdrReceive(@RequestBody RemoteCdrDTO remoteCdrDTO) {
        LOGGER.info(JSONObject.toJSONString(remoteCdrDTO));
        RemoteCdrVO remoteCdrVO = new RemoteCdrVO();
        remoteCdrVO.setCode("200");
        return remoteCdrVO;
    }

}
