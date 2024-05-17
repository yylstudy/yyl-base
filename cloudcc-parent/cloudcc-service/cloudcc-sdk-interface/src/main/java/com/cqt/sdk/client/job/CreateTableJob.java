package com.cqt.sdk.client.job;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.util.Utils;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.common.CloudCallCenterProperties;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.sdk.cache.CdrSqlCache;
import com.cqt.sdk.client.mapper.CreateTableMapper;
import com.cqt.xxljob.annotations.XxlJobRegister;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.StringReader;
import java.sql.Connection;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author linshiqiang
 * date:  2023-09-04 17:19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTableJob {

    private static final ExecutorService EXECUTOR_SERVICE = ThreadUtil.newExecutor(5);

    private final CreateTableMapper createTableMapper;

    private final CommonDataOperateService commonDataOperateService;

    private final DataSource dataSource;

    private final CloudCallCenterProperties cloudCallCenterProperties;


    /**
     * 建表定时任务
     */
    @XxlJobRegister(jobDesc = "定时创建企业表(按企业id和月份)",
            cron = "0 0 0 26 * ?",
            triggerStatus = 1,
            executorParam = "3",
            executorRouteStrategy = ExecutorRouteStrategyEnum.CONSISTENT_HASH)
    @XxlJob("createCompanyTable")
    public void createTable() {
        String jobParam = XxlJobHelper.getJobParam();
        int monthCount = 5;
        if (StrUtil.isNotEmpty(jobParam)) {
            monthCount = Integer.parseInt(jobParam);
        }
        DateTime offset = DateUtil.offset(DateUtil.date(), DateField.MONTH, monthCount);
        List<DateTime> monthList = DateUtil.rangeToList(DateUtil.date(), offset, DateField.MONTH);

        Set<String> allCompanyCode = commonDataOperateService.getAllCompanyCode();
        for (String companyCode : allCompanyCode) {
            CompanyInfo companyInfoDTO = commonDataOperateService.getCompanyInfoDTO(companyCode);
            String vccId = companyInfoDTO.getVccId();
            for (DateTime dateTime : monthList) {
                String month = DateUtil.format(dateTime, CommonConstant.MONTH_FORMAT);
                createCompanyTable(companyCode, vccId, month);
            }
        }
    }

    /**
     * 创建企业时 创建当月表
     *
     * @param companyCode 企业id
     */
    public void createCompanyTable(String companyCode) {
        DateTime date = DateUtil.date();
        String month = DateUtil.format(date, CommonConstant.MONTH_FORMAT);
        CompanyInfo companyInfoDTO = commonDataOperateService.getCompanyInfoDTO(companyCode);
        String vccId = "";
        if (Objects.nonNull(companyInfoDTO)) {
            vccId = companyInfoDTO.getVccId();
        }
        createCompanyTable(companyCode, vccId, month);
    }

    /**
     * 创建企业表
     *
     * @param companyCode 企业id
     * @param vccId       企业vccId
     * @param month       月份
     */
    public void createCompanyTable(String companyCode, String vccId, String month) {
        try {
            log.info("建表, companyCode: {}, vccId: {}, month: {}", companyCode, vccId, month);
            if (dataSource instanceof DynamicRoutingDataSource) {
                DynamicRoutingDataSource dynamicRoutingDataSource = (DynamicRoutingDataSource) dataSource;
                Map<String, DataSource> dataSources = dynamicRoutingDataSource.getDataSources();
                String cdrDbNames = cloudCallCenterProperties.getDefaultConfig().getCdrDbNames();
                List<String> list = StrUtil.split(cdrDbNames, StrUtil.COMMA);
                String sql = CdrSqlCache.get();
                HashMap<String, String> map = new HashMap<>();
                map.put(CommonConstant.COMPANY_CODE_KEY, companyCode);
                map.put(CommonConstant.VCC_ID_KEY, vccId);
                map.put(CommonConstant.MONTH_KEY, month);
                StringReader stringReader = new StringReader(Utils.format(sql, map, true));
                for (String name : list) {
                    if (StrUtil.isNotEmpty(vccId)) {
                        createTableMapper.createAcrRecordTable(vccId, month);
                    }
                    DataSource dataSource = dataSources.get(name);
                    if (Objects.isNull(dataSource)) {
                        continue;
                    }
                    Connection connection = dataSource.getConnection();
                    connection.setNetworkTimeout(EXECUTOR_SERVICE, Convert.toInt(Duration.ofMinutes(30).toMillis()));
                    try {
                        ScriptRunner scriptRunner = new ScriptRunner(connection);
                        scriptRunner.runScript(stringReader);
                    } catch (Exception e) {
                        log.error("[createCompanyTable] db: {}, runScript error: ", name, e);
                    } finally {
                        DataSourceUtils.releaseConnection(connection, dataSource);
                    }
                }
                return;
            }
            createTableMapper.createAgentStatusTable(companyCode, month);
            createTableMapper.createExtStatusTable(companyCode, month);
            createTableMapper.createMainCdrTable(companyCode, month);
            createTableMapper.createSubCdrTable(companyCode, month);
            createTableMapper.createChannelDataTable(companyCode, month);
            createTableMapper.createAcrRecordTable(vccId, month);
        } catch (Exception e) {
            log.error("[创建企业表] error: ", e);
        }
    }

}
