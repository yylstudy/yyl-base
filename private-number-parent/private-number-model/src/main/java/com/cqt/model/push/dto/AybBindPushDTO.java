package com.cqt.model.push.dto;

import com.cqt.model.common.BaseAuth;
import lombok.*;

/**
 * @author linshiqiang
 * @date 2022/2/28 14:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AybBindPushDTO extends BaseAuth {

    /**
     * 企业id
     */
    private String vccId;

    /**
     * axyb绑定请求时传入的appId
     */
    private String appId;

    /**
     * 地市编码
     */
    private String areaCode;

    /**
     * axyb-ayb关系的绑定关系id
     */
    private String bindId;

    /**
     * 绑定时刻UNIXTIME，单位为秒
     */
    private Long bindTime;

    /**
     * axyb-ax绑定时的tel
     */
    private String telA;

    /**
     * 真实号码
     */
    private String telB;

    /**
     * axyb-ayb动态分配的虚拟号
     */
    private String telX;

    /**
     * axyb_ayb
     */
    private String type;

    /**
     * axyb_ax绑定时生成的bindID
     */
    private String sourceBindId;


}
