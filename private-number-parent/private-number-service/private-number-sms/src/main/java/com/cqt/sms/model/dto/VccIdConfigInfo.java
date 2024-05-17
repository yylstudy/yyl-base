package com.cqt.sms.model.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.security.SecureRandom;

/**
 * @author youngder
 * @description 企业配置信息实体
 * @date 2022/2/24 2:20 PM
 */
@Data
@TableName(value = "private_vcc_info")
public class VccIdConfigInfo {
    private String vccId;
    private String secretKey;
    private String extNumCount;
    private String bindQueryUrl;
    private String bindConvertUrl;
    private String masterNum;
    private String billPushUrl;
    private String smsPushUrl;


}
