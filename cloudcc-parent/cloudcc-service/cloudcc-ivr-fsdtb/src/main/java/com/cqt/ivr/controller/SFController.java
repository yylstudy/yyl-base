package com.cqt.ivr.controller;

import com.alibaba.fastjson.JSON;
import com.cqt.ivr.config.nacos.DynamicConfig;
import com.cqt.ivr.entity.dto.SFWOReq;
import com.cqt.ivr.entity.vo.SFWORes;
import com.cqt.ivr.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.UUID;


//@RestController注解相当于@ResponseBody ＋ @Controller
@RestController
@CrossOrigin
@Api(value = "顺丰ivr接口", tags = {"顺丰ivr接口"})
public class SFController {

    private static final Logger logger = LoggerFactory.getLogger(SFController.class);
    @Resource
    private DynamicConfig dynamicConfig;


    @RequestMapping(value = "/getTtsUrlByWO", method = RequestMethod.POST)
    @ApiOperation(value= "通过开头按键和工单获取tts信息")
    public SFWORes getTtsUrlByWO(SFWOReq req) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 通过开头按键和工单获取tts信息 | ";
        logger.info(LOG_TAG + "req:" + req);
        SFWORes res = new SFWORes();
        res.setCode("0");
        res.setResult("yes");
        String ttsUrl = dynamicConfig.getTtsurl();
        String orderCode = req.getNumberbegin() + req.getInquiryno();
        String text = "您的工号为：" + req.getAgentid() + ",结算单号为：" + orderCode;
        String speed = req.getSpeed();
        if (StringUtil.isEmpty(speed)){
            speed = "3";
        }
        String url = ttsUrl + "?text=" + URLEncoder.encode(text) + "&speed=" + speed;
        res.setTtsUrl(url);
        logger.info(LOG_TAG + "返回信息 | " + JSON.toJSONString(res));
        return res;
    }
}
