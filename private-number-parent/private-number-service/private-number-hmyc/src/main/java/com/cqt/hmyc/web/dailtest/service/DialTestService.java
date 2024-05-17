package com.cqt.hmyc.web.dailtest.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.dailtest.dto.DialTestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author linshiqiang
 * @date 2021/10/11 10:22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DialTestService {

    private final HideProperties hideProperties;


    public Result start(DialTestDTO dialTestDTO) {
        ArrayList<String> argsList = new ArrayList<>();
        argsList.add(dialTestDTO.getTs());
        argsList.add(dialTestDTO.getSign());
        argsList.add(dialTestDTO.getRequestId());
        argsList.add(dialTestDTO.getBindId());
        argsList.add(dialTestDTO.getPhoneCalling());
        argsList.add(dialTestDTO.getPhoneCalled());
        argsList.add(dialTestDTO.getPhoneX());
        argsList.add(dialTestDTO.getAreaCode());
        argsList.add(hideProperties.getDialTestVoice());
        argsList.add(hideProperties.getVccId());
        String args = String.join(" ", argsList);
        // 发起语音通知
        String cmd = String.format(hideProperties.getDialTestLuaCmd(), args);
        String dialTestUrl = hideProperties.getDialTestUrl();
        try (HttpResponse httpResponse = HttpRequest.post(dialTestUrl)
                .body(cmd)
                .contentType(ContentType.TEXT_PLAIN.getValue())
                .timeout(10000)
                .execute()) {
            String execute = httpResponse.body();
            log.info("拨测url: {}, 拨测命令: {}, 结果: {}", dialTestUrl, cmd, execute);
            if (StrUtil.isBlank(execute) || !execute.contains("OK")) {
                return Result.fail(500, "拨测失败");
            }
        }

        return Result.ok();
    }

    public static void main(String[] args) {
        String url = "http://172.16.246.40:18802/private-agent/command/execute";
        String data = "fs_cli -x 'bgapi lua tencent_meituan_axb_hmbc_1.0.0.lua 22 22 11111111111111 cqt-000001111111111 13107618845 13107618845 18652984415 025 axb_playBackName.wav 3155'";
        HttpResponse execute = HttpRequest.post(url)
                .body(data)
                .contentType(ContentType.TEXT_PLAIN.getValue())
                .timeout(10000)
                .execute();

        System.out.println(execute.body());
    }
}
