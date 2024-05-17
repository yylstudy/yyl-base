package com.cqt.broadnet.web.x.service.impl;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.cqt.broadnet.common.model.x.dto.SmsCheckDTO;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.common.model.x.vo.SmsCheckVO;
import com.cqt.broadnet.web.axb.mapper.SmsMapper;
import com.cqt.broadnet.web.x.service.PrivateCorpBusinessInfoService;
import com.cqt.broadnet.web.x.service.SmsCheckService;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.common.ResultVO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date:  2023-04-26 14:58
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCheckServiceImpl implements SmsCheckService {

    private final PrivateCorpBusinessInfoService privateCorpBusinessInfoService;

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final ObjectMapper objectMapper;

    private final RedissonUtil redissonUtil;

    @Override
    public SmsCheckVO check(String sms1CheckDTO) throws JsonProcessingException {
        log.info("sms check => "+ sms1CheckDTO);
        sms1CheckDTO= sms1CheckDTO.replace("\r\t","").replace("\r\n","").replace("\n","");
        SmsCheckDTO smsCheckDTO = objectMapper.readValue(sms1CheckDTO, SmsCheckDTO.class);
        String msgIdentifier = smsCheckDTO.getMsgIdentifier();
        if (log.isInfoEnabled()) {
            log.info("msgIdentifier: {}, 短信校验参数： {}", msgIdentifier, objectMapper.writeValueAsString(smsCheckDTO));
        }
        redissonUtil.setString("sms_content_"+msgIdentifier,smsCheckDTO.getSmsContent(),1L, TimeUnit.HOURS);
        redissonUtil.setString("sms_num_"+msgIdentifier,String.valueOf(smsCheckDTO.getSmNums()),1L, TimeUnit.HOURS);
        // 格式化号码为标准号码
        smsCheckDTO.transfer();

        // 非X模式, AXB
        if (StrUtil.isNotEmpty(smsCheckDTO.getCalled())) {
            String virtualCalled = smsCheckDTO.getVirtualCalled();
            PrivateCorpBusinessInfoDTO businessInfoDTO = privateCorpBusinessInfoService.getPrivateCorpBusinessInfoDTO(virtualCalled);
            boolean isInBack = isInBlack(smsCheckDTO, businessInfoDTO.getVccId());
            if (isInBack) {
                return SmsCheckVO.reject(msgIdentifier);
            }
            return SmsCheckVO.ok(smsCheckDTO);
        }

        // 查询绑定的关系
        BindInfoApiQuery apiQuery = getBindInfoApiQuery(smsCheckDTO);
        try (HttpResponse httpResponse = HttpRequest
                .post(privateNumberBindProperties.getGetBindInfoUrl())
                .body(objectMapper.writeValueAsString(apiQuery))
                .timeout(privateNumberBindProperties.getQueryTimeout())
                .execute()) {
            String body = httpResponse.body();
            log.info("短信-通用查询绑定关系结果: {}", body);

            if (!httpResponse.isOk()) {
                return SmsCheckVO.reject(msgIdentifier);
            }
            ResultVO<BindInfoApiVO> bindInfoResultVO = objectMapper.readValue(body, new TypeReference<ResultVO<BindInfoApiVO>>() {
            });
            if (!bindInfoResultVO.success()) {
                return SmsCheckVO.reject(msgIdentifier);
            }
            BindInfoApiVO bindInfoApiVO = bindInfoResultVO.getData();
            // type=0 允许发送短信。
            if (0 == bindInfoApiVO.getType()) {
                // 短信内容黑名单判断
                boolean isInBack = isInBlack(smsCheckDTO, bindInfoApiVO.getVccId());
                if (isInBack) {
                    return SmsCheckVO.reject(msgIdentifier);
                }
                return SmsCheckVO.ok(smsCheckDTO, bindInfoApiVO.getCalledNum());
            }
        }
        return SmsCheckVO.reject(smsCheckDTO);
    }



    /**
     * 是否命中黑名单
     * true 命中，拒绝
     * false 转发
     */
    private boolean isInBlack(SmsCheckDTO smsCheckDTO, String vccId) throws JsonProcessingException {
        if (ObjectUtil.isEmpty(smsCheckDTO.getSmsContent())) {
            return true;
        }
        String smsContent = String.valueOf(smsCheckDTO.getSmsContent());
//        String smsContent = HexUtil.decodeHexStr(smsCheckDTO.getSmsContent());

        // 全局敏感词
        String sensitiveWordsKey = redissonUtil.getString(PrivateCacheConstant.COMMON_SENSITIVE_WORDS_KEY);
        String sensitiveWordsJson = redissonUtil.getString(sensitiveWordsKey);
        if (StrUtil.isNotEmpty(sensitiveWordsJson)) {
            List<String> sensitiveList = objectMapper.readValue(sensitiveWordsJson, new TypeReference<List<String>>() {
            });
            for (String word : sensitiveList) {
                if (smsContent.contains(word)) {
                    log.info("短信文本内容: {}, 命中敏感词汇: {}", smsContent, word);
                    return true;
                }
            }
        }

        // 企业黑名单
        String corpBlackWordsKey = String.format(PrivateCacheConstant.COMMON_BLACK_WORDS_KEY, vccId);
        String corpBlackWordsJson = redissonUtil.getString(corpBlackWordsKey);
        if (StrUtil.isNotEmpty(corpBlackWordsJson)) {
            List<String> blackWordList = objectMapper.readValue(corpBlackWordsJson, new TypeReference<List<String>>() {
            });
            return keyWordsFilter(smsContent, blackWordList);
        }

        // 企业白名单
        String corpWhiteWordsKey = String.format(PrivateCacheConstant.COMMON_WHITE_WORDS_KEY, vccId);
        String corpWhiteWordsJson = redissonUtil.getString(corpWhiteWordsKey);
        if (StrUtil.isNotEmpty(corpWhiteWordsJson)) {
            List<String> whiteWordList = objectMapper.readValue(corpWhiteWordsJson, new TypeReference<List<String>>() {
            });
            return keyWordsFilter(smsContent, whiteWordList);
        }
        return false;
    }

    /**
     * 匹配给定的文本内容是否在给定的关键字集c
     *
     * @param content  文本内容
     * @param keyWords 关键词集
     * @return boolean 匹配结果
     */
    public boolean keyWordsFilter(String content, List<String> keyWords) {
        //遍历循环匹配
        //同一个企业的多组关键字词汇为或关系, 一组的关键字词汇为且的关系
        for (String keyWord : keyWords) {
            boolean filterFlag = true;
            //多级关键字切分
            List<String> partWords = StrUtil.split(keyWord, "、");
            //匹配当前多级黑白名单词汇
            for (String partWord : partWords) {
                filterFlag &= content.contains(partWord);
            }
            //命中一组关键字词汇才算通过
            if (filterFlag) {
                log.info("短信文本内容: {}, 命中多级关键字词汇: {}", content, keyWord);
                return true;
            }
        }
        return false;
    }

    /**
     * 构造 查询绑定关系参数
     */
    private BindInfoApiQuery getBindInfoApiQuery(SmsCheckDTO smsCheckDTO) {
        BindInfoApiQuery apiQuery = new BindInfoApiQuery();
        apiQuery.setCaller(smsCheckDTO.getCalling());
        apiQuery.setCalled(smsCheckDTO.getVirtualCalled());
        apiQuery.setCallId(smsCheckDTO.getMsgIdentifier());
        apiQuery.setDigitInfo(smsCheckDTO.getOttSubId());
        return apiQuery;
    }
}
