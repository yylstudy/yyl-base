package com.repush.dao.mapper;


import com.repush.dao.domain.SmsFailedRequest;
import com.repush.dao.domain.SmsSdr;
import com.repush.dao.domain.SmsStatePush;
import com.repush.dao.domain.LostMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


@Mapper
public interface SmsMapper {

    List<Map<String, String>> findAllHcode();

    List<Map<String, String>> findAllGsmCenter();

    List<String> findAllBlackList();

    List<String> findAllWhiteList();

    List<String> findAllMultipleBlackList();


    List<SmsFailedRequest> findAllSmsFailedRequest(String ip);

    void saveFailedRequest(SmsFailedRequest smsFailedRequest);

    void updateFailedRequest(SmsFailedRequest smsFailedRequest);

    void delById(String id);

    List<Map<String, String>> findAllNumberOperator();

    String isExistTable(String tableName);

    void createSmsTable(@Param("tableName") String tableName);

    void saveSmsSdr(SmsSdr smsSdr);

    void saveSmsFailedStatePush(SmsStatePush smsStatePush);

    void saveLostMsg(LostMsg lostMsg);


}
