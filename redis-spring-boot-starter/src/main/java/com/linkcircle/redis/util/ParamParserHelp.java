package com.linkcircle.redis.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/25 13:46
 */
@Slf4j
public class ParamParserHelp {
    private static final String startFlag = "#{";
    private static Map<Method,String[]> paramNameMap = new ConcurrentHashMap<>();
    /**
     * 参数名称解析器
     */
    private static LocalVariableTableParameterNameDiscoverer localVariableTableParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    public static String getRealContent(String content, Method method, Object[] args) {
        if (!content.contains(startFlag)) {
            return content;
        }
        String[] argsName = paramNameMap.computeIfAbsent(method,key->localVariableTableParameterNameDiscoverer.getParameterNames(method));
        EvaluationContext ctx = new StandardEvaluationContext();
        for(int i=0;i<argsName.length;i++){
            ctx.setVariable(argsName[i],args[i]);
        }
        ExpressionParser ep = new SpelExpressionParser();
        String newContent = (String)ep.parseExpression(content, new TemplateParserContext()).getValue(ctx);
        return newContent;
    }

    public static void main(String[] args) {
        ExpressionParser ep = new SpelExpressionParser();
        // 创建上下文变量
        EvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("alarmTime", "2018-09-26 13:00:00");
        ctx.setVariable("location", "二楼201机房");
        TestUser user = new TestUser();
        ctx.setVariable("user", user);
        Object obj = ep.parseExpression("告警发生时间 #{#alarmTime}，#{#user.username}，位置是在#{#location}", new TemplateParserContext()).getValue(ctx);
        System.out.println(obj.getClass());
        System.out.println();
        System.out.println(ep.parseExpression("#alarmTime").getValue(ctx));
    }
    @Data
    static class TestUser implements Serializable {
        private String username = "yyl";
    }
}
