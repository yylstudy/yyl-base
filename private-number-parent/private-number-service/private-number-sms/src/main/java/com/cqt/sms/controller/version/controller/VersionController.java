package com.cqt.sms.controller.version.controller;


import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dong
 */
@RestController
@Slf4j
public class VersionController {

    /**
     * 获取版本信息接口
     */
    @GetMapping(value = "/get-version")
    public Object getVersion() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("version.json");
            Resource resource = resources[0];
            String version = IoUtil.readUtf8(resource.getInputStream());
            log.info("获取版本信息: {}", version);
            return version;
        }catch (Exception e) {
            log.error("获取版本信息异常: ", e);
        }
        return null;
    }
}
