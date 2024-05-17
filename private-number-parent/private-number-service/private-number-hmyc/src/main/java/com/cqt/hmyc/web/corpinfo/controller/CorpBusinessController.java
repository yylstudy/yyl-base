package com.cqt.hmyc.web.corpinfo.controller;

import com.alibaba.nacos.api.exception.NacosException;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/2/24 10:42
 */
@Api(tags = "企业信息")
@RestController
@RequestMapping("api/v1/corp-business-info")
public class CorpBusinessController {

    private final CorpBusinessService corpBusinessService;

    public CorpBusinessController(CorpBusinessService corpBusinessService) {
        this.corpBusinessService = corpBusinessService;
    }

    /**
     * 企业信息同步刷新
     */
    @PostMapping("refresh")
    public void refresh() throws NacosException {

        corpBusinessService.refresh();
    }

    /**
     * 查询企业业务配置
     */
    @GetMapping()
    public Optional<PrivateCorpBusinessInfoDTO> getVccInfo(String vccId) {

        return corpBusinessService.getCorpBusinessInfo(vccId);
    }

    /**
     * 删除 企业业务配置缓存
     */
    @PostMapping("delLocalVccInfo/{vccId}")
    public Boolean delLocalVccInfo(@PathVariable("vccId") String vccId) {

        return corpBusinessService.delLocalVccInfo(vccId);
    }

}
