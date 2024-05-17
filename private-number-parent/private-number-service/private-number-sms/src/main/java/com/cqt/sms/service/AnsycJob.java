package com.cqt.sms.service;

import com.cqt.sms.util.StringUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

@Component
public class AnsycJob {
    @Async // 异步标签
    public void testAsyn(HttpServletResponse response, String json) {
        //异步向客户返回消息
        StringUtil.responToClient(response, json);
    }
}
