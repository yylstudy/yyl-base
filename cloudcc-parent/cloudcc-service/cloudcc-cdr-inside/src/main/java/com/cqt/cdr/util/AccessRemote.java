package com.cqt.cdr.util;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.cqt.cdr.conf.DynamicConfig;
import com.cqt.feign.freeswitch.FreeswitchApiFeignClient;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import com.cqt.model.freeswitch.dto.api.PlayRecordDTO;
import com.cqt.model.freeswitch.vo.PlayRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccessRemote {
    @Resource
    private FreeswitchApiFeignClient freeswitchApiFeignClient;

    @Resource
    private DynamicConfig dynamicConfig;

    /**
     * 获取存储路径
     *
     * @param serviceId
     * @param subCdr
     * @param LOG_TAG
     * @return
     */
    public boolean getStoragePath(String serviceId, CallCenterSubCdr subCdr, String LOG_TAG) {
        try {
            String recordUrl = subCdr.getRecordUrl();
            if (StringUtils.isEmpty(recordUrl)) {
                return true;
            }
            String specificPath = "/" + serviceId + recordUrl.substring("/home/lcc_media".length());
            PlayRecordDTO playRecordDTO = new PlayRecordDTO(subCdr.getCompanyCode(), specificPath);
            PlayRecordVO playRecord = null;
            for (int i = 0; i < dynamicConfig.getPushnum(); i++) {
                playRecord = getPlayRecordVO(playRecordDTO, LOG_TAG);
                if (playRecord != null) {
                    break;
                }
                ThreadUtils.sleep(dynamicConfig.getSleepTime());
            }
            if (playRecord.getResult()) {
                subCdr.setWsRtsp(playRecord.getWsRtsp());
                subCdr.setRecordUrlRtsp(playRecord.getRecordUrlRtsp());
                subCdr.setAbsoluteUrl(playRecord.getRecordUrl());
                subCdr.setRecordUrlIn(playRecord.getRecordUrlIn());
            }
        } catch (Exception e) {
            log.error(LOG_TAG + "getStoragePath异常：", e);
            return false;
        }
        return true;
    }

    /**
     * 访问远程接口
     *
     * @param playRecordDTO
     * @param LOG_TAG
     * @return
     */
    public PlayRecordVO getPlayRecordVO(PlayRecordDTO playRecordDTO, String LOG_TAG) {
        PlayRecordVO playRecord = null;
        try {
            log.info(LOG_TAG + "访问底层接口接收参数：{}", playRecordDTO);
            playRecord = freeswitchApiFeignClient.getPlayRecord(playRecordDTO);
            log.info(LOG_TAG + "访问底层接口返回结果：{}", playRecord);
        } catch (Exception e) {
            log.error(LOG_TAG + "传递参数：{}，访问底层接口异常：", playRecordDTO, e);
        }
        return playRecord;
    }
}
