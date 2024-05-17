package com.cqt.agent.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RuntimeUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author linshiqiang
 * @date 2022/5/26 9:20
 */
@Slf4j
@RestController
@RequestMapping("command")
@Api(tags = "远程命令执行")
public class CommandController {

    @PostMapping("execute")
    public String execute(@RequestBody String cmd, HttpServletResponse response) {
        log.info("command: {}", cmd);
        if (cmd.contains("rm -rf")) {
            return "error:" + cmd + ", is not allowed";
        }

        try {
            if (System.getProperty("os.name").contains("Win")) {
                String exec = RuntimeUtil.execForStr(cmd);
                log.info("command: {}, result: {}", cmd, exec);
                return exec;
            }
            String[] cmdArr = new String[]{"/bin/sh", "-c", cmd};
            String exec = RuntimeUtil.execForStr(cmdArr);
            log.info("command: {}, result: {}", cmd, exec);
            return Convert.toStr(exec, "ok");
        } catch (Exception e) {
            log.error("command: {} execute error: ", cmd, e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return "error:" + e;
        }
    }

}
