package com.cqt.monitor.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.cloud.api.basesetting.BaseSettingFeignClient;
import com.cqt.common.enums.MessageTypeEnum;
import com.cqt.model.common.MessageDTO;
import com.cqt.model.common.Result;
import com.cqt.monitor.cache.AreaCodeCache;
import com.cqt.monitor.web.callevent.entity.PlatProperty;
import com.cqt.monitor.web.callevent.entity.PrivateWarningConfig;
import com.cqt.monitor.web.callevent.entity.WarningConfig;
import com.cqt.monitor.web.callevent.mapper.PrivateSupplierInfoMapper;
import com.cqt.monitor.web.callevent.mapper.PrivateWarningConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author hlx
 * @since 2021-09-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DingUtil {

    private static final String IP = NetUtil.getLocalhostStr();

    private final BaseSettingFeignClient baseSettingFeignClient;

    private final PlatProperty platProperty;

    private final PrivateWarningConfigMapper warningConfigMapper;

    private final PrivateSupplierInfoMapper supplierInfoMapper;


    /**
     * 发送钉钉群消息
     *
     * @param message 消息内容
     */
    public void sendMessage(String message) {
        try {
            String msgTemp = "【倒换监控告警】\n告警时间: %s\n当前设备: %s\n告警内容: %s\n";
            String msg = String.format(msgTemp, DateUtil.now(), IP, message);

            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setType(MessageTypeEnum.dingding.name());
            messageDTO.setGroup("private");
            messageDTO.setOperateType("倒换监控告警");
            messageDTO.setContent(msg);
            Result sendMessage = baseSettingFeignClient.sendMessage(messageDTO);
            log.info("dingding response: {}", sendMessage);
        } catch (Exception e) {
            log.error("send dingding message error: ", e);
        }
    }


    /**
     * 发送钉钉群消息
     */
    public void sendWarnMessage(WarningConfig warningConfig, String num, String content, String areaCode, String supplierId) {
        PrivateWarningConfig privateWarningConfig = new PrivateWarningConfig();
        privateWarningConfig.setId(warningConfig.getId());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        privateWarningConfig.setWarnTime(dateFormat.format(new Date()));
        warningConfigMapper.updateById(privateWarningConfig);
        String supplierName = supplierInfoMapper.getSupplierName(supplierId);
        try {
            String msg = "";
            String vciname = warningConfig.getVccName() + "(" + warningConfig.getVccId() + ")";
            if ("nj".equals(platProperty.getFormValue())) {
                vciname = "南京平台-" + vciname;
            } else {
                vciname = "扬州平台-" + vciname;
            }
            Map<String, String> all = AreaCodeCache.all();
            String s = all.get(areaCode);
            vciname = vciname + "-" + s;
            if (StringUtils.isNotEmpty(num)) {
                msg = StrUtil.format("【通用隐私号告警】\n告警规则: {}\n告警时间: {}\n当前设备: {}\n告警企业: {}\n告警供应商: {}\n告警号码: {}\n告警内容: {}\n明细: {}\n", warningConfig.getWarnName(), DateUtil.now(), IP, vciname, supplierName, num, warningConfig.getWarningContent(), content);
            } else {
                msg = StrUtil.format("【通用隐私号告警】\n告警规则: {}\n告警时间: {}\n当前设备: {}\n告警企业: {}\n告警供应商: {}\n告警内容: {}\n明细: {}\n", warningConfig.getWarnName(), DateUtil.now(), IP, vciname, supplierName, warningConfig.getWarningContent(), content);
            }
            log.info("告警内容：" + msg);
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setContent(msg);
            if (warningConfig.getWarningWay().contains("0")) {
                messageDTO.setType("dingding");
                messageDTO.setGroup("private");
                messageDTO.setOperateType("话务监控告警");
                Result result = baseSettingFeignClient.sendMessage(messageDTO);
                log.info("钉钉告警结果: {}", result);
            }
            if (warningConfig.getWarningWay().contains("1")) {
                messageDTO.setType("email");
                messageDTO.setTo(warningConfig.getEmailReceiver());
                messageDTO.setOperateType("话务监控告警");
                Result result = baseSettingFeignClient.sendMessage(messageDTO);
                log.info("邮件告警结果: {}", result);
            }


        } catch (Exception e) {
            log.error("send dingding message error: ", e);
        }
    }

}

