package com.cqt.model.bind.axbn.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2022/3/29 19:13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AxbnAppendTelVO implements Serializable {

    private static final long serialVersionUID = 1039974094574126926L;

    /**
     * A号码
     */
    @JsonProperty("tel_a")
    private String telA;

    /**
     * 虚号个数对应tel_b_other号码的个数，中间以英文逗号分隔
     * 例如：
     * tel_b_other = 186xxxx0001,186xxxx0002
     * tel_y = 176xxxx0008,176xxxx0009
     * tel_a呼叫号码0008转接到0001
     * tel_a呼叫号码0009转接到 0002
     */
    @JsonProperty("tel_y")
    private String telY;

    /**
     * 绑定关系ID
     */
    @JsonProperty("bind_id")
    private String bindId;

    /**
     * 地市编码
     */
    @JsonProperty("area_code")
    private String areaCode;
}
