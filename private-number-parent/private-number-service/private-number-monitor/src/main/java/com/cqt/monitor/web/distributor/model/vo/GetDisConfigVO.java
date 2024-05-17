package com.cqt.monitor.web.distributor.model.vo;

import lombok.Data;

/**
 * @author linshiqiang
 * @since 2022-12-02 10:52
 */
@Data
public class GetDisConfigVO {

    private String serverIp;

    private String content;

    private Boolean success;

    public GetDisConfigVO(String serverIp, String content, Boolean success) {
        this.serverIp = serverIp;
        this.content = content;
        this.success = success;
    }

    public static GetDisConfigVO setVO(String serverIp, String content, Boolean success) {

        return new GetDisConfigVO(serverIp, content, success);
    }
}
