package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.system.entity.Corp;
import com.linkcircle.system.entity.CorpUser;
import com.linkcircle.system.mapper.CorpUserMapper;
import com.linkcircle.system.service.CorpUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Slf4j
@Service
public class CorpUserServiceImpl extends ServiceImpl<CorpUserMapper, CorpUser> implements CorpUserService {
    @Autowired
    private CorpUserMapper corpUserMapper;

    @Override
    public List<Corp> getCorpByUserId(Long userId) {
        return corpUserMapper.getCorpByUserId(userId);
    }
}
