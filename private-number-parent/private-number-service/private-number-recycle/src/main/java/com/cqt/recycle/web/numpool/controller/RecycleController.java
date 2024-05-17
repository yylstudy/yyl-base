package com.cqt.recycle.web.numpool.controller;

import com.cqt.recycle.web.numpool.job.CreateTableJob;
import com.cqt.recycle.web.numpool.job.NewDeleteUnusedPoolJob;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * @author linshiqiang
 * @date 2021/9/9 17:06
 */
@Api(tags = "AXB号码回收")
@RestController
@RequestMapping("recycle")
public class RecycleController {

    @Resource
    private CreateTableJob createTableJob;

    @Resource
    private NewDeleteUnusedPoolJob newDeleteUnusedPoolJob;

    @ApiOperation("创建绑定关系历史表:xxljob-createTableJobHandler")
    @PostMapping("createTable")
    public void createTable() throws JsonProcessingException {
        createTableJob.createTable();
    }

    @ApiOperation("AXB可用号码池redis回收:xxljob-recycleUsablePoolJobHandler")
    @PostMapping("recycleUsablePoolJobHandler")
    public void recycleUsablePoolJobHandler() {
        newDeleteUnusedPoolJob.recycleUsablePoolJobHandler();
    }

}
