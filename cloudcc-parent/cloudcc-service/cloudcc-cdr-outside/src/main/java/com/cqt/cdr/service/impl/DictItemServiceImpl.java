package com.cqt.cdr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.cdr.entity.Dict;
import com.cqt.cdr.entity.DictItem;
import com.cqt.cdr.mapper.DictMapper;
import com.cqt.cdr.service.DictItemService;
import com.cqt.cdr.mapper.DictItemMapper;
import com.cqt.cdr.service.PushErrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Administrator
 * @description 针对表【sys_dict_item】的数据库操作Service实现
 * @createDate 2023-08-30 10:10:46
 */
@Service
@Slf4j
public class DictItemServiceImpl extends ServiceImpl<DictItemMapper, DictItem>
        implements DictItemService {

    @Resource
    private DictMapper dictMapper;

    @Resource
    private PushErrService pushErrService;


    public DictItem isHaveDictItem(String message, String LOG_TAG, String month, String companyCode) {
        Dict dict = dictMapper.selectOne(new QueryWrapper<Dict>().lambda().eq(Dict::getDictCode, "cdr_push").eq(Dict::getTenantId, "000000"));
        List<DictItem> dictItems = baseMapper.selectList(new QueryWrapper<DictItem>().lambda().eq(DictItem::getDictId, dict.getId()).groupBy(DictItem::getItemText));
        if (dictItems.isEmpty() || dict == null) {
            log.info(LOG_TAG + "字典没有配置发送路径，消息：{}，存入数据库", message);
            pushErrService.errIntoErrTable(message, null, "2", month, companyCode, LOG_TAG);
            return null;
        }
        DictItem dictItem = null;
        for (DictItem item : dictItems) {
            if (companyCode.equals(item.getItemText())) {
                dictItem = item;
            }
        }
        return dictItem;
    }
}




