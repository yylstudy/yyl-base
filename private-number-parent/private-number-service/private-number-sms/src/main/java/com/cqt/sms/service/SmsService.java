package com.cqt.sms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.sms.dao.mapper.SmsMapper;
import com.cqt.sms.dao.mapper.VccIdConfigInfoMapper;
import com.cqt.sms.model.dto.SmsStatePush;
import com.cqt.sms.model.dto.VccIdConfigInfo;
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

    @Resource
    private VccIdConfigInfoMapper vccIdConfigInfoMapper;

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

    /**
     * 查询所有企业配置
     */
    public List<VccIdConfigInfo> findAllVccIdConfigInfo(){
        LambdaQueryWrapper<VccIdConfigInfo> queryWrapper = new LambdaQueryWrapper<>();
        return vccIdConfigInfoMapper.selectList(queryWrapper);
    }

    /**
     * 查询所有企业配置bg vccId
     */
    public VccIdConfigInfo findVccIdConfigInfoByVccId(String vccId){
        LambdaQueryWrapper<VccIdConfigInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VccIdConfigInfo::getVccId,vccId);
        return vccIdConfigInfoMapper.selectOne(queryWrapper);
    }

    public void saveSmsFailedStatePush(SmsStatePush smsStatePush) {
        smsMapper.saveSmsFailedStatePush(smsStatePush);
    }


}
