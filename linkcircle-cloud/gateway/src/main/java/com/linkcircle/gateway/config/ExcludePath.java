package com.linkcircle.gateway.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/5/6 13:52
 */
@Component
public class ExcludePath {
    private volatile static List<String> allPathList;
    @Autowired
    private ExcludePathConfig excludePathConfig;

    public List<String> getAllPathList(){
        if(allPathList==null){
            synchronized (ExcludePath.class){
                if(allPathList==null){
                    allPathList = new ArrayList();
                    addDefaultExcludePath(allPathList);
                    allPathList.addAll(excludePathConfig.getPath());
                }
            }
        }
        return allPathList;
    }

    private void addDefaultExcludePath(List<String> list){
        list.add("/system/login/getCaptcha");
        list.add("/system/login");
        list.add("/system/tokenVerify");
        list.add("/system/doc.html");
        list.add("/system/webjars/css/**");
        list.add("/system/webjars/js/**");
        list.add("/system/favicon.ico");
        list.add("/**/v3/api-docs/**");
    }
}
