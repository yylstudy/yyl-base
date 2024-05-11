package com.linkcircle.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linkcircle.system.entity.Corp;
import com.linkcircle.system.entity.CorpUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Mapper
public interface CorpUserMapper extends BaseMapper<CorpUser> {
    List<Corp> getCorpByUserId(Long userId);
}
