package com.yyl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/9 11:38
 */
@RestController
@Slf4j
public class MyController3 {
    AtomicLong atomicLong = new AtomicLong(0);
    @RequestMapping("test3")
    public String test3(){
        if(atomicLong.incrementAndGet()%1000==0){
            log.info("===========================");
        }
        return "success";
    }
}
