package com.linkcircle.basecom.handler;

import com.linkcircle.basecom.common.DictModel;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 字段服务
 * @createTime 2024/3/29 17:56
 */
public interface DictHandler {
    /**
     * 根据指点编码获取字典项
     * @param dictCode
     * @return
     */
    List<DictModel> getDictItemByDictCode(String dictCode);
}
