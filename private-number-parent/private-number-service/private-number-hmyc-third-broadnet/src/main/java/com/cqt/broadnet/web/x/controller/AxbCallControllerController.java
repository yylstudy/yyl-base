package com.cqt.broadnet.web.x.controller;

import com.cqt.broadnet.common.model.x.vo.CallControlResponseVO;
import com.cqt.broadnet.config.Auth;
import com.cqt.broadnet.web.x.service.ApiStrategyFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author linshiqiang
 * date:  2023-02-15 14:27
 */
@Api(tags = "呼叫控制接口API")
@RestController
@RequiredArgsConstructor
public class AxbCallControllerController {

    private final ApiStrategyFactory apiStrategyFactory;

    @Auth
    @PostMapping("call")
    public CallControlResponseVO execute(@RequestParam Map<String, Object> params) throws JsonProcessingException {

        return apiStrategyFactory.execute(params);
    }
}
