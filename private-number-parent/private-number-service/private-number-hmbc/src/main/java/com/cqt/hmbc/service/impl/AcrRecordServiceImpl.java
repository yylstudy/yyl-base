package com.cqt.hmbc.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.cqt.hmbc.mapper.AcrRecordMapper;
import com.cqt.hmbc.service.AcrRecordService;
import com.cqt.model.hmbc.dto.AcrRecordQueryDTO;
import com.cqt.model.hmbc.dto.CdrRecordSimpleEntity;
import com.cqt.model.hmbc.properties.HmbcProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * AcrRecordServiceImpl
 *
 * @author Xienx
 * @date 2023年02月10日 9:46
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcrRecordServiceImpl implements AcrRecordService {

    private final HmbcProperties hmbcProperties;
    private final AcrRecordMapper acrRecordMapper;

    @DS("#dsName")
    @Override
    public Long findAcr(CdrRecordSimpleEntity cdrRecord, String dsName) {
        String yyyyMm = DateUtil.format(new Date(), DatePattern.SIMPLE_MONTH_PATTERN);
        AcrRecordQueryDTO queryDTO = new AcrRecordQueryDTO(cdrRecord.getVccId(), yyyyMm);

        queryDTO.setServiceKey(hmbcProperties.getDialTest().getServiceKey());
        queryDTO.setCalledpartynumber(cdrRecord.getCalledNum());
        queryDTO.setCallerNumbers(hmbcProperties.getDialTest().getCallerNumbers());
        try {
            return acrRecordMapper.acrQuery(queryDTO);
        } catch (Exception e) {
            log.error("[话单查询] 表名: {}, 数据源: {}, 查询异常: ", queryDTO.getTableName(), dsName, e);
            return null;
        }
    }

    @Override
    public String getGtCodeByNumber(String number) {
        return acrRecordMapper.getGtCodeByNumber(number);
    }

}
