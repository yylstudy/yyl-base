package com.yyl;//package com.yyl;

import cn.hutool.core.util.IdUtil;
import com.linkcircle.basecom.annotation.OperateLog;
import com.linkcircle.basecom.annotation.SignCheck;
import com.linkcircle.basecom.filter.LoginUserInfoHolder;
import com.linkcircle.basecom.util.NoticeUtil;
import com.linkcircle.redis.annotation.Lock;
import com.linkcircle.redis.annotation.RepeatSubmit;
import com.yyl.mapper.TestMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/13 14:28
 */
@RestController
@Slf4j
public class MyController2 {
    @Autowired
    private CorpMapStruct corpMapStruct;
    @Autowired
    private SysDictService sysDictService;

    @OperateLog(content = "测试操作")
    @RequestMapping("get")
    @SignCheck
    public String get(@Valid @RequestBody SysUser sysUser){
        MyLoginUserInfo myLoginUserInfo = LoginUserInfoHolder.get();
        log.info("myLoginUserInfo:{}",myLoginUserInfo);
        return "success";
    }

    @RequestMapping("testLock")
    @Lock(lockKey = "aaaaaaaaaaa:#{#sysUser.sex}")
    public String testLock(@RequestBody SysUser sysUser){
        try {
            log.info("testLock");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "success";
    }

    @RequestMapping("testRepeatSubmit")
    @RepeatSubmit(lockTime = 10,lockKey = "testRepeatSubmit:#{#sysUser.sex}",keyAppendUserId = true)
    public String testRepeatSubmit(@RequestBody SysUser sysUser){
        try {
            Thread.sleep(5000);
            log.info("testRepeatSubmit");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "success";
    }

    @RequestMapping("test")
    public String set(){
//        NoticeUtil.sendMail("1594818954@qq.com","测试主题","测试内容");
        NoticeUtil.sendSms("15255178553","测试短信");

        return "success";
    }

    @RequestMapping("testEncry")
    public String testEncry(){
        SysDict sysDict = new SysDict();
        sysDict.setId(IdUtil.getSnowflakeNextId());
        sysDict.setDictCode("15255178554");
        sysDict.setDictName("15255178554");
        sysDict.setCreateTime(new Date());
        sysDictService.save(sysDict);
        List<SysDict> list = new ArrayList<>();
        sysDict = new SysDict();
        sysDict.setId(IdUtil.getSnowflakeNextId());
        sysDict.setDictCode("15255178553");
        sysDict.setDictName("15255178553");
        sysDict.setCreateTime(new Date());
        list.add(sysDict);
        sysDict = new SysDict();
        sysDict.setId(IdUtil.getSnowflakeNextId());
        sysDict.setDictCode("15255178552");
        sysDict.setDictName("15255178552");
        sysDict.setCreateTime(new Date());
        list.add(sysDict);
        sysDict = new SysDict();
        sysDict.setId(IdUtil.getSnowflakeNextId());
        sysDict.setDictCode("15255178551");
        sysDict.setDictName("15255178551");
        sysDict.setCreateTime(new Date());
        list.add(sysDict);
        sysDictService.saveBatch(list);
        return "success";
    }

    @RequestMapping("testdencry")
    public List<SysDict> testdencry(){
        List<SysDict> sysDict = sysDictService.list();
        log.info("sysDict:{}",sysDict);
        return sysDict;
    }

    @RequestMapping("testdelete")
    public String testdelete(){
        List<SysDict> sysDict = sysDictService.list();
        sysDictService.removeById(sysDict.get(0));
        return "success";
    }

    @RequestMapping("testupdate")
    public String testupdate(){
        List<SysDict> sysDict = sysDictService.list();
        sysDict.get(0).setDictName("15255178559");
        sysDict.get(0).setDictCode("test");
        sysDictService.updateById(sysDict.get(0));
        sysDict = sysDictService.list();
        sysDict.get(0).setDictName("15255178555");
        sysDict.get(0).setDictCode("test5");
        sysDictService.updateBatchById(sysDict);
        return "success";
    }
    @Autowired
    private TestService testService;
    @RequestMapping("testSharding")
    public String testSharding(){
        List<Test> list = new ArrayList<>();
        Test test = new Test();
        test.setId(IdUtil.getSnowflakeNextId());
        test.setName(UUID.randomUUID().toString());
        test.setCreateTime(new Date());
        list.add(test);
        test = new Test();
        test.setId(IdUtil.getSnowflakeNextId());
        test.setName(UUID.randomUUID().toString());
        test.setCreateTime(new Date());
        list.add(test);
        test = new Test();
        test.setId(IdUtil.getSnowflakeNextId());
        test.setName(UUID.randomUUID().toString());
        test.setCreateTime(new Date());
        list.add(test);

        testService.saveBatch(list);
        return "success";
    }


}
