package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.system.entity.CorpBusiness;
import com.linkcircle.system.mapper.CorpBusinessMapper;
import com.linkcircle.system.service.CorpBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Slf4j
@Service
public class CorpBusinessServiceImpl extends ServiceImpl<CorpBusinessMapper, CorpBusiness> implements CorpBusinessService {
    @Override
    public List<String> getCorpIdByBusiness(String business) {
        LambdaQueryWrapper<CorpBusiness> wrapper = Wrappers.lambdaQuery();
        wrapper.select(CorpBusiness::getCorpId);
        wrapper.eq(CorpBusiness::getBusiness,business);
        return listObjs(wrapper,o->(String)o);
    }
}
