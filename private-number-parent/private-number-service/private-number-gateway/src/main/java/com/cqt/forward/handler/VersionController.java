package com.cqt.forward.handler;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dong
 * @date 2020/6/1 11:11
 */
@RestController
@Slf4j
public class VersionController {

    private final static Map<String, String> VERSION_MAP = new HashMap<>();

    /**
     * 获取版本信息接口
     */
    @GetMapping(value = "/get-version")
    public String getVersion() throws IOException {
        if (CollUtil.isNotEmpty(VERSION_MAP)) {
            return VERSION_MAP.get("version");
        }
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("version.json");
        Resource resource = resources[0];
        String version = IoUtil.readUtf8(resource.getInputStream());
        VERSION_MAP.put("version", version);
        return version;
    }
}
