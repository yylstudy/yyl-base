package com.linkcircle.basecom.easyexcel;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.linkcircle.basecom.annotation.Dict;
import com.linkcircle.basecom.common.DictModel;
import com.linkcircle.basecom.config.ApplicationContextHolder;
import com.linkcircle.basecom.handler.DictHandler;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description String、Long、Integer字典类型转换
 * @createTime 2024/3/29 17:37
 */
public class DictConverter implements Converter<Object> {
    @Override
    public Class<?> supportJavaTypeKey() {
        return Object.class;
    }
    @Override
    public WriteCellData<?> convertToExcelData(Object value,ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        if(ObjectUtil.isEmpty(value)){
            return new WriteCellData();
        }
        Field field = contentProperty.getField();
        Dict dict = field.getDeclaredAnnotation(Dict.class);
        if(dict==null){
            throw new RuntimeException("请在字段上添加@Dict注解");
        }
        DictHandler dictHandler = ApplicationContextHolder.getBean(DictHandler.class);
        String dictCode = dict.dictCode();
        List<DictModel> dictModelList = dictHandler.getDictItemByDictCode(dictCode);
        String str = String.valueOf(value);
        String dictValue = dictModelList.stream().filter(item->str.equals(item.getItemValue()))
                .map(DictModel::getItemText).findFirst().orElse(str);
        return new WriteCellData(dictValue);
    }
}
