package com.cqt.hmyc.web.model.hdh.axb;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author huweizhong
 * date  2023/12/8 10:45
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HdhAxeBindDTO implements Serializable {

    private static final long serialVersionUID = -8056276578787752106L;

    /**
     * 应用 Id，标志出要操作的应用。
     * 若账户名下只有一个应用，本字段可选填。
     * 若不填此字段，则操作针对该应用。
     * 若账户名下有多个应用，本字段必填，否则
     * 会报错
     */
    private String appId;

    /**
     * 用于请求重试去重，每一组绑定关系该参数
     * 具有唯一性
     */
    private String requestId ;

    /**
     * 用户 A 号码: 18600008888或0108888999;
     */
    private String telA;

    /**
     * 小号区号
     */
    private Integer areaCode;

    /**
     * 绑定关系过期失效时间
     */
    private Integer expiration;

    /**
     * 是否对通话进行录音，：‘0’：否；‘1’：是；
     */
    private String record;

    /**
     *     回传参数
     */
    private String userData;


    /**
     *     放音编码
     */
    private JSON audio;

}
