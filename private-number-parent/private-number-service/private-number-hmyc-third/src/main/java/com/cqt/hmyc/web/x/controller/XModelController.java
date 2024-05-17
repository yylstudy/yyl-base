package com.cqt.hmyc.web.x.controller;

import com.cqt.hmyc.web.x.model.*;
import com.cqt.hmyc.web.x.service.XModelBillService;
import com.cqt.hmyc.web.x.service.XModelBindQueryService;
import com.cqt.hmyc.web.x.service.XModelStatusService;
import com.cqt.model.common.ThirdPushResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * date:  2023-06-07 14:56
 */
@RestController
@RequestMapping("api/v1/x")
@RequiredArgsConstructor
@Api(tags = "X模式")
public class XModelController {

    private final XModelStatusService xmodelStatusService;
    private final XModelBindQueryService xModelBindQueryService;

    private final XModelBillService xModelBillService;

    @PostMapping("status")
    public ThirdPushResult status(@RequestBody XModelStatusDTO xmodelStatusDTO) throws JsonProcessingException {

        return xmodelStatusService.status(xmodelStatusDTO);
    }

    @ApiOperation(value = "呼转控制接口")
    @PostMapping("queryBindInfo")
    public XModelQueryVO call(@RequestBody XModelQueryDTO queryDTO) {
        return xModelBindQueryService.query(queryDTO);
    }

    @ApiOperation(value = "话单接收接口")
    @PostMapping("bill")
    public ThirdPushResult bill(@RequestBody XModelBillDTO billDTO) throws JsonProcessingException {
        return xModelBillService.bill(billDTO);
    }

    @PostMapping("record")
    public ThirdPushResult record(@RequestBody XModelRecordDTO recordDTO) throws JsonProcessingException {
        return xModelBillService.record(recordDTO);
    }
}
