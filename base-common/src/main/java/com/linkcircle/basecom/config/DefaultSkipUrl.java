package com.linkcircle.basecom.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/2/29 15:17
 */

public class DefaultSkipUrl {
    private static List<String> skipUrl = new ArrayList<>();
    static {
        skipUrl.add("/login/getCaptcha");
        skipUrl.add("/login");
        //knife4j
        skipUrl.add("/doc.html");
        skipUrl.add("/webjars/css/**");
        skipUrl.add("/webjars/js/**");
        skipUrl.add("/favicon.ico");
        skipUrl.add("/v3/api-docs/**");
    }

    public static List<String> getSkipUrl() {
        return skipUrl;
    }
}
