package com.cqt.unicom.service;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.ControlOperateEnum;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.common.ResultVO;
import com.cqt.model.unicom.vo.NumberBindingQueryVO;
import com.cqt.unicom.dto.QueryBindDTO;
import com.cqt.unicom.properties.QueryBindProperties;
import com.cqt.unicom.vo.ResultErrVO;
import com.cqt.unicom.vo.UnicomAxbBindInfoVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: private-number-parent
 * @description: 调用通用号码隐藏查询绑定关系
 * @author: yy
 * @create: 2023-11-06 10:26
 **/

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivateNumberBindInfoService {

    private final QueryBindProperties queryBindProperties;

    private final ObjectMapper objectMapper;


    /**
     * 查询号码绑定关系服务实现
     * @param queryBindDTO 联通集团总部(江苏)号码绑定查询入参
     * @return NumberBindingQueryVO
     */
    public Object getNumberBindingQuery(QueryBindDTO queryBindDTO)  {
        String callId = queryBindDTO.getCallId();
        String phoneNumberA = queryBindDTO.getCaller ();
        String phoneNumberX = queryBindDTO.getCallee ();
        String bindInfoApiQueryString = JSON.toJSONString(queryBindDTO.buildBindInfoApiQuery());
        String bindInfoData;
        try {
            bindInfoData = HttpUtil.post(queryBindProperties.getBindnumerUrl(), bindInfoApiQueryString, 5000);
        } catch (Exception e) {
            // 接口调用失败
            log.error("callId:{},调用内部查询绑定关系接口: {}, 异常: ", callId,queryBindProperties.getBindnumerUrl(), e);
            return ResultErrVO.fail("内部异常");
        }
        ResultVO<BindInfoApiVO> bindInfoApiVOResultVO = null;
        try {
            bindInfoApiVOResultVO = objectMapper.readValue(bindInfoData, new TypeReference<ResultVO<BindInfoApiVO>> () {
            });
        } catch (JsonProcessingException e) {
            log.error("callId: {}解析返回绑定关系实体类异常",e);
        }
        log.info("callId: {}, 内部接口查询绑定关系结果返回: {}", callId, JSON.toJSONString(bindInfoApiVOResultVO));
        if(bindInfoApiVOResultVO==null){
            return ResultErrVO.fail("查不到绑定关系");
        }
        BindInfoApiVO bindInfoApiVO = bindInfoApiVOResultVO.getData();


        String controlOperate = bindInfoApiVO.getControlOperate();
        // 通话拦截
        if (ControlOperateEnum.REJECT.name().equals(controlOperate)) {
            NumberBindingQueryVO notBind = NumberBindingQueryVO.notBind(phoneNumberA, phoneNumberX, FileNameUtil.mainName(bindInfoApiVO.getCallerIvr()));
            log.info("callId: {}, 无绑定关系结果返回：{}", callId, JSON.toJSONString(notBind));
            return ResultErrVO.fail("查不到绑定关系");
        }
        UnicomAxbBindInfoVO unicomAxbBindInfoVO = UnicomAxbBindInfoVO.buildUnicomBindInfoVO(bindInfoApiVO, queryBindDTO.getCallee());
        Map<String,String> map = new HashMap<> (16);
        map.put ("userData",bindInfoApiVO.getUserData ());
        unicomAxbBindInfoVO.getData ().setuId (JSON.toJSONString(map));
        log.info("callId: {}, 查询绑定关系结果返回：{}", callId, JSON.toJSONString(unicomAxbBindInfoVO));
        return unicomAxbBindInfoVO;
    }
}
