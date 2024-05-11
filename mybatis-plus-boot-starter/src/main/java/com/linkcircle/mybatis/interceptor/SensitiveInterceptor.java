package com.linkcircle.mybatis.interceptor;

import cn.hutool.json.JSONUtil;
import com.linkcircle.mybatis.annotation.FieldSensitive;
import com.linkcircle.mybatis.constant.CommonConstant;
import com.linkcircle.mybatis.sensitive.SensitiveField;
import com.linkcircle.mybatis.sensitive.SensitiveStrategy;
import com.linkcircle.mybatis.util.MybatisParamUtil;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 脱敏拦截器，切在prepare上批量插入只会执行第一条，具体原因未知，所以这里放在update
 * @createTime 2024/4/11 17:01
 */
@Intercepts({@Signature(type= Executor.class,method = CommonConstant.SENSITIVE_METHOD,args = {MappedStatement.class,Object.class})})
public class SensitiveInterceptor implements Interceptor {
    private Logger log = LoggerFactory.getLogger(SensitiveInterceptor.class);
    /**
     * 类脱敏字段映射
     */
    private static Map<Class, List<SensitiveField>> classSensitiveFieldMap = new ConcurrentHashMap<>();
    /**
     * 脱敏器映射
     */
    private static Map<Class, SensitiveStrategy> sensitiveStrategyMap = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        Object entity = args[1];
        MappedStatement mappedStatement = (MappedStatement)args[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if(entity==null||!MybatisParamUtil.isWriteCommand(sqlCommandType)){
            return invocation.proceed();
        }
        List<Object> sensitiveObjects = MybatisParamUtil.getParamObjectList(entity);
        for(Object sensitiveObject:sensitiveObjects){
            List<SensitiveField> sensitiveFields = getSensitiveField(sensitiveObject.getClass());
            if(!sensitiveFields.isEmpty()){
                String beforeJson = JSONUtil.toJsonStr(sensitiveObject);
                sensitiveFieldValue(sensitiveFields,sensitiveObject,sqlCommandType);
                log.info("执行数据脱敏操作,脱敏前：{}，脱敏后：{}",beforeJson,JSONUtil.toJsonStr(sensitiveObject));
            }
        }
        return invocation.proceed();
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 获取脱敏的属性
     * @param clazz
     * @return
     */
    public static List<SensitiveField> getSensitiveField(Class clazz){
        return classSensitiveFieldMap.computeIfAbsent(clazz,key->{
            List<SensitiveField> list = new ArrayList<>();
            ReflectionUtils.doWithFields(clazz, field -> {
                FieldSensitive fieldEncrypt = field.getAnnotation(FieldSensitive.class);
                if(fieldEncrypt!=null&&field.getType()==String.class){
                    ReflectionUtils.makeAccessible(field);
                    SensitiveField sensitiveField = new SensitiveField(field,fieldEncrypt);
                    list.add(sensitiveField);
                }
            });
            return list;
        });
    }

    /**
     * 脱敏属性值
     * @param encryptObject
     */
    private void sensitiveFieldValue(List<SensitiveField> sensitiveFields, Object encryptObject,SqlCommandType sqlCommandType) throws Exception{
        for(SensitiveField sensitiveField : sensitiveFields){
            Field field = sensitiveField.getField();
            String rawValue = (String)field.get(encryptObject);
            if(!StringUtils.hasText(rawValue)){
                continue;
            }
            //更新方法，且包含*表示已脱敏
            if(sqlCommandType==SqlCommandType.UPDATE&&rawValue.contains("*")){
                continue;
            }
            FieldSensitive fieldSensitive = sensitiveField.getFieldSensitive();
            SensitiveStrategy sensitiveStrategy = getSensitiveStrategy(fieldSensitive.strategy());
            field.set(encryptObject,sensitiveStrategy.handle(rawValue));
        }
    }

    /**
     * 获取脱敏策略
     * @return
     */
    private SensitiveStrategy getSensitiveStrategy(Class<? extends SensitiveStrategy> clazz){
        return sensitiveStrategyMap.computeIfAbsent(clazz,t-> {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


}
