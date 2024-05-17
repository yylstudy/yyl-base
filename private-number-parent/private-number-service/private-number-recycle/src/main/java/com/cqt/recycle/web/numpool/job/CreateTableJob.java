package com.cqt.recycle.web.numpool.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.dto.CorpInfoVO;
import com.cqt.recycle.web.corpinfo.mapper.PrivateCorpBusinessInfoMapper;
import com.cqt.recycle.web.numpool.mapper.PrivateTableCreateMapper;
import com.cqt.xxljob.annotations.XxlJobRegister;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2021/12/7 9:48
 * 定时任务 自动创建表
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CreateTableJob {

    private final static String STATS_DATASOURCE_NAME = "stats";

    private final HideProperties hideProperties;

    private final PrivateTableCreateMapper privateTableCreateMapper;

    private final PrivateCorpBusinessInfoMapper privateCorpBusinessInfoMapper;

    private final ObjectMapper objectMapper;

    private final SqlSessionFactory sqlSessionFactory;

    @Resource(name = "shardingDataSource")
    private AbstractDataSourceAdapter shardingDataSource;

    @XxlJobRegister(jobDesc = "通用自动创建表",
            cron = "0 0 4 * * ?",
            triggerStatus = 0,
            executorRouteStrategy = ExecutorRouteStrategyEnum.ROUND)
    @XxlJob("createTableJobHandler")
    public void createTable() throws JsonProcessingException {

        DateTime tomorrow = DateUtil.date();
        DateTime dateTime = DateUtil.offset(tomorrow, DateField.DAY_OF_YEAR, 7);
        String jobParam = XxlJobHelper.getJobParam();
        /*
         * {"AXB": true, "AX": true, "AXE": false, "AXBN": false, "AXG": false, "AXYB": false}
         */
        log.info("xxljob param: {}", jobParam);
        HashMap<String, Boolean> createFlagMap = new HashMap<>(8);
        if (StrUtil.isNotEmpty(jobParam)) {
            createFlagMap = objectMapper.readValue(jobParam, new TypeReference<HashMap<String, Boolean>>() {
            });
        }
        DataSource dataSource = shardingDataSource.getDataSourceMap().get(STATS_DATASOURCE_NAME);
        // 日期 每日 yyyyMMdd
        List<DateTime> dateTimes = DateUtil.rangeToList(tomorrow, dateTime, DateField.DAY_OF_YEAR);
        for (DateTime time : dateTimes) {
            String date = DateUtil.format(time, "yyyyMMdd");
            // B机房 自动创建表 下一天
            if (SystemConstant.B.equals(hideProperties.getCurrentLocation())) {
                if (CollUtil.isNotEmpty(createFlagMap)) {
                    if (createFlagMap.get(BusinessTypeEnum.AXB.name())) {
                        privateTableCreateMapper.createTableOfBindInfoAxbHis(date);
                        log.info("create AXB his table of {}", date);
                    }
                    if (createFlagMap.get(BusinessTypeEnum.AX.name())) {
                        privateTableCreateMapper.createTableOfBindInfoAxHis(date);
                        log.info("create AX his table of {}", date);
                    }
                    if (createFlagMap.get(BusinessTypeEnum.AXE.name())) {
                        privateTableCreateMapper.createTableOfBindInfoAxeHis(date);
                        log.info("create AXE his table of {}", date);
                    }
                    if (createFlagMap.get(BusinessTypeEnum.AXBN.name())) {
                        privateTableCreateMapper.createTableOfBindInfoAxbnHis(date);
                        log.info("create AXBN his table of {}", date);
                    }
                    if (createFlagMap.get(BusinessTypeEnum.AXG.name())) {
                        privateTableCreateMapper.createTableOfBindInfoAxgHis(date);
                        log.info("create AXG his table of {}", date);
                    }
                    if (createFlagMap.get(BusinessTypeEnum.AXYB.name())) {
                        privateTableCreateMapper.createTableOfBindInfoAxybHis(date);
                        log.info("create AXYB his table of {}", date);
                    }
                }
            }
            HashMap<String, String> paramMap = new HashMap<>(16);
            paramMap.put("date", date);
            BoundSql boundSql = getBoundSql("com.cqt.recycle.web.numpool.mapper.PrivateTableCreateMapper.createTableOfCallEventStats", paramMap);
            try {
                int execute = Db.use(dataSource).execute(boundSql.getSql());
                log.info("createTableOfCallEventStats: {}, {}", date, execute);
            } catch (SQLException e) {
                log.error("err: ", e);
            }

        }

        // 企业id
        List<CorpInfoVO> corpInfoVOList = privateCorpBusinessInfoMapper.getStatsCorpInfo();

        // 每月
        List<DateTime> monthList = DateUtil.rangeToList(DateUtil.date(), DateUtil.offset(DateUtil.date(), DateField.MONTH, 5), DateField.MONTH);
        for (DateTime time : monthList) {
            String month = DateUtil.format(time, "yyyyMM");

            // 企业并发量统计
            HashMap<String, String> paramMap = new HashMap<>(16);
            paramMap.put("month", month);
            BoundSql boundSql = getBoundSql("com.cqt.recycle.web.numpool.mapper.PrivateTableCreateMapper.createTableOfCorpConcurrencyInfo", paramMap);
            try {
                int execute = Db.use(dataSource).execute(boundSql.getSql());
                log.info("createTableOfCorpConcurrencyInfo: {}, {}", month, execute);
            } catch (SQLException e) {
                log.error("createTableOfCorpConcurrencyInfo err: ", e);
            }

            // 短信
            for (CorpInfoVO corpInfoVO : corpInfoVOList) {
                paramMap.put("vccId", corpInfoVO.getVccId());
                BoundSql smsSql = getBoundSql("com.cqt.recycle.web.numpool.mapper.PrivateTableCreateMapper.createSmsTable", paramMap);
                try {
                    int execute = Db.use(dataSource).execute(smsSql.getSql());
                    log.info("createSmsTable: {}, {}", paramMap, execute);
                } catch (SQLException e) {
                    log.error("createSmsTable err: ", e);
                }
            }

            // 每小时绑定次数统计表
            if (SystemConstant.B.equals(hideProperties.getCurrentLocation())) {
                privateTableCreateMapper.createTableOfCorpBindCountStats(month);
                log.info("createTableOfCorpBindCountStats: {}", month);
            }

        }
    }

    /**
     * 创建表sql
     */
    private BoundSql getBoundSql(String id, HashMap<String, String> paramMap) {
        return sqlSessionFactory.getConfiguration()
                .getMappedStatement(id)
                .getBoundSql(paramMap);
    }

}
