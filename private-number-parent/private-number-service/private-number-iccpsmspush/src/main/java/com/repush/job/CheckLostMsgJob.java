package com.repush.job;

import cn.hutool.core.util.StrUtil;
import com.repush.dao.domain.LostMsg;
import com.repush.service.LockService;
import com.repush.service.SmsService;
import com.repush.util.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author ffy
 * 上行长短信拆分丢失检查
 */
@Service
@Log4j2
public class CheckLostMsgJob {

    private static final String TABLE_NAME = "sms_sdr_";
    private static final String LONG_MESSAGE_SET = "long_message_set";
    private static final String LONG_MESSAGE = "long_message_";
    //日志文件路径
    private static String LOGPATH = "/home/smp/logs";
    private static Boolean STATE = true;
    @Resource
    SmsService smsService;
    @Resource
    private JedisCluster jedisCluster;
    @Resource
    private LockService lockService;

    //每分钟检查是否有长短信拆分丢失
    @Scheduled(cron = "0/60 * * * * ?")
    public void checkLostMsg() {
        String localIp = StringUtil.getLocalIp();
        try {
            if (lockService.lock(localIp)) {
                Set<String> longMsgSet = jedisCluster.smembers(LONG_MESSAGE_SET);
                log.info("长短信丢失定时器");
                for (String longMsgKey : longMsgSet) {
                    log.info("==" + longMsgKey);
                    LostMsg lostMsg = new LostMsg();
                    if (jedisCluster.ttl(longMsgKey) == -2) {
                        jedisCluster.srem(LONG_MESSAGE_SET, longMsgKey);
                        log.info("长短信丢失");
                        //长短信丢失
                        lostMsg.setId(StrUtil.uuid());
                        //主叫号码
                        String caller = longMsgKey.split("_")[0];
                        lostMsg.setCaller(caller);
                        String imsi = longMsgKey.split("_")[1];
                        lostMsg.setImsi(imsi);
                        String msgId = longMsgKey.split("_")[2];
                        lostMsg.setMsgId(msgId);
                        //查分总条数
                        String splitNum = longMsgKey.split("_")[3];
                        lostMsg.setTotalNum(splitNum);
                        String content = "";
                        String redisContent = "";
                        String lostNum = "";
                        for (int i = 1; i < Integer.parseInt(splitNum) + 1; i++) {
                            redisContent = jedisCluster.get(LONG_MESSAGE + caller + "_" + imsi + "_" + msgId + i);
                            jedisCluster.del(LONG_MESSAGE + caller + "_" + imsi + "_" + msgId + i);
                            if (StrUtil.isEmpty(redisContent)) {
                                lostNum += i;
                            }
                            content = content + redisContent;
                        }
                        lostMsg.setContent(content);
                        lostMsg.setLostNum(lostNum);
                        smsService.saveLostMsg(lostMsg);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("unKnow Exceptin");
        } finally {
            lockService.unlock(localIp);
        }
    }
}
