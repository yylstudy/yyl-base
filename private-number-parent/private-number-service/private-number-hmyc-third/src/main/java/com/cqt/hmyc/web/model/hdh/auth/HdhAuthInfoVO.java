package com.cqt.hmyc.web.model.hdh.auth;

import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-04-13 14:43
 */
@Data
public class HdhAuthInfoVO {

    /**
     * 请求url
     */
    private String url;

    /**
     * 鉴权header值
     */
    private String header;

    private HdhAuthDTO hdhAuthDTO;
}
