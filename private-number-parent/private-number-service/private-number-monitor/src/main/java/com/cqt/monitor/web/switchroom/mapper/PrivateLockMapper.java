package com.cqt.monitor.web.switchroom.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.bind.entity.PrivateLock;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author linshiqiang
 * @date 2022/1/24 16:44
 * mysql 锁
 */
@Mapper
@DS("ms")
public interface PrivateLockMapper extends BaseMapper<PrivateLock> {
}
