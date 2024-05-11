package com.yyl;

import com.linkcircle.mq.common.RocketmqLocalTransactionState;
import com.linkcircle.mq.common.RocketmqSendCallback;
import com.linkcircle.mq.producer.MqProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/25 15:22
 */
@RestController
@Slf4j
public class RocketmqOrderlyController {
    @Autowired
    private MqProducer mqProducer;

    @RequestMapping("send3")
    public void send1(){
        SysUser sysUser = new SysUser();
        sysUser.setUsernme("yyl1");
        sysUser.setSex(UUID.randomUUID().toString());

        mqProducer.asyncSendOrderlyMessage("order_topic:aaa",sysUser,"1");
        String key = UUID.randomUUID().toString();
        log.info("key:{}",key);
        sysUser = new SysUser();
        sysUser.setUsernme("yyl2");
        sysUser.setSex(UUID.randomUUID().toString());
        mqProducer.asyncSendOrderlyMessage("order_topic:aaa", sysUser, "1", key, new RocketmqSendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("sendResult:{}",sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.info("onException:{}",e);
            }
        });

        sysUser = new SysUser();
        sysUser.setUsernme("yyl3");
        sysUser.setSex(UUID.randomUUID().toString());
        boolean syncResult = mqProducer.syncSendOrderlyMessage("order_topic:aaa",sysUser,"1");
        log.info("syncResult:{}",syncResult);

        key = UUID.randomUUID().toString();
        log.info("key:{}",key);
        sysUser = new SysUser();
        sysUser.setUsernme("yyl4");
        sysUser.setSex(UUID.randomUUID().toString());
        syncResult = mqProducer.syncSendOrderlyMessage("order_topic:aaa",sysUser,"1",key);
        log.info("syncResult:{}",syncResult);
    }

}
