package com.cqt.cdr.cloudccsfaftersales.entity.agent;

import cn.hutool.core.util.IdUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用excel导入结果
 *
 * @author scott
 * @date 2022年05月19日 10:10
 */
@Data
public class CommonImportResult implements Serializable {
    private static final long serialVersionUID = -5890340836046299476L;

    /**
     * 导入成功条数
     */
    private Integer successCount = 0;

    /**
     * 导入失败条数
     */
    private Integer failCount = 0;

    /**
     * 本次请求唯一标识
     */
    private String requestId;

    public Integer getTotalCount() {
        return this.successCount + this.failCount;
    }

    public CommonImportResult() {
        this.requestId = IdUtil.objectId();
    }

    public CommonImportResult(Integer successCount, Integer failCount) {
        this.requestId = IdUtil.objectId();
        this.successCount = successCount;
        this.failCount = failCount;
    }
}

