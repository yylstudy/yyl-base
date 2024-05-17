package com.cqt.cdr.interceptor;

/**
 * 由于运维需求，数据库异常后输出sql语句，引入TheadLocal
 * 用于异步多线程写数据库时，存sql语句，保持线程安全
 * <p>
 * 用完要记得finally里reset，防止内存泄漏
 *
 * @author lightonyang
 * @date 2022/10/31
 */
public class TheadLocalUtil {

    private static final ThreadLocal<TheadLocalUtil> LOCAL = ThreadLocal.withInitial(TheadLocalUtil::new);

    private String sql;

    private TheadLocalUtil() {
    }

    public static TheadLocalUtil instance() {
        return LOCAL.get();
    }

    public TheadLocalUtil reset() {
        sql = null;
        LOCAL.remove();
        return this;
    }

    /*用于链式添加*/
    public TheadLocalUtil sql(String sql) {
        this.sql = sql;
        return this;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}

