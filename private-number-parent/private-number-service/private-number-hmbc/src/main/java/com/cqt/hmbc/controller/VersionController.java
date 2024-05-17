package com.cqt.hmbc.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * @since 2022/6/24 9:54
 */
@Slf4j
@RestController
@Api(tags = "版本号接口")
public class VersionController {

    private final static Map<String, String> VERSION_MAP = new HashMap<>(1);

    private final static String VERSION = "version";

    /**
     * 获取版本信息接口
     */
    @ApiOperation(value = "版本查询接口", notes = "版本查询接口")
    @GetMapping(value = "/get-version")
    public String getVersion() throws IOException {
        if (StrUtil.isEmpty(VERSION_MAP.get(VERSION))) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("version.json");
            Resource resource = resources[0];
            String version = IoUtil.readUtf8(resource.getInputStream());
            VERSION_MAP.put(VERSION, version);
        }
        return VERSION_MAP.get(VERSION);
    }
}
