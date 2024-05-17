package com.cqt.cdr.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.cdr.interceptor.TheadLocalUtil;
import com.cqt.cdr.mapper.CdrChanneldataMapper;
import com.cqt.cdr.mapper.MainCdrMapper;
import com.cqt.cdr.service.ICdrChanneldataService;
import com.cqt.cdr.util.CommonUtils;
import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.CdrChanneldata;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.client.RedisConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.cqt.cdr.util.CommonUtils.tables;

/**
 * @author ld
 * @since 2023-08-15
 */
@Service
@Slf4j
public class CdrChanneldataServiceImpl extends ServiceImpl<CdrChanneldataMapper, CdrChanneldata> implements ICdrChanneldataService {
    private static final Logger MYSQL_FAIL_LOG = LoggerFactory.getLogger("cdrInsertFailLogger");

    @Resource
    MainCdrMapper mainCdrMapper;

    @Resource
    private RedissonUtil redissonUtil;

    @Override
    public void insertCdrChannel(String LOG_TAG, CallCenterMainCdr mainCdr, CdrChanneldata cdrChanneldata, String companyCode, String month) {

        cdrChanneldata.setCallId(mainCdr.getCallId());
        cdrChanneldata.setUuid(mainCdr.getUuid());
        cdrChanneldata.setClientUuid(mainCdr.getClientUuid());
        cdrChanneldata.setDirection(mainCdr.getDirection());
        cdrChanneldata.setCreateTime(mainCdr.getCallEndTime());
        cdrChanneldata.setIvrParametersData(cdrChanneldata.getClientUuid() != null ? redissonUtil.get("cc_ivr_track_data_" + cdrChanneldata.getClientUuid()) : "");
        cdrChanneldata.setRunccTimes(CommonUtils.timeStamp2Date(cdrChanneldata.getRunccTimes(), "yyyy-MM-dd HH:mm:ss"));
        try {
            this.save(cdrChanneldata);
        } catch (DuplicateKeyException duplicateKeyException) {
        } catch (Exception e) {
            log.error(LOG_TAG + "通道变量录入：{}，录入异常", cdrChanneldata, e);
            if (e instanceof TransientDataAccessResourceException) {
                throw e;
            }
            MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
        } finally {
            TheadLocalUtil.instance().reset();
        }
    }

//    @Override
//    public void insertCdrChannel(String LOG_TAG, CallCenterMainCdr mainCdr, CdrChanneldata cdrChanneldata, String companyCode, String month) {
//        try {
//            cdrChanneldata.setCallId(mainCdr.getCallId());
//            cdrChanneldata.setUuid(mainCdr.getUuid());
//            cdrChanneldata.setClientUuid(mainCdr.getClientUuid());
//            cdrChanneldata.setDirection(mainCdr.getDirection());
//            cdrChanneldata.setCreateTime(mainCdr.getCallEndTime());
//            cdrChanneldata.setIvrParametersData(cdrChanneldata.getClientUuid() != null ? redissonUtil.get("cc_ivr_track_data_" + cdrChanneldata.getClientUuid()) + "" : null);
//            this.save(cdrChanneldata);
//        } catch (DuplicateKeyException duplicateKeyException) {
//            log.warn(LOG_TAG + "通道变量录入：{}，录入异常", cdrChanneldata, duplicateKeyException);
//        } catch (BadSqlGrammarException badSqlGrammarException) {
//            badSqlGrammarException.printStackTrace();
//            String errorMessage = badSqlGrammarException.getMessage();
//            if (errorMessage.contains("doesn't exist")) {
//                String cloucccChanneldata = tables("clouccc_cdr_channeldata", month, companyCode);
//                mainCdrMapper.createTable(cloucccChanneldata);
//                try {
//                    this.save(cdrChanneldata);
//                } catch (Exception ex) {
//                    log.warn(LOG_TAG + "通道变量录入：{}，录入异常", cdrChanneldata, ex);
//                    MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
//                }
//            }
//        } catch (Exception e) {
//            log.error(LOG_TAG + "通道变量录入：{}，录入异常", cdrChanneldata, e);
//            MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
//        } finally {
//            TheadLocalUtil.instance().reset();
//        }
//    }
}
