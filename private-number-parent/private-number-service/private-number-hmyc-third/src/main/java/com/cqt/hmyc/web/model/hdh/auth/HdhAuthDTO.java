package com.cqt.hmyc.web.model.hdh.auth;

import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-04-13 15:00
 * hdh鉴权信息
 */
@Data
public class HdhAuthDTO {

    private String platformId;

    private String secret;

    private String appId;
}
