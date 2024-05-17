package com.cqt.cdr.interceptor;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.PreparedStatementLogger;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.sql.Statement;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
})
@Component
public class MybatisInterceptor implements Interceptor {


    private String getSql(String sql) {
        int index = sql.toUpperCase().indexOf("INSERT");
        if (index > -1) {
            return sql.substring(index).replaceAll("\\s+", " ");
        }
        return sql;
    }


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Statement statement = (Statement) invocation.getArgs()[0];
        String sql = null;
        if (Proxy.isProxyClass(statement.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(statement);
            PreparedStatementLogger psl = (PreparedStatementLogger) metaObject.getValue("h");
            sql = getSql(psl.getPreparedStatement().toString());
            System.out.println(sql);
        } else {
            sql = getSql(statement.toString());
        }
        TheadLocalUtil.instance().sql(sql);
        return invocation.proceed();
    }
}
