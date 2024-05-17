package com.cqt.model.bind.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2021/9/10 12:55
 * AXB 设置绑定响应结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddBindAxbVO implements Serializable {

    private static final long serialVersionUID = 3217769365749378633L;

    /**
     * 虚拟号码
     */
    @JsonProperty("tel_x")
    private String telX;

    /**
     * 绑定关系ID
     */
    @JsonProperty("bind_id")
    private String bindId;
}
