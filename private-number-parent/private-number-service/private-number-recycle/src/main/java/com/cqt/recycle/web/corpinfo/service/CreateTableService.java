package com.cqt.recycle.web.corpinfo.service;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.recycle.web.numpool.mapper.PrivateTableCreateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-02-21 10:01
 * 创建绑定关系表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTableService {

    private final PrivateTableCreateMapper privateTableCreateMapper;

    private final HideProperties hideProperties;


    /**
     * 创建绑定关系表按vccId, 只能在扬州机房B
     *
     * @param vccId        企业id
     * @param businessType 业务模式
     */
    public void createTable(String vccId, String businessType) {

        Integer bindTableSharingMaxIndex = hideProperties.getBindTableSharingMaxIndex();
        String bindTableSharingBusinessType = hideProperties.getBindTableSharingBusinessType();

        if (ReUtil.isMatch(businessType, BusinessTypeEnum.AXB.name()) && enableCreate()) {
            privateTableCreateMapper.createBindAxb(vccId);
            privateTableCreateMapper.createBindAxbInit(vccId);
            logCreateTable(vccId, BusinessTypeEnum.AXB.name());
        }
        if (ReUtil.isMatch(businessType, BusinessTypeEnum.AXE.name())) {
            // AXE是否分表, 同时连多个数据源创建表
            if (isSharding(bindTableSharingBusinessType, businessType)) {
                for (int i = 0; i <= bindTableSharingMaxIndex; i++) {
                    // 当前数据库bind0
                    privateTableCreateMapper.createBindAxeSharding(vccId, i);
                    logCreateTable(vccId + "_" + i, BusinessTypeEnum.AXE.name());
                }
            }
            // 原始axe表
            privateTableCreateMapper.createBindAxe(vccId);
            logCreateTable(vccId, BusinessTypeEnum.AXE.name());
        }
        if (ReUtil.isMatch(businessType, BusinessTypeEnum.AX.name()) && enableCreate()) {
            privateTableCreateMapper.createBindAx(vccId);
            logCreateTable(vccId, BusinessTypeEnum.AX.name());
        }
        if (ReUtil.isMatch(businessType, BusinessTypeEnum.AXBN.name()) && enableCreate()) {
            privateTableCreateMapper.createBindAxbn(vccId);
            privateTableCreateMapper.createBindAxbnRealTel(vccId);
            logCreateTable(vccId, BusinessTypeEnum.AXBN.name());
        }
        if (ReUtil.isMatch(businessType, BusinessTypeEnum.AXYB.name()) && enableCreate()) {
            privateTableCreateMapper.createBindAxyb(vccId);
            logCreateTable(vccId, BusinessTypeEnum.AXYB.name());
        }
        if (ReUtil.isMatch(businessType, BusinessTypeEnum.AXG.name()) && enableCreate()) {
            privateTableCreateMapper.createBindAxg(vccId);
            logCreateTable(vccId, BusinessTypeEnum.AXG.name());
        }
    }


    private void logCreateTable(String vccId, String businessType) {
        log.info("创建绑定关系表: {}, {}", vccId, businessType);
    }

    private Boolean enableCreate() {
        return SystemConstant.B.equals(hideProperties.getCurrentLocation());
    }

    public Boolean isSharding(String shardingBusinessType, String businessTypeRegx) {
        if (StrUtil.isEmpty(shardingBusinessType)) {
            return false;
        }

        List<String> list = StrUtil.split(shardingBusinessType, "|");
        for (String type : list) {
            if (ReUtil.isMatch(businessTypeRegx, type)) {
                return true;
            }
        }

        return false;
    }
}
