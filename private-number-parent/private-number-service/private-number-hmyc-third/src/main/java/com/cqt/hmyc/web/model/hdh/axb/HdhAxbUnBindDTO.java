package com.cqt.hmyc.web.model.hdh.axb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HdhAxbUnBindDTO implements Serializable {

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
     *     第三方绑定id
     */
    private String bindId;



}
