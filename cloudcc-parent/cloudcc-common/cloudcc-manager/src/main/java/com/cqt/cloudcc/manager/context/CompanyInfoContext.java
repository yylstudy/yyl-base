package com.cqt.cloudcc.manager.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.cqt.model.company.entity.CompanyInfo;

/**
 * @author linshiqiang
 * date:  2023-11-07 19:19
 */
public class CompanyInfoContext {

    private static final TransmittableThreadLocal<CompanyInfo> THREAD_LOCAL = new TransmittableThreadLocal<>();

    public static void set(CompanyInfo companyInfo) {
        THREAD_LOCAL.set(companyInfo);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

    public static CompanyInfo get() {
        return THREAD_LOCAL.get();
    }
}
