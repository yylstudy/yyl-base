package com.cqt.cdr.cloudccsfaftersales.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.cdr.cloudccsfaftersales.entity.PushErr;
import com.cqt.cdr.cloudccsfaftersales.service.PushErrService;
import com.cqt.cdr.cloudccsfaftersales.mapper.PushErrMapper;
import com.cqt.cdr.cloudccsfaftersales.util.AccessRemote;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @description 针对表【cc_push_err】的数据库操作Service实现
 * @createDate 2023-09-08 14:13:57
 */
@Service
public class PushErrServiceImpl extends ServiceImpl<PushErrMapper, PushErr>
        implements PushErrService {
    @Resource
    AccessRemote accessRemote;

//    public void insetPushError(String LOG_TAG, CdrMessageDTO cdrMessageDTO, String companyCode) {
//        RemoteQualityCdrDTO remoteQualityCdrDTO = accessRemote.getRemoteQualityCdrDTO(cdrMessageDTO, companyCode);
//        RemoteCdrVO remoteCdrVO = accessRemote.sendQualityCdr(item, remoteQualityCdrDTO, LOG_TAG);
//        if (remoteCdrVO != null && !remoteCdrVO.getCode().equals("200")) {
//            PushErrorJson<RemoteQualityCdrDTO> data = new PushErrorJson<>();
//            data.setData(remoteQualityCdrDTO);
//            data.setCompanyCode(companyCode);
//            String month = DateUtil.format(DateUtil.date(), CommonConstant.MONTH_FORMAT);
//            errIntoErrTable(JSONObject.toJSONString(data), removeUrl, dynamicConfig.getPushnum() + "次推送失败", month, companyCode, LOG_TAG);
//        }
//    }

    public boolean errIntoErrTable(String json, String url, String reason, String month, String companyCode, String LOG_TAG, String type) {
        boolean flag = true;
        try {
            baseMapper.insert(PushErr.getPushErr(json, companyCode, url, reason, type));
        } catch (BadSqlGrammarException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("doesn't exist")) {
                boolean isSuccess = createTable("cc_push_err", month, companyCode, LOG_TAG);
                if (isSuccess) {
                    baseMapper.insert(PushErr.getPushErr(json, companyCode, url, reason, type));
                } else {
                    flag = false;
                }
            }
        } catch (Exception e) {
            log.error(LOG_TAG + "推送失败数据入库异常：", e);
            flag = false;
        }
        return flag;
    }

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




