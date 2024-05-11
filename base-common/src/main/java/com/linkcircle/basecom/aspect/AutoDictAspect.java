package com.linkcircle.basecom.aspect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.linkcircle.basecom.annotation.Dict;
import com.linkcircle.basecom.common.DictModel;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.constants.CommonConstant;
import com.linkcircle.basecom.handler.DictHandler;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.basecom.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
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
 * @Description TODO
 * @createTime 2024/4/1 11:02
 */
@Aspect
@Slf4j
@ConditionalOnClass(name="org.apache.ibatis.binding.MapperProxy")
@Component
public class AutoDictAspect {

    private static Map<Class,List<Field>> map = new ConcurrentHashMap<>();
    @Autowired
    private DictHandler dictHandler;
    public AutoDictAspect(){
        log.info("初始化：{}完成============================",this.getClass().getSimpleName());
    }

    @Pointcut("@annotation(com.linkcircle.basecom.annotation.AutoDict)" +
            "||@within(com.linkcircle.basecom.annotation.AutoDict)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        long start=System.currentTimeMillis();
        result=this.parseDictText(result);
        long end=System.currentTimeMillis();
        log.debug("注入字典到JSON数据  耗时"+(end-start)+"ms");
        return result;
    }

    public Object parseDictText(Object result){
        //只翻译分页列表的方法
        if (!(result instanceof Result)||!(((Result<?>) result).getData() instanceof PageResult)) {
            return result;
        }
        List<Object> records = ((PageResult) ((Result<?>) result).getData()).getList();
        //数据为空
        if(CollectionUtil.isEmpty(records)){
            return result;
        }
        List<Field> dictFields = getDictField(records);
        //不包含@Dict注解
        if(dictFields.isEmpty()){
            return result;
        }
        Map<String,List<DictModel>> dictCodeMap = new ConcurrentHashMap();
        List<Map<String,Object>> resultList = new ArrayList<>();
        for(Object record:records){
            String jsonStr = JsonUtil.toJSONString(record);
            Map<String,Object> map = JsonUtil.parseObject(jsonStr,Map.class);
            for(Field field:dictFields){
                Object dictFieldValue = ReflectionUtils.getField(field,record);
                //值为空，不翻译
                if(ObjectUtil.isEmpty(dictFieldValue)){
                    break;
                }
                Dict dict = field.getAnnotation(Dict.class);
                String dictCode = dict.dictCode();
                //字典空，不翻译
                if(!StringUtils.hasText(dictCode)){
                    break;
                }
                String dictText = dict.dictText();
                if(!StringUtils.hasText(dictText)){
                    dictText =  field.getName()+CommonConstant.DICT_TEXT_SUFFIX;
                }
                String finalDictText = dictText;
                List<DictModel> dictModels = dictCodeMap.computeIfAbsent(dictCode,key->
                        dictHandler.getDictItemByDictCode(dictCode));
                //字典翻译，如果未找到字典，则不翻译
                dictModels.stream().filter(dictModel ->dictModel.getItemValue().equals(String.valueOf(dictFieldValue)))
                        .map(DictModel::getItemText).findFirst().ifPresent(value->map.put(finalDictText,value));
            }
            resultList.add(map);
        }
        ((PageResult) ((Result<?>) result).getData()).setList(resultList);
        return result;
    }

    private List<Field> getDictField(List<Object> records){
        Class clazz = records.get(0).getClass();
        List<Field> dictFields = map.computeIfAbsent(clazz,key->{
            List<Field> fields = new ArrayList<>();
            ReflectionUtils.doWithFields(clazz,field -> {
                if(field.isAnnotationPresent(Dict.class)){
                    ReflectionUtils.makeAccessible(field);
                    fields.add(field);
                }
            });
            return fields;
        });
        return dictFields;
    }



}
