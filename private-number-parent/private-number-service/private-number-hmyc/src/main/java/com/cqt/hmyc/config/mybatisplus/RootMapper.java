package com.cqt.hmyc.config.mybatisplus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2021/6/28 16:38
 * 根Mapper，给表Mapper继承用的，可以自定义通用方法
 * {@link BaseMapper}
 * {@link com.baomidou.mybatisplus.extension.service.IService}
 * {@link com.baomidou.mybatisplus.extension.service.impl.ServiceImpl}
 */
public interface RootMapper<T> extends BaseMapper<T> {


    /**
     * 自定义批量插入
     * 如果要自动填充，@Param(xx) xx参数名必须是 list/collection/array 3个的其中之一
     */
    int insertBatch(@Param("list") List<T> list);

    /**
     * 自定义批量更新，条件为主键
     * 如果要自动填充，@Param(xx) xx参数名必须是 list/collection/array 3个的其中之一
     */
    int updateBatch(@Param("list") List<T> list);

}
