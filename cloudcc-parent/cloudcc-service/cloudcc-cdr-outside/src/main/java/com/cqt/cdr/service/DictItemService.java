package com.cqt.cdr.service;

import com.cqt.cdr.entity.DictItem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Administrator
 * @description 针对表【sys_dict_item】的数据库操作Service
 * @createDate 2023-08-30 10:10:46
 */
public interface DictItemService extends IService<DictItem> {
    public DictItem isHaveDictItem(String message, String LOG_TAG, String month, String companyCode);
}
