package com.cqt.sdk.client.controller;

import com.cqt.base.model.ResultVO;
import com.cqt.model.agent.dto.AgentNotifyDTO;
import com.cqt.rpc.sdk.SdkInterfaceRemoteService;
import com.cqt.sdk.client.job.CreateTableJob;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-08-09 18:27
 */
@Api(tags = "企业坐席变动通知")
@RestController
@RequestMapping("notify")
@RequiredArgsConstructor
public class AgentNotifyController {

    private final SdkInterfaceRemoteService sdkInterfaceRemoteService;

    private final CreateTableJob createTableJob;

    @ApiOperation("坐席被删除-调用通知")
    @PostMapping("agent-delete")
    public ResultVO<Void> agentDeleteNotify(@RequestBody List<AgentNotifyDTO> agentNotifyList) {
        sdkInterfaceRemoteService.agentChangeNotify(agentNotifyList);
        return ResultVO.ok();
    }

    @ApiOperation("企业创建通知")
    @PostMapping("company-create/{companyCode}")
    public ResultVO<Void> companyCreateNotify(@PathVariable("companyCode") String companyCode) {
        createTableJob.createCompanyTable(companyCode);
        return ResultVO.ok();
    }

    @ApiOperation("取消事后处理任务(本服务自调用)")
    @PostMapping("cancelArrangeTask")
    public Boolean cancelArrangeTask(String companyCode, String agentId) {
        return sdkInterfaceRemoteService.cancelArrangeTask(companyCode, agentId);
    }
}
