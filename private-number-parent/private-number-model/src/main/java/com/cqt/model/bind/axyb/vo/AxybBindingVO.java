package com.cqt.model.bind.axyb.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2021/9/10 12:55
 * AXYB模式  号码绑定接口响应结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AxybBindingVO implements Serializable {

    private static final long serialVersionUID = 3217769365749378633L;

    /**
     * 虚拟号码X
     */
    @JsonProperty("tel_x")
    private String telX;

    /**
     * 虚拟号码Y
     */
    @JsonProperty("tel_y")
    private String telY;

    /**
     * 绑定关系ID
     */
    @JsonProperty("bind_id")
    private String bindId;
}
