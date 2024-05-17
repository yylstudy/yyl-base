package com.cqt.sms.service;

import com.cqt.model.numpool.entity.PrivateNumberPool;
import com.cqt.model.numpool.entity.PrivateVccInfo;
import com.cqt.sms.model.entity.SmsRequest;

import java.util.List;


/**
 * @Description: 短信推送
 * @author: scott
 * @date: 2022年03月25日 14:47
 */
public interface SmsPushService {

    /**
     * 通用短信接收
     *
     * @param smsRequest 短信接收参数
     */
    void smsReceive(SmsRequest smsRequest);

    /**
     * @param pushUrl      短信推送地址
     * @param telX         telX
     * @param telA         telA
     * @param smsContent   短信内容
     * @param requestTime  请求时间
     * @param msgId        短信流水号
     * @param vccId        企业vccId
     * @param currentCount 当前重试次数
     */
    void smsContentPush(String pushUrl, String telX, String telA, String smsContent, String requestTime, String msgId, String vccId, Integer currentCount);

    /**
     * 查询通用短信黑名单词汇
     *
     * @param vccId 企业vccId
     * @return List 黑名单词汇
     */
    List<String> queryBlackWordsByVccId(String vccId);

    /**
     * 查询通用短信白名单词汇
     *
     * @param vccId 企业vccId
     * @return List 白名单词汇
     */
    List<String> queryWhiteWordsByVccId(String vccId);

    /**
     * 查询通用短信敏感词
     * @return List 短信敏感词
     * */
    List<String> querySensitiveWords();

    /**
     * 查询号码池信息
     *
     * @param xNumber x号码
     * @return PrivateNumberPool 号码池信息
     */
    PrivateNumberPool queryNumberPoolByNumber(String xNumber);


    public PrivateNumberPool findNumberPoolByNumber(String xNumber);

    /**
     * 查询企业配置信息
     *
     * @param vccId 企业vccId
     * @return PrivateVccInfo 企业配置信息
     */
    PrivateVccInfo queryVccInfoByVccId(String vccId);

    /**
     * 短信白名单词汇匹配
     * 判断给定的短信文本是否命中白名单关键字
     * @param vccId 企业vccId
     * @param smsContent 短信文本
     * @return boolean 短信白名单匹配结果
     */
    boolean whiteWordsFilter(String vccId, String smsContent);
    /**
     * 短信黑名单词汇匹配
     * 判断给定的短信文本是否命中黑名单关键字
     * @param vccId 企业vccId
     * @param smsContent 短信文本
     * @return boolean 短信黑名单匹配结果, 命中则为true
     * */
    boolean blackWordsFilter(String vccId, String smsContent);

    /**
     * 匹配给定的文本内容是否命中敏感词汇
     * @param content 文本内容
     * @return boolean 匹配结果
     * */
    boolean sensitiveWordsFilter(String content);

    boolean smsCount(SmsRequest smsRequest);
}
