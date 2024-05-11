package com.alibaba.otter.manager.deployer;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/3/31 14:44
 */

public enum PipelineStatus {
    UNWORK("未工作"),POSITIONING("定位中"),WORKING("工作中");
    private String message;

    PipelineStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
