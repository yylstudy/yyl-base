package com.cqt.hmyc.web.model.hdh.axb;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;

/**
 * hdh AXB模式  号码绑定接口参数
 *
 * @author dingsh
 * @since 2022-07-06
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HdhAxbBindDTO implements Serializable {

    private static final long serialVersionUID = -8056276556567752106L;

    /**
     * 应用 Id，标志出要操作的应用。
     * 若账户名下只有一个应用，本字段可选填。
     * 若不填此字段，则操作针对该应用。
     * 若账户名下有多个应用，本字段必填，否则
     * 会报错
     */
    private String appId;

    /**
     * 用户 A 号码: 18600008888或0108888999;
     */
    private String telA;

    /**
     * 用户 B 号码；18600008888或0108888999,
     */
    private String telB;


    /**
     * 中间号码
     */
    private String telX;


    /**
     * 用于请求重试去重，每一组绑定关系该参数
     * 具有唯一性
     */
    private String requestId ;

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
     *     private String 来电号码是否显示中间号;
     *     00：互相拨打时都显示中间号（默认）
     *     10：A 为主叫时，B 的来显为 A 号码；B 为主叫时，A 的来显为中间号
     *     01：B 为主叫时，A 的来显为 B 号码；A 为主叫时，B 的来显为中间号
     *     11：互相拨打都显示对方真实号码
     */
    private String GNFlag;


    /**
     *     回传参数
     */
    private String userData;


    /**
     *     放音编码
     */
    private JSON audio;



}
