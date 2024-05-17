package com.cqt.hmyc.web.bind.controller;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.cqt.hmyc.web.bind.job.NumberRecycleJob;
import com.cqt.hmyc.web.cache.SyncNumberPoolJob;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author linshiqiang
 * @date 2021/9/9 17:06
 */
@Api(tags = "测试接口")
@RestController
@RequestMapping("test")
public class TestController {

    @Resource
    private SyncNumberPoolJob syncNumberPoolJob;

    @Resource
    private NumberRecycleJob recycleJob;

    @ApiOperation("生成接口鉴权-sign值")
    @PostMapping("createSign/{secretKey}")
    public String createSign(@RequestBody TreeMap<String, Object> params, @PathVariable("secretKey") String secretKey) {

        return createSign(params, "", secretKey);
    }

    public String createSign(TreeMap<String, Object> params, String vccId, String secretKey) {
        params.remove("appkey");
        params.remove("sign");
        params.remove("vcc_id");
        List<String> paramList = new ArrayList<>();
        params.forEach((key, value) -> {
            if (ObjectUtil.isNotEmpty(value)) {
                paramList.add(key + "=" + StrUtil.removeAll(value.toString(), CharUtil.CR, CharUtil.LF, CharUtil.SPACE));
            }
        });
        paramList.add("secret_key=" + secretKey);
        return SecureUtil.md5(String.join("&", paramList)).toUpperCase();
    }

    @ApiOperation("AXB 扫描数据库回收号码")
    @PostMapping("dealAxbRecycleFail")
    public void dealAxbRecycleFail() {
        recycleJob.dealAxbRecycleFailJobHandler();
    }

    @ApiOperation("AXE 扫描数据库回收号码")
    @PostMapping("dealAxeRecycleFail")
    public void dealAxeRecycleFail() {
        recycleJob.dealAxeRecycleFailJobHandler();
    }

    @ApiOperation("AX 扫描数据库回收号码")
    @PostMapping("dealAxRecycleFail")
    public void dealAxRecycleFail() {
        recycleJob.dealAxRecycleFailJobHandler();
    }

    @ApiOperation("处理发送mq失败的绑定关系数据(mysql)")
    @PostMapping("dealPushMqFailDataJobHandler")
    public void dealPushMqFailDataJobHandler() {
        recycleJob.dealPushMqFailDataJobHandler();
    }

    @ApiOperation("检查AXE分机号是否有回收失败")
    @PostMapping("checkAxeExtensionNumber")
    public void checkAxeExtensionNumber() {
        recycleJob.checkAxeExtensionNumber();
    }

    @ApiOperation("检查AXE分机号是否有回收失败-单个号码")
    @PostMapping("checkAxeExtensionNumberSingle")
    public void checkAxeExtensionNumberSingle(String vccId, String areaCode, String number) {
        PrivateNumberInfo numberInfo = new PrivateNumberInfo();
        numberInfo.setVccId(vccId);
        numberInfo.setAreaCode(areaCode);
        numberInfo.setNumber(number);
        recycleJob.dealExtensionSingle(numberInfo);
    }


    @ApiOperation("刷新JVM本地号码池缓存")
    @PostMapping("refreshNumberPool")
    public void refreshNumberPool() {
        syncNumberPoolJob.refreshNumberPool();
    }

}
