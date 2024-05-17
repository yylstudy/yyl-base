package com.cqt.client.controller;

import com.alibaba.fastjson.JSONObject;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.rpc.client.ClientServerRemoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.cqt.client.websocket.WebSocketHandler.*;

/**
 * @author 86180
 */
@RestController
@Slf4j
@RequestMapping("/netty")
@RequiredArgsConstructor
public class MessageSendController {

    @Resource
    private ClientServerRemoteService clientServerRemoteService;

    @GetMapping("/channelCount")
    public int channelCount() {
        return channelMap.size();
    }

    @GetMapping("/clientCount")
    public int clientCount() {
        return clientMap.size();
    }

    @GetMapping("/clientAgent")
    public String channelList() {
        return clientMap.values().toString();
    }

    @GetMapping("/futureMap")
    public String futureMapInfo() {
        return futureMap.keySet().toString();
    }

    @PostMapping("sendMsgToClient")
    public ClientResponseBaseVO sendToClient(@RequestBody JSONObject jsonObject) {
        return clientServerRemoteService.request(JSONObject.toJSONString(jsonObject));
    }
}
