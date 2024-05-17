package com.cqt.sms.dao.mapper;


import com.cqt.model.numpool.entity.PrivateNumberPool;
import com.cqt.model.numpool.entity.PrivateVccInfo;
import com.cqt.sms.model.dto.SmsStatePush;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


@Mapper
public interface SmsMapper {

    List<Map<String, String>> findAllHcode();

    List<Map<String, String>> findAllGsmCenter();

    List<String> findAllBlackList();

    List<String> findAllWhiteList();

    List<String> findAllMultipleBlackList();

    /**
     * 查询指定企业的黑名单词汇
     * @param vccId 企业vccId
     * @return List 返回黑名单词汇
     * */
    @Select(" SELECT black_words FROM sms_black_words WHERE vcc_id = #{vccId} ")
    List<String> findBlackWordsByVccId(String vccId);

    /**
     * 查询指定企业的白名单词汇
     * @param vccId 企业vccId
     * @return List 返回白名单词汇
     * */
    @Select(" SELECT white_words FROM sms_white_words WHERE vcc_id = #{vccId} ")
    List<String> findWhiteWordsByVccId(String vccId);

    void saveSmsFailedStatePush(SmsStatePush smsStatePush);

    /**
     * 查询指定X号码的短信发送数量配置
     * @param number X号码
     * @return PrivateNumberPool 号码池信息
     * */
    @Select(" SELECT * FROM private_number_pool WHERE number = #{number} ")
    PrivateNumberPool findNumberPoolInfoByNumber(String number);

    /**
     * 查询指定企业vccId的企业配置信息
     * @param vccId 企业vccId
     * @return PrivateVccInfo 企业信息
     * */
    @Select(" SELECT * FROM private_vcc_info WHERE vcc_id = #{vccId} ")
    PrivateVccInfo findVccInfoByVccId(String vccId);

}
