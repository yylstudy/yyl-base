package com.alibaba.otter.manager.deployer;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/3/31 14:41
 */

public enum ChannelStatus {
    RUNNING("运行"),PAUSE("挂起"),STOP("停止");
    private String message;

    ChannelStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
