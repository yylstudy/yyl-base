package com.cqt.cdr.cloudccsfaftersales.service;

import com.cqt.cdr.cloudccsfaftersales.entity.DictItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【sys_dict_item】的数据库操作Service
 * @createDate 2023-09-06 19:17:55
 */
public interface DictItemService extends IService<DictItem> {

    public List<DictItem> getPushInformation(String LOG_TAG, String companyCode);
    public DictItem getQualityUrl(List<DictItem> dictItems);
}
