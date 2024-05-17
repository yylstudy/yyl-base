package com.cqt.cdr.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.cdr.entity.PushErr;
import com.cqt.cdr.service.PushErrService;
import com.cqt.cdr.mapper.PushErrMapper;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【cc_push_err】的数据库操作Service实现
* @createDate 2023-08-28 16:26:02
*/
@Service
public class PushErrServiceImpl extends ServiceImpl<PushErrMapper, PushErr>
    implements PushErrService{

    /**
     * 错误信息入库
     *
     * @param json
     * @param url
     * @param reason
     * @param month
     * @param companyCode
     * @param LOG_TAG
     * @return
     */
    public boolean errIntoErrTable(String json, String url, String reason, String month, String companyCode, String LOG_TAG) {
        boolean flag = true;
        try {
            baseMapper.insert(PushErr.getPushErr(json, companyCode, url, reason));
        } catch (BadSqlGrammarException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("doesn't exist")) {
                boolean isSuccess = createTable("cc_push_err", month, companyCode, LOG_TAG);
                if (isSuccess) {
                    baseMapper.insert(PushErr.getPushErr(json, companyCode, url, reason));
                } else {
                    flag = false;
                }
            }
        } catch (Exception e) {
            log.error(LOG_TAG + "数据入库异常：", e);
            flag = false;
        }
        return flag;
    }

    /**
     * 创建表
     *
     * @param tablename
     * @param yearmonth
     * @param companycode
     * @param LOG_TAG
     * @return
     */
    public boolean createTable(String tablename, String yearmonth, String companycode, String LOG_TAG) {
        boolean flag = true;
        StringBuilder table = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(tablename)
                .append("_")
                .append(yearmonth)
                .append(" LIKE ")
                .append("cloudcc_tmptable." + tablename)
                .append(";");
        try {
            baseMapper.createTable(table.toString());
        } catch (Exception e) {
            log.error(LOG_TAG + "创建表异常", e);
            flag = false;
        }
        return flag;
    }
}




