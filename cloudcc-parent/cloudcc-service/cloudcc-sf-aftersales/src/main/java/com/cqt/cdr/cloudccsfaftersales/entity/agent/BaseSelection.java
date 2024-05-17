/*
 * Copyright (c) 2022. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.cqt.cdr.cloudccsfaftersales.entity.agent;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * BaseSelection
 *
 * @author Xienx
 * @date 2022年12月21日 10:53
 */
@Data
public class BaseSelection implements Serializable {

    private static final long serialVersionUID = 1524004316290756842L;

    /**
     * 勾选的内容, 多条以逗号分隔
     */
    private String selections;

    /**
     * 将selection参数转换成set集合
     */
    public Set<String> getSelectedSet() {
        if (StrUtil.isBlank(selections)) {
            return Collections.emptySet();
        }
        return new HashSet<>(StrUtil.split(selections, StrUtil.C_COMMA));
    }
}
