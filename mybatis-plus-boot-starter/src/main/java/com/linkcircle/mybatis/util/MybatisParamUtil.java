package com.linkcircle.mybatis.util;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/12 11:36
 */

public class MybatisParamUtil {
    /**
     * 获取需要加密的参数对象，有可能参数对象中存在多个Entity，但是是同一个对象，这样也只要一个就可以了
     * @param entity
     * @return
     */
    public static List<Object> getParamObjectList(Object entity){
        List<Object> list = new ArrayList();
        //mapper方法参数为多个或者（参数个数为1且类型为Collection或者数组）时，具体代码查看ParamNameResolver#getNamedParams方法
        if(entity instanceof MapperMethod.ParamMap){
            Collection collection = ((MapperMethod.ParamMap<?>) entity).values();
            for(Object param:collection){
                if(param ==null){
                    continue;
                }
                //参数为1一个且类型为Collection
                if (param instanceof Collection) {
                    ((Collection<?>) param).stream().forEach(s->addIfNotSameObject(list,param));
                }
                //参数为1一个且类型为数组
                else if (param.getClass().isArray()) {
                    Object[] objArray = (Object[])param;
                    for(Object obj:objArray){
                        addIfNotSameObject(list,obj);
                    }
                }else{
                    addIfNotSameObject(list,param);
                }
            }
        }else{
            addIfNotSameObject(list,entity);
        }
        return list;
    }

    /**
     * 对象相等时，不添加
     * @param list
     * @param obj
     */
    public static void addIfNotSameObject(List<Object> list,Object obj){
        boolean exists = list.stream().filter(a->a==obj).findFirst().isPresent();
        if(!exists){
            list.add(obj);
        }
    }

    /**
     * 是否写入命令
     * @param sqlCommandType
     * @return
     */
    public static boolean isWriteCommand(SqlCommandType sqlCommandType){
        return sqlCommandType==SqlCommandType.INSERT||sqlCommandType==SqlCommandType.UPDATE;
    }


}
