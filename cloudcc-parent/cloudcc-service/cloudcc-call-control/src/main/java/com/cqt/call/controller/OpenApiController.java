package com.cqt.call.controller;

import com.cqt.base.annotations.Auth;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.model.client.base.ClientBase;
import com.cqt.model.client.dto.ClientCallDTO;
import com.cqt.model.client.dto.ClientPreviewOutCallDTO;
import com.cqt.rpc.call.CallControlRemoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * date:  2023-08-10 10:18
 */
@Api(tags = "外呼接口")
@RestController
@RequestMapping("openapi")
@RequiredArgsConstructor
public class OpenApiController {

    private final CallControlRemoteService callControlRemoteService;

    private final ObjectMapper objectMapper;

    @Auth
    @ApiOperation("话务接口入口")
    @PostMapping("call-request")
    public ClientBase request(@RequestBody String requestBody) throws Exception {
        return callControlRemoteService.request(requestBody);
    }

    @ApiOperation("预览外呼接口")
    @PostMapping("preview-out-call")
    public ClientBase previewOutCall(@RequestBody ClientPreviewOutCallDTO clientPreviewOutCallDTO) throws Exception {
        clientPreviewOutCallDTO.setMsgType(MsgTypeEnum.preview_out_call.name());
        return callControlRemoteService.request(objectMapper.writeValueAsString(clientPreviewOutCallDTO));
    }

    @ApiOperation("外呼接口(正常外呼/工单回拨/留言回呼)")
    @PostMapping("call")
    public ClientBase call(@RequestBody ClientCallDTO clientCallDTO) throws Exception {
        clientCallDTO.setMsgType(MsgTypeEnum.call.name());
        return callControlRemoteService.request(objectMapper.writeValueAsString(clientCallDTO));
    }

}
