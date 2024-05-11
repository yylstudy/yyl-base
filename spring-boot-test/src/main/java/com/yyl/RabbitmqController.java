//package com.yyl;
//
//import com.linkcircle.mq.producer.MqProducer;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.UUID;
//
///**
// * @author yang.yonglian
// * @version 1.0.0
// * @Description TODO
// * @createTime 2024/4/25 15:22
// */
//@RestController
//@Slf4j
//public class RabbitmqController {
//    @Autowired
//    private MqProducer mqProducer;
//
//    @RequestMapping("send2")
//    public void send1(){
//        SysUser sysUser = new SysUser();
//        sysUser.setUsernme("yyl");
//        sysUser.setSex(UUID.randomUUID().toString());
//        //正常消息
//        mqProducer.asyncSendMessage("normal_exchange:aaa",sysUser);
//        String key = UUID.randomUUID().toString();
//        log.info("key:{}",key);
//        sysUser = new SysUser();
//        sysUser.setUsernme("yyl");
//        sysUser.setSex(UUID.randomUUID().toString());
//        mqProducer.asyncSendMessage("normal_exchange:aaa",sysUser,key);
//        //延迟消息
//        sysUser = new SysUser();
//        sysUser.setUsernme("yyl");
//        sysUser.setSex(UUID.randomUUID().toString());
//        mqProducer.asyncSendDelayMessage("delay_exchange:aaa",sysUser,30000);
//        key = UUID.randomUUID().toString();
//        log.info("key:{}",key);
//        sysUser = new SysUser();
//        sysUser.setUsernme("yyl");
//        sysUser.setSex(UUID.randomUUID().toString());
//        mqProducer.asyncSendDelayMessage("delay_exchange:aaa",sysUser,30000,key);
//        //过期消息
//        sysUser = new SysUser();
//        sysUser.setUsernme("yyl");
//        sysUser.setSex(UUID.randomUUID().toString());
//        mqProducer.asyncSendExpireMessage("expire_exchange:aaa",sysUser,30000);
//        key = UUID.randomUUID().toString();
//        log.info("key:{}",key);
//        sysUser = new SysUser();
//        sysUser.setUsernme("yyl");
//        sysUser.setSex(UUID.randomUUID().toString());
//        mqProducer.asyncSendExpireMessage("expire_exchange:aaa",sysUser,30000,key);
//
//    }
//
//}
