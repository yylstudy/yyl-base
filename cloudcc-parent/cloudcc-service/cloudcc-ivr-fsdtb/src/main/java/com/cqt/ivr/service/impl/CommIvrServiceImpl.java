package com.cqt.ivr.service.impl;
import com.cqt.ivr.entity.IvrInfo;
import com.cqt.ivr.entity.QueueAgentInfo;
import com.cqt.ivr.entity.dto.BuryingPointReq;
import com.cqt.ivr.entity.dto.CommIvrReq;
import com.cqt.ivr.entity.dto.QueueStatusReq;
import com.cqt.ivr.entity.vo.TableResult;
import com.cqt.ivr.interceptor.TheadLocalUtil;
import com.cqt.ivr.mapper.BuryingPointDao;
import com.cqt.ivr.mapper.CommIvrDao;
import com.cqt.ivr.service.CommIvrService;
import com.github.pagehelper.PageInfo;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.github.pagehelper.PageHelper;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Title: NoticeServiceImpl
 * @Description:
 * @author xinson
 */

//@Service("acrRecordService")
@Service
public class CommIvrServiceImpl implements CommIvrService {
    private static final Logger logger = LoggerFactory.getLogger(CommIvrServiceImpl.class);
    private static final Logger txCompanyIvrInfoLog = LoggerFactory.getLogger("txCompanyIvrInfoLog");

    @Resource
    private CommIvrDao commIvrDao;

    @Resource
    private BuryingPointDao buryingPointDao;

    @Override
    public TableResult getIvrInfoList(CommIvrReq commIvrReq, String LOG_TAG) {
        TableResult tableResult = new TableResult();
        tableResult.setMessage("获取成功");
        tableResult.setStatus(0);
        try {
            if (StringUtil.isEmpty(commIvrReq.getCompany_code())){
                tableResult.setMessage("companyCode必填");
                tableResult.setStatus(500);
            }else{
                PageHelper.startPage(commIvrReq.getPageNo(), commIvrReq.getPageSize());
                List<IvrInfo> list = commIvrDao.getIvrInfoList(commIvrReq);
                PageInfo<IvrInfo> listInfo = new PageInfo<>(list);
                tableResult.setRows(listInfo.getList());
                tableResult.setTotal(listInfo.getTotal());
                tableResult.setTotalPages((long) listInfo.getPages());
            }
        }catch (Exception e){
            logger.error(LOG_TAG + "操作异常:", e);
            tableResult.setMessage("操作异常");
            tableResult.setStatus(500);
        }
        return tableResult;
    }

    @Override
    public void insertAllsByMonth(BuryingPointReq req, String LOG_TAG) {
        //全量入库月份表
        try {
            logger.info(LOG_TAG + "入月份表信息：" + req);
            //全量入库月份表
            buryingPointDao.insertAllsByMonth(req);
        } catch (Exception e) {
            logger.error(LOG_TAG + "入库月份表异常：", e);
            if (StringUtil.isNotEmpty(e.getMessage())) {
                if (e.getMessage().contains("doesn't exist")) {
                    String tx_company_ivr_month = tables("tx_company_ivr", req.getMonth(), req.getCompany_code(), txCompanyIvr());
                    try {
                        logger.info(LOG_TAG + "表不存在创建表再次写入");
                        buryingPointDao.superManagerSelect(tx_company_ivr_month);
                    } catch (Exception e1) {
                        logger.error(LOG_TAG + "创建表异常", e1);
                    }
                    try {
                        //全量入库月份表
                        buryingPointDao.insertAllsByMonth(req);
                    } catch (Exception e1) {
                        logger.error(LOG_TAG + "创建表或写入异常", e1);
                        txCompanyIvrInfoLog.info(TheadLocalUtil.instance().getSql());
                    }
                } else {
                    logger.error(LOG_TAG + "写入异常", e);
                    txCompanyIvrInfoLog.info(TheadLocalUtil.instance().getSql());
                }
            } else {
                logger.error(LOG_TAG + "写入异常", e);
                txCompanyIvrInfoLog.info(TheadLocalUtil.instance().getSql());
            }
        } finally {
            TheadLocalUtil.instance().reset();
        }
    }

    @Override
    public void superManagerSelect(String str) {
        buryingPointDao.superManagerSelect(str);
    }

    @Override
    public List<BuryingPointReq> getBuryingPointInfo(BuryingPointReq req) {
        return buryingPointDao.getBuryingPointInfo(req);
    }

    @Override
    public String getQueueNameBySysQueueId(QueueStatusReq req) {
        return commIvrDao.getQueueNameBySysQueueId(req);
    }

    public static String tables(String tablename, String month, String vccid, String tablefield){
        String str =  "CREATE TABLE IF NOT EXISTS `"+tablename+"_"+vccid+"_"+month+"` "+tablefield;
        return str;
    }

    public static String txCompanyIvr(){

        String  str=" LIKE "+"`cloudcc_tmptable`.tx_company_ivr;";
        return str;

    }
}
