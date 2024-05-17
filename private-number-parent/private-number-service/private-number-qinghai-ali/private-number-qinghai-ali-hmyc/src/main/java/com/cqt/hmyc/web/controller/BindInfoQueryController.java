package com.cqt.hmyc.web.controller;

import com.cqt.common.util.TraceIdUtil;
import com.cqt.hmyc.web.service.BindInfoQueryService;
import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.call.vo.TaobaoBindInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * date:  2023-01-28 13:36
 */
@Api(tags = "查询绑定关系")
@RestController
@RequestMapping("bind")
@RequiredArgsConstructor
public class BindInfoQueryController {

    private final BindInfoQueryService bindInfoQueryService;

    @ApiOperation("查询绑定关系接口")
    @RequestMapping(value = "get", method = {RequestMethod.GET, RequestMethod.POST})
    public TaobaoBindInfoVO getBindInfo(@Validated BindInfoApiQuery bindInfoApiQuery) {
        try {
            TraceIdUtil.setTraceId(bindInfoApiQuery.getCallId());
            return bindInfoQueryService.getBindInfo(bindInfoApiQuery);
        } finally {
            TraceIdUtil.clear();
        }
    }

}
