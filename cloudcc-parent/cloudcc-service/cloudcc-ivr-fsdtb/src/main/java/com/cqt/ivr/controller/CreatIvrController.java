package com.cqt.ivr.controller;

import com.alibaba.fastjson.JSON;
import com.cqt.ivr.service.IIvrFsdtbService;
import com.cqt.ivr.utils.UT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @author xinson
 * date 2023-07-13 10:00:00
 */
@RestController
public class CreatIvrController {

    private static final Logger logger = LoggerFactory.getLogger(CreatIvrController.class);
    @Resource
    public IIvrFsdtbService iIvrFsdtbService;

    @RequestMapping(value = "/FswSI_Dtb/httppbxapi", method = RequestMethod.POST)
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String LOG_TAG = UUID.randomUUID().toString() + " | httppbxapi | ";
        response.setContentType("text/JavaScript; charset=utf-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        logger.info(LOG_TAG + "requesstInfo:" + JSON.toJSONString(request.getParameterMap()));
        String returnStr = iIvrFsdtbService.tojson(request.getParameterMap(), null, response, LOG_TAG);
        UT.printstr(response.getWriter(), returnStr, LOG_TAG);
    }
}
