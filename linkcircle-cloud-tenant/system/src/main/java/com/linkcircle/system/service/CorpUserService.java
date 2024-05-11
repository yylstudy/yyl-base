package com.linkcircle.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linkcircle.system.entity.Corp;
import com.linkcircle.system.entity.CorpUser;

import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/2 22:31
 */

public interface CorpUserService extends IService<CorpUser> {
    List<Corp> getCorpByUserId(Long userId);
}
