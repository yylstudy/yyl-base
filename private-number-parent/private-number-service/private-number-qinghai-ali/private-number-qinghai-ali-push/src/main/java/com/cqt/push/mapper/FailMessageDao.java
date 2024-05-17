package com.cqt.push.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.push.entity.PrivateFailMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhengsuhao
 * @date 2023-02-16
 */
@Mapper
public interface FailMessageDao extends BaseMapper<PrivateFailMessage> {

}
