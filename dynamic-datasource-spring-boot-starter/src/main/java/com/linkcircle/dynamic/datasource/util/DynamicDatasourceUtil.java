package com.linkcircle.dynamic.datasource.util;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;

import java.util.function.Supplier;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/4/20 17:37
 */

public class DynamicDatasourceUtil {
    public static <T> T doExecute(String tenantId,Supplier<T> function){
        try{
            DynamicDataSourceContextHolder.push(tenantId);
            return function.get();
        }finally {
            DynamicDataSourceContextHolder.poll();
        }
    }
}
