package com.cqt.cdr.cloudccsfaftersales.controller;

import com.cqt.cdr.cloudccsfaftersales.entity.dto.SFStatusReq;
import com.cqt.cdr.cloudccsfaftersales.service.CtiWorkeventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


//@RestController注解相当于@ResponseBody ＋ @Controller
@RestController
@CrossOrigin
@Api(value = "售后获取坐席状态接口", tags = {"售后获取坐席状态接口"})
public class SFController {

    private static final Logger logger = LoggerFactory.getLogger(SFController.class);
    @Resource
    private CtiWorkeventService cws;


    @RequestMapping(value = "/xfsfstatu", method = RequestMethod.POST, produces = "application/json")
    @ApiOperation(value= "售后获取坐席状态接口")
    public Map<String, String> sfdigitcount(@RequestBody SFStatusReq sfStatusReq) {
        String LOG_TAG = UUID.randomUUID().toString() + " | 售后获取坐席状态接口 | ";
        logger.info(LOG_TAG + "sfStatusReq:" + sfStatusReq);
        CompletableFuture<Map<String, String>> check = cws.check(sfStatusReq);
        Map<String, String> map = null;
        try {
            map = check.get();
        } catch (InterruptedException e) {
            logger.error("坐席状态检测异常", e);
            Thread.currentThread().interrupt();
        }  catch (Exception e) {
            logger.error("坐席状态检测异常", e);
        }
        return map;
    }
}
