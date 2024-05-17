package com.cqt.model.bind.axbn.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2021/9/10 12:55
 * AXBN模式  号码绑定接口响应结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AxbnBindingVO implements Serializable {

    private static final long serialVersionUID = 3217769365749378633L;

    /**
     * 虚号；所有B号码呼叫tel_x都能转接到tel_a号码, tel_a呼叫tel_x转接到tel_b号码
     */
    @JsonProperty("tel_x")
    private String telX;

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
}
