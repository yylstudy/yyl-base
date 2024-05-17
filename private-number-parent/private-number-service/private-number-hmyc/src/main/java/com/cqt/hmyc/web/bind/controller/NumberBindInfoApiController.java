package com.cqt.hmyc.web.bind.controller;

import com.cqt.hmyc.web.bind.service.strategy.BindInfoApiService;
import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.common.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author linshiqiang
 * @since 2022-11-16 10:01
 * 对外查询绑定关系接口(绑定关系在本平台)
 */
@Api(tags = "对外查询绑定关系接口(绑定关系在本平台)")
@RestController
@RequestMapping("api/v1/bind")
@RequiredArgsConstructor
public class NumberBindInfoApiController {

    private final BindInfoApiService bindInfoApiService;


    @ApiOperation("对外查询绑定关系接口")
    @PostMapping("query/{vccId}")
    public ResultVO<BindInfoApiVO> queryBindInfo(@PathVariable("vccId") String vccId,
                                                 @RequestBody BindInfoApiQuery bindInfoApiQuery) {
        bindInfoApiQuery.setVccId(vccId);
        return bindInfoApiService.getBindInfo(bindInfoApiQuery);
    }

    @ApiOperation("查询绑定关系接口(内部)")
    @PostMapping("getBindInfo")
    public ResultVO<BindInfoApiVO> queryBindInfo(@RequestBody BindInfoApiQuery bindInfoApiQuery) {

        return bindInfoApiService.getBindInfo(bindInfoApiQuery);
    }

}
