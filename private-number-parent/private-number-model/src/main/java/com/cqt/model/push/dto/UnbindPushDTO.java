package com.cqt.model.push.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.cqt.model.common.BaseAuth;
import lombok.*;

import java.io.Serializable;

/**
 * @author hlx
 * @date 2021-11-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnbindPushDTO extends BaseAuth implements Serializable {

    private static final long serialVersionUID = 3392933289067565543L;

    /**
     * vccId 企业id  必选
     */
    @JSONField(name = "vcc_id")
    private String vccId;

    /**
     * 必选 解绑类型 axb axyb ax axbn(不进行序列化)
     */
    @JSONField(deserialize = false, serialize = false)
    private String numberType;

    /**
     * 必选	绑定关系请求id(号码绑定时传递请求Id)
     */
    @JSONField(name = "request_id")
    private String requestId;

    /**
     * 必选	绑定关系唯一id
     */
    @JSONField(name = "bind_id")
    private String bindId;

    /**
     * 必选	解绑时刻UNIXTIME，单位为秒
     */
    @JSONField(name = "unbind_time")
    private Long unbindTime;

    @JSONField(name = "tel_a")
    private String telA;

    @JSONField(name = "tel_b")
    private String telB;

    @JSONField(name = "area_code")
    private String areaCode;

    private String type;

    private String appId;

    /**
     * 该bindID中的tel_b_other，如果有追加绑定的号码，也需要加上，用英文逗号分隔
     */
    @JSONField(name = "tel_b_other")
    private String otherTelB;


}
