package com.cqt.model.bind.axebn.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2021/9/10 12:55
 * AXEBN模式 响应参数 requestId
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AxebnBindingVO implements Serializable {

    private static final long serialVersionUID = -4815811557965500655L;

    /**
     * 虚拟号码
     */
    @JsonProperty("tel_x")
    private String telX;

    /**
     * 分机号
     */
    @JsonProperty("tel_x_ext")
    private String telXExt;

    /**
     * 绑定关系ID
     */
    @JsonProperty("bind_id")
    private String bindId;
}
