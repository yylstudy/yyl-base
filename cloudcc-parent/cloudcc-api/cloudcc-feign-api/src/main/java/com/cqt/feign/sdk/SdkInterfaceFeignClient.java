package com.cqt.feign.sdk;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

/**
 * @author linshiqiang
 * date:  2023-09-11 14:36
 */
@FeignClient(name = "cloudcc-sdk-interface", path = "cloudcc-sdk-interface")
public interface SdkInterfaceFeignClient {

    /**
     * 取消事后处理任务
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 是否成功
     */
    @PostMapping("/notify/cancelArrangeTask")
    Boolean cancelArrangeTask(URI uri, @RequestParam String companyCode, @RequestParam String agentId);

    /**
     * 取消事后处理任务
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 是否成功
     */
    @PostMapping("/notify/cancelArrangeTask")
    Boolean cancelArrangeTask(@RequestParam String companyCode, @RequestParam String agentId);
}
