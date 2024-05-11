package com.linkcircle.basecom.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/29 17:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictModel {
    /**
     * 编码
     */
    private String itemValue;
    /**
     * 名称
     */
    private String itemText;
}
