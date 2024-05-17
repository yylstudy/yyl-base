package com.cqt.ivr.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author xinson
 * @since 2023-07-13
 */
public interface IIvrFsdtbService {
    //查找企业标识
    ArrayList<String> getAllCompanyCode();

    //创建ivr脚本
    String tojson(Map<String, String[]> reqpara, Map<String, Object> data, HttpServletResponse response, String LOG_TAG);
}


