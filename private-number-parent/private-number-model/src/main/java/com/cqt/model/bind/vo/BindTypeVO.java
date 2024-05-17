package com.cqt.model.bind.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * date:  2022-12-20 16:58
 * 接口类型 绑定, 解绑, 修改
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BindTypeVO {

    /**
     * 地市编码
     */
    private String areaCode;

    /**
     * 绑定id
     */
    private String bindId;

    /**
     * 接口是否为设置绑定接口
     */
    private Boolean isBinding;
}
