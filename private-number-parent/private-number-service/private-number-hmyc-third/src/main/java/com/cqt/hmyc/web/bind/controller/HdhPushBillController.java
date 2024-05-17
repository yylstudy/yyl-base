package com.cqt.hmyc.web.bind.controller;

import com.cqt.common.constants.SystemConstant;
import com.cqt.hmyc.web.bind.service.hdh.HdhPushService;
import com.cqt.hmyc.web.bind.service.hdh.HdhSmsPushService;
import com.cqt.hmyc.web.model.hdh.push.HdhPushIccpDTO;
import com.cqt.model.common.ThirdPushResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;


/**
 * @author linshiqiang
 * @date 2021/9/9 14:53
 */
@Api(tags = "hdh 话单接收接口")
@RestController
@RequestMapping(SystemConstant.THIRD_CDR_URI)
public class HdhPushBillController {

    private final HdhPushService pushService;

    private final HdhSmsPushService smsPushService;

    public HdhPushBillController(HdhPushService pushService, HdhSmsPushService smsPushService) {
        this.pushService = pushService;
        this.smsPushService = smsPushService;
    }


    /**
     * （hdh）通话话单接收
     *
     */
    @ApiOperation("接收通话话单")
    @PostMapping("bill/receive")
    public ThirdPushResult push(@RequestBody HdhPushIccpDTO hdhPushIccpDTO) {
      return  pushService.hdhPush(hdhPushIccpDTO);
    }

    /**
     * （hdh）短信话单接收
     *
     */
    @ApiOperation("接收短信话单")
    @PostMapping("sms/receive")
    public ThirdPushResult smsPush(@RequestBody HdhPushIccpDTO hdhPushIccpDTO) {
        return  smsPushService.hdhSmsPush(hdhPushIccpDTO);
    }

}
