package com.cqt.wechat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.push.entity.PrivateFailMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hlx
 * @date 2021-09-15
 */
@Mapper
public interface FailMessageDao extends BaseMapper<PrivateFailMessage> {


}
