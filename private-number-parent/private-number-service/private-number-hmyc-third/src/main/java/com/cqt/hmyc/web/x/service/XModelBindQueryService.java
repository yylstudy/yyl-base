package com.cqt.hmyc.web.x.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cqt.common.enums.ControlOperateEnum;
import com.cqt.hmyc.config.properties.BindProperties;
import com.cqt.hmyc.enums.BehaviorType;
import com.cqt.hmyc.enums.OpTypeEnum;
import com.cqt.hmyc.web.bind.cache.LocalCacheService;
import com.cqt.hmyc.web.x.model.XModelQueryDTO;
import com.cqt.hmyc.web.x.model.XModelQueryVO;
import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.common.ResultVO;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author Xienx
 * @date 2023-06-08 09:22:9:22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XModelBindQueryService {

    private static final String RECORD_TYPE_CALL = "0";
    private final BindProperties bindProperties;

    private final RedissonUtil redissonUtil;

    public XModelQueryVO query(XModelQueryDTO queryDTO) {
        redissonUtil.increment("queryBindTimes_"+queryDTO.getCallId(), Duration.ofMinutes(5L));
        log.info("江苏移动查询绑定关系入参: {}", JSON.toJSONString(queryDTO));
        XModelQueryVO queryVO = new XModelQueryVO();
        queryVO.setCode("0000");
        queryVO.setOpType(OpTypeEnum.CALL_HANG_UP.getCode());

        BindInfoApiQuery bindInfoApiQuery = new BindInfoApiQuery();
        bindInfoApiQuery.setCallId(queryDTO.getCallId());
        bindInfoApiQuery.setCaller(queryDTO.getCallNo());
        bindInfoApiQuery.setCalled(queryDTO.getSecretNo());
        bindInfoApiQuery.setDigitInfo(queryDTO.getExtensionNo());
        // 设置行为类型
        if (RECORD_TYPE_CALL.equals(queryDTO.getRecordType())) {
            bindInfoApiQuery.setBehaviorType(BehaviorType.CALL.name());
        } else {
            bindInfoApiQuery.setBehaviorType(BehaviorType.SMS.name());
        }
        String url = bindProperties.getUrl();
        log.info("callId: {}, 内部接口查询参数:{} ", queryDTO.getCallId(), JSON.toJSONString(bindInfoApiQuery));
        try {
            //设置超时
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setReadTimeout(bindProperties.getTimeout());
            requestFactory.setConnectTimeout(bindProperties.getTimeout());
            RestTemplate restTemplate = new RestTemplate(requestFactory);
            ResponseEntity<String> response = restTemplate
                    .postForEntity(url, bindInfoApiQuery, String.class);
            log.info("callId: {}, 内部接口查询绑定关系结果返回: {}", queryDTO.getCallId(), response);
            // 绑定查询结果解析
            ResultVO<BindInfoApiVO> resultVO = JSON.parseObject(response.getBody(), new TypeReference<ResultVO<BindInfoApiVO>>() {
            });
            // 如果没有响应内容或者响应失败则不继续处理
            if (resultVO == null) {
                log.info("接收到callID：{},主叫号码：{}--查询绑定关系异常", queryDTO.getCallId(), queryDTO.getCallNo());
                return queryVO;
            }

            BindInfoApiVO bindInfoVO = resultVO.getData();

            queryVO.setMessage(resultVO.getMessage());
            if (StrUtil.isNotEmpty(bindInfoVO.getVccId())) {
                String prefix = bindInfoVO.getVccId() + StrUtil.SLASH;
                queryVO.setCallNoPlayCode(LocalCacheService.HDH_AUDIO_CODE_CACHE.get(prefix + bindInfoVO.getCallerIvr()));
                // 江苏移动需要的是放音编码, 这一块需要进行映射处理
                queryVO.setCalledPlayCode(LocalCacheService.HDH_AUDIO_CODE_CACHE.get(prefix + bindInfoVO.getCalledIvr()));
                if (StrUtil.isNotEmpty(bindInfoVO.getCallerIvrBefore())) {
                    queryVO.setCallNoPlayCode(LocalCacheService.HDH_AUDIO_CODE_CACHE.get(prefix + bindInfoVO.getCallerIvrBefore()));
                }
            }

            // 无绑定关系
            if (ControlOperateEnum.REJECT.name().equals(bindInfoVO.getControlOperate())) {
                log.warn("接收到callID：{},主叫号码：{} --无绑定关系", queryDTO.getCallId(), queryDTO.getCallNo());
                return queryVO;
            }

            // 转成江苏移动需要的内容
            String uuid = StrUtil.uuid().replace("-","");
            redissonUtil.setString("bindId_"+uuid,bindInfoVO.getBindId(),6, TimeUnit.HOURS);
            queryVO.setBindId(uuid);
            // 设置被叫以被叫显号 需要加地区码前缀
            queryVO.setCalledNo(StrUtil.addPrefixIfNot(bindInfoVO.getCalledNum(), "86"));
            queryVO.setCalledDisplayNo(StrUtil.addPrefixIfNot(bindInfoVO.getDisplayNum(), "86"));

            // 客户透传参数
            queryVO.setData(bindInfoVO.getUserData());
            // 设置是否录音
            queryVO.setNeedRecord(bindInfoVO.getEnableRecord());
            // 设置操作指令
            queryVO.setOpType(getOpType(bindInfoVO.getControlOperate()));
            log.info("江苏移动查询绑定结果返回: {}", JSON.toJSONString(queryVO));
        } catch (Exception e) {
            log.error("调用本平台查询绑定关系接口: {} 出现异常: ", url, e);
            queryVO.setMessage("内部异常");
            queryVO.setCode("9999");
        }
        if ("2".equals(queryVO.getOpType())){
            int integer = Integer.parseInt(redissonUtil.getString("queryBindTimes_" + queryDTO.getCallId()));
            if (integer>3){
                queryVO.setOpType("0");
            }
        }

        return queryVO;
    }



    /**
     * 转换成江苏移动需要的OpType
     */
    private String getOpType(String controlOperate) {
        if (ControlOperateEnum.REJECT.name().equals(controlOperate)) {
            return OpTypeEnum.CALL_HANG_UP.getCode();
        }

        if (ControlOperateEnum.IVR.name().equals(controlOperate)) {
            return OpTypeEnum.CALL_IVR.getCode();
        }

        return OpTypeEnum.CALL_CONTINUE.getCode();
    }
}
