package com.cqt.feign.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cqt.cdr.cloudccsfaftersales.entity.agent.*;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

@RefreshScope
@FeignClient(name = "AUTH-SERVICE", url = "http://172.16.250.218/jeecgboot/aftersale/")
public interface AfterSaleClient {


    /**
     * 新增坐席
     *
     * @param addDTO
     * @return
     */
    @PostMapping("/sys/agent-info/add")
    Result<CommonImportResult> addAgent(AgentInfoAddDTO addDTO,@RequestHeader("X-Tenant-Id") String tennantId);

    /*
     * 编辑坐席
     * */
    @PutMapping("/sys/agent-info/edit")
    Result<Void> edit(@Validated @RequestBody AgentInfoEditDTO editDTO,@RequestHeader("X-Tenant-Id") String tennantId);


    /*
     * 删除坐席
     * */
    @DeleteMapping("/sys/agent-info/delete-batch")
    Result<Void> batchDelete(@NotBlank(message = "请至少选择一条记录") String ids,@RequestHeader("X-Tenant-Id") String tennantId);

    /*
     * 检索坐席
     * */
    @GetMapping("/sys/agent-info/list")
    Result<IPage<AgentInfoQueryVO>> pageQuery(AgentInfoQueryDTO queryDTO,@RequestHeader("X-Tennant-Id") String tennantId);



    /*
     * 更新技能组
     * */


    /*
     * 新增技能组
     * */


}