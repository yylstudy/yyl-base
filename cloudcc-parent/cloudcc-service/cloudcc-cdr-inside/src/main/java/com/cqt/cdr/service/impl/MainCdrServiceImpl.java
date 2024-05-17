package com.cqt.cdr.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.cdr.interceptor.TheadLocalUtil;
import com.cqt.cdr.mapper.MainCdrMapper;
import com.cqt.cdr.service.IMainCdrService;
import com.cqt.cdr.util.CommonUtils;
import com.cqt.model.cdr.entity.CallCenterMainCdr;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import static com.cqt.cdr.util.CommonUtils.tables;

/**
 * @author ld
 * @since 2023-08-15
 */
@Service
@Slf4j
public class MainCdrServiceImpl extends ServiceImpl<MainCdrMapper, CallCenterMainCdr> implements IMainCdrService {
    private static final Logger MYSQL_FAIL_LOG = LoggerFactory.getLogger("cdrInsertFailLogger");

    @Override
    public void inserMainCdr(String LOG_TAG, CallCenterMainCdr mainCdr, String companyCode, String month) {

        mainCdr.setCallerNumber(CommonUtils.replaceNumPerfix(mainCdr.getCallerNumber()));
        mainCdr.setDisplayNumber(CommonUtils.replaceNumPerfix(mainCdr.getDisplayNumber()));
        mainCdr.setCalleeNumber(CommonUtils.replaceNumPerfix(mainCdr.getCalleeNumber()));
        mainCdr.setPlatformNumber(CommonUtils.replaceNumPerfix(mainCdr.getPlatformNumber()));
        mainCdr.setChargeNumber(CommonUtils.replaceNumPerfix(mainCdr.getChargeNumber()));
        try {
            this.baseMapper.insert(mainCdr);
        } catch (DuplicateKeyException duplicateKeyException) {
        }catch (Exception e) {
            log.error(LOG_TAG + "主话单：{}，录入异常", mainCdr, e);
            if (e instanceof TransientDataAccessResourceException) {
                throw e;
            }
            MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
        } finally {
            TheadLocalUtil.instance().reset();
        }
    }

//    @Override
//    public void inserMainCdr(String LOG_TAG, CallCenterMainCdr mainCdr, String companyCode, String month) {
//        try {
//            this.baseMapper.insert(mainCdr);
//        } catch (DuplicateKeyException duplicateKeyException) {
//            duplicateKeyException.printStackTrace();
//        } catch (BadSqlGrammarException badSqlGrammarException) {
//            badSqlGrammarException.printStackTrace();
//            String errorMessage = badSqlGrammarException.getMessage();
//            if (errorMessage.contains("doesn't exist")) {
//                String cloucccSubCdr = tables("clouccc_main_cdr", month, companyCode);
//                this.baseMapper.createTable(cloucccSubCdr);
//                try {
//                    this.baseMapper.insert(mainCdr);
//                } catch (Exception ex) {
//                    log.warn(LOG_TAG + "主话单sql：{}，录入异常存入cdr-insert-fail日志", TheadLocalUtil.instance().getSql(), ex);
//                    MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
//                }
//            } else {
//                log.warn(LOG_TAG + "主话单sql：{}，录入异常存入cdr-insert-fail日志", TheadLocalUtil.instance().getSql(), badSqlGrammarException);
//                MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
//            }
//        } catch (Exception e) {
//            log.error(LOG_TAG + "主话单：{}，录入异常", mainCdr, e);
//            MYSQL_FAIL_LOG.info(TheadLocalUtil.instance().getSql());
//        } finally {
//            TheadLocalUtil.instance().reset();
//        }
//    }
}
