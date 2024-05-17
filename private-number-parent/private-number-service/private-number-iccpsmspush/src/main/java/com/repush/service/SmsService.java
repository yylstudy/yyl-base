package com.repush.service;

import com.repush.dao.domain.LostMsg;
import com.repush.dao.domain.SmsFailedRequest;
import com.repush.dao.domain.SmsSdr;
import com.repush.dao.domain.SmsStatePush;
import com.repush.dao.mapper.SmsMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 修改人：	@author fat boy y
 * 修改时间：	2019-09-05 下午03:59:37
 * 修改备注：
 */
@Service
public class SmsService {

    @Resource
    private SmsMapper smsMapper;


    public List<Map<String, String>> findAllHcode() {
        return smsMapper.findAllHcode();
    }

    public List<Map<String, String>> findAllGsmCenter() {
        return smsMapper.findAllGsmCenter();
    }

    public List<String> findAllBlackList() {
        return smsMapper.findAllBlackList();
    }

    public List<String> findAllWhiteList() {
        return smsMapper.findAllWhiteList();
    }

    public List<String> findAllMultipleBlackList() {
        return smsMapper.findAllMultipleBlackList();
    }


    public List<SmsFailedRequest> findAllSmsFailedRequest(String ip) {
        return smsMapper.findAllSmsFailedRequest(ip);
    }

    public void delById(String id) {
        smsMapper.delById(id);
    }

    public void saveFailedRequest(SmsFailedRequest smsFailedRequest) {
        smsMapper.saveFailedRequest(smsFailedRequest);
    }

    public void updateFailedRequest(SmsFailedRequest smsFailedRequest) {
        smsMapper.updateFailedRequest(smsFailedRequest);
    }

    public List<Map<String, String>> findAllNumberOperator() {
        return smsMapper.findAllNumberOperator();
    }

    public String isExistTable(String tableName) {
        return null;
    }

    public void createSmsTable(String tableName) {
        smsMapper.createSmsTable(tableName);
    }

    /**
     * 入库短信记录
     *
     * @param smsSdr
     */
    public void saveSmsSdr(SmsSdr smsSdr) {
        smsMapper.saveSmsSdr(smsSdr);
    }

    public void saveSmsFailedStatePush(SmsStatePush smsStatePush) {
        smsMapper.saveSmsFailedStatePush(smsStatePush);
    }

    public void saveLostMsg(LostMsg lostMsg) {
        smsMapper.saveLostMsg(lostMsg);
    }


}
