package com.cqt.hmyc.web.dailtest.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author linshiqiang
 * @date 2021/10/11 14:19
 */
@FeignClient(name = "COMMAND-CHECK", fallbackFactory = CommandFeignFallbackFactory.class)
public interface CommandFeign {

    /**
     * 执行命令
     *
     * @param command 命令
     * @return String
     */
    @GetMapping("/COMMAND/checkCommand")
    String execute(@RequestParam("cmd") String command);
}
