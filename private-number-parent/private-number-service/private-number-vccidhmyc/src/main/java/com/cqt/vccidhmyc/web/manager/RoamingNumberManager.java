package com.cqt.vccidhmyc.web.manager;

import com.cqt.vccidhmyc.config.cache.RoamingNumberCache;
import com.cqt.vccidhmyc.web.mapper.VlrMsrnInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-03-27 14:34
 * 漫游号缓存设置
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoamingNumberManager implements InitializingBean {

    private final VlrMsrnInfoMapper vlrMsrnInfoMapper;

    @Override
    public void afterPropertiesSet() {
        List<String> msrnList = vlrMsrnInfoMapper.getMsrnList();
        log.info("查询到漫游号数量: {}", msrnList.size());
        RoamingNumberCache.addAll(msrnList);
        log.info("查询到漫游号缓存数量: {}", RoamingNumberCache.size());
    }
}
