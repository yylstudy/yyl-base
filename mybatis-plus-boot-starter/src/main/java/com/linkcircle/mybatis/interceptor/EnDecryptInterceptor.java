package com.linkcircle.mybatis.interceptor;

import com.linkcircle.mybatis.annotation.FieldEncrypt;
import com.linkcircle.mybatis.constant.CommonConstant;
import com.linkcircle.mybatis.encrypt.EnDecryptField;
import com.linkcircle.mybatis.encrypt.EnDecryptor;
import com.linkcircle.mybatis.util.MybatisParamUtil;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 加解密拦截器，切在prepare上批量插入只会执行第一条，具体原因未知，所以这里放在update
 * @createTime 2024/4/11 17:01
 */
@Intercepts({@Signature(type= Executor.class,method = CommonConstant.ENCRYPT_METHOD,args = {MappedStatement.class,Object.class}),
        @Signature(type= ResultSetHandler.class,method = CommonConstant.DECRYPT_METHOD,args = {Statement.class})})
public class EnDecryptInterceptor implements Interceptor {
    private Logger log = LoggerFactory.getLogger(EnDecryptInterceptor.class);
    /**
     * 类加密字段缓存
     */
    private static Map<Class, List<EnDecryptField>> classEncryptFieldMap = new ConcurrentHashMap<>();
    /**
     * 加解密器缓存
     */
    private static Map<Class, EnDecryptor> encryptorMap = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if(CommonConstant.ENCRYPT_METHOD.equals(method.getName())){
            //加密参数
            encryptParam(invocation);
            return invocation.proceed();
        }else{
            //解密结果集
            return decryptResult(invocation);
        }
    }

    /**
     * 参数加密
     * @param invocation
     * @throws Throwable
     */
    private void encryptParam(Invocation invocation) throws Throwable{
        Object[] args = invocation.getArgs();
        Object entity = args[1];
        MappedStatement mappedStatement = (MappedStatement)args[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if(entity==null||!MybatisParamUtil.isWriteCommand(sqlCommandType)){
            return ;
        }
        List<Object> encryptObjects = MybatisParamUtil.getParamObjectList(entity);
        for(Object encryptObject:encryptObjects){
            List<EnDecryptField> enDecryptFieldList = getEncryptField(encryptObject.getClass());
            if(!enDecryptFieldList.isEmpty()){
                String before = encryptObject.toString();
                encryptFieldValue(enDecryptFieldList,encryptObject);
                log.info("执行数据加密操作,加密前：{}，加密后：{}",before,encryptObject.toString());
            }
        }
    }

    /**
     * 解密结果集
     * @param invocation
     * @return
     * @throws Throwable
     */
    private List<Object>  decryptResult(Invocation invocation) throws Throwable{
        List<Object> results = (List<Object>)invocation.proceed();
        if(results.isEmpty()){
            return results;
        }
        List<EnDecryptField> decryptFieldList = getEncryptField(results.get(0).getClass());
        if(decryptFieldList.isEmpty()){
            return results;
        }
        for(Object result:results){
            String before = result.toString();
            for(EnDecryptField enDecryptField :decryptFieldList){
                Field field = enDecryptField.getField();
                FieldEncrypt fieldEncryptAnnotation = enDecryptField.getFieldEncryptAnnotation();
                EnDecryptor enDecryptor = getEncryptor(fieldEncryptAnnotation.encryptor());
                String rawValue = (String)field.get(result);
                if(StringUtils.hasText(rawValue)){
                    field.set(result, enDecryptor.decrypt(rawValue));
                }
            }
            log.info("执行数据解密操作,解密前：{}，解密后：{}",before,result.toString());
        }
        return results;
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 获取加密的属性
     * @param clazz
     * @return
     */
    public static List<EnDecryptField> getEncryptField(Class clazz){
        return classEncryptFieldMap.computeIfAbsent(clazz,key->{
            List<EnDecryptField> list = new ArrayList<>();
            ReflectionUtils.doWithFields(clazz, field -> {
                FieldEncrypt fieldEncrypt = field.getAnnotation(FieldEncrypt.class);
                if(fieldEncrypt!=null&&field.getType()==String.class){
                    ReflectionUtils.makeAccessible(field);
                    EnDecryptField enDecryptField = new EnDecryptField(field,fieldEncrypt);
                    list.add(enDecryptField);
                }
            });
            return list;
        });
    }

    /**
     * 加密属性值
     * @param encryptObject
     */
    private void encryptFieldValue(List<EnDecryptField> enDecryptFieldList, Object encryptObject) throws Exception{
        for(EnDecryptField enDecryptField : enDecryptFieldList){
            Field field = enDecryptField.getField();
            String rawValue = (String)field.get(encryptObject);
            if(!StringUtils.hasText(rawValue)){
                continue;
            }
            FieldEncrypt fieldEncryptAnnotation = enDecryptField.getFieldEncryptAnnotation();
            EnDecryptor enDecryptor = getEncryptor(fieldEncryptAnnotation.encryptor());
            field.set(encryptObject, enDecryptor.encrypt(rawValue));
        }
    }

    /**
     * 获取加密器
     * @return
     */
    private EnDecryptor getEncryptor(Class<? extends EnDecryptor> clazz){
        return encryptorMap.computeIfAbsent(clazz,t-> {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


}
