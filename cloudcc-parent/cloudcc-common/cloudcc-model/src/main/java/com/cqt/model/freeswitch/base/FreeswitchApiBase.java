package com.cqt.model.freeswitch.base;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 9:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FreeswitchApiBase implements Serializable {

    private static final long serialVersionUID = 3216909658236707407L;

    /**
     * 请求唯一性ID
     */
    @JsonProperty("req_id")
    private String reqId;

    /**
     * 业务编码，用于区分业务
     */
    @JsonProperty("service_code")
    private String serviceCode;

    /**
     * 否  | 后续需要桥接的通话所在的ACD服务ID，不带随机分配。
     */
    @JsonProperty("server_id")
    private String serverId;

    /**
     * 企业id
     */
    @JsonProperty("company_code")
    private String companyCode;

    /**
     * 构建底层通用请求对象
     *
     * @param companyCode 企业id
     * @return 底层通用请求对象
     */
    public static FreeswitchApiBase build(String companyCode) {
        FreeswitchApiBase base = new FreeswitchApiBase();
        base.setCompanyCode(companyCode);
        base.setReqId(IdUtil.fastUUID());
        return base;
    }
}
