package com.cqt.cdr.cloudccsfaftersales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.cdr.cloudccsfaftersales.entity.Dict;
import com.cqt.cdr.cloudccsfaftersales.entity.DictItem;
import com.cqt.cdr.cloudccsfaftersales.entity.PushErr;
import com.cqt.cdr.cloudccsfaftersales.mapper.DictMapper;
import com.cqt.cdr.cloudccsfaftersales.service.DictItemService;
import com.cqt.cdr.cloudccsfaftersales.mapper.DictItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description 针对表【sys_dict_item】的数据库操作Service实现
 * @createDate 2023-09-06 19:17:55
 */
@Service
@Slf4j
public class DictItemServiceImpl extends ServiceImpl<DictItemMapper, DictItem>
        implements DictItemService {
    @Resource
    private DictMapper dictMapper;

    @Override
    public DictItem getQualityUrl(List<DictItem> dictItems) {
        DictItem item = null;
        for (DictItem dictItem : dictItems) {
            if ("quality".equals(dictItem.getItemText())) {
                item = dictItem;
            }
        }
        return item;
    }

    @Override
    public List<DictItem> getPushInformation(String LOG_TAG, String companyCode) {
        Dict dict = dictMapper.selectOne(new QueryWrapper<Dict>().lambda().eq(Dict::getDictCode, "cdr_push").eq(Dict::getTenantId, companyCode));
        List<DictItem> dictItems = new ArrayList<>();
        if (dict != null) {
            dictItems = baseMapper.selectList(new QueryWrapper<DictItem>().lambda().eq(DictItem::getDictId, dict.getId()).groupBy(DictItem::getItemText));
        }
        log.info(LOG_TAG + "字典没有配置发送路径");
        return dictItems;
    }

}




