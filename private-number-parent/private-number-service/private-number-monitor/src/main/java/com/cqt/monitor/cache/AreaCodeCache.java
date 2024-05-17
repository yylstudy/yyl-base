package com.cqt.monitor.cache;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cqt.monitor.web.callevent.entity.AreaTable;
import com.cqt.monitor.web.callevent.mapper.AreaTableMapper;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/6/7 13:40
 */
@Component
@AllArgsConstructor
public class AreaCodeCache {

    private final AreaTableMapper areaTableMapper;

    private static final ConcurrentHashMap<String, String> AREA_CODE_CACHE = new ConcurrentHashMap<>(256);

    @PostConstruct
    @Scheduled(cron = "0 0 10,14,16 * * ?")
    public void init() {
        List<AreaTable> list = areaTableMapper.selectList(Wrappers.<AreaTable>lambdaQuery()
                .select(AreaTable::getTelCode, AreaTable::getDetails));
        list.forEach(areaTable -> {
            if (StrUtil.isNotEmpty(areaTable.getTelCode()) && StrUtil.isNotEmpty(areaTable.getDetails())) {
                AREA_CODE_CACHE.put(areaTable.getTelCode(), areaTable.getDetails());
            }
        });
    }

    /**
     * 获取区号对应的区域名称
     *
     * @param areaCode 区号
     * @return 区号对应的区域名称
     */
    public static String getAreaName(String areaCode) {
        if (StrUtil.isEmpty(areaCode)) {
            return "";
        }
        return AREA_CODE_CACHE.get(areaCode);
    }

    /**
     * 判断给定的区号是否在平台存在
     *
     * @param areaCode 区号
     * @return 区号是否存在
     */
    public static boolean areaCodeIsExist(String areaCode) {
        return AREA_CODE_CACHE.containsKey(areaCode);
    }

    public static Map<String, String> all() {
        return AREA_CODE_CACHE;
    }
}
