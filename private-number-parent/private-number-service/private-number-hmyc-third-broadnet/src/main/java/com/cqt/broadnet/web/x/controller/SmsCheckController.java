package com.cqt.broadnet.web.x.controller;

import com.cqt.broadnet.common.model.x.dto.SmsCheckDTO;
import com.cqt.broadnet.common.model.x.vo.SmsCheckVO;
import com.cqt.broadnet.web.x.service.SmsCheckService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * date:  2023-04-26 14:55
 */
@Api(tags = "短信统一接口API")
@RestController
@RequiredArgsConstructor
public class SmsCheckController {

    private final SmsCheckService smsCheckService;

    @PostMapping("sms")
    public SmsCheckVO check(@RequestBody String smsCheckDTO) throws JsonProcessingException {

        return smsCheckService.check(smsCheckDTO);
    }
}
