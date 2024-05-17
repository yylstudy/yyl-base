package com.cqt.push.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.push.entity.PrivateFailMessage;
import org.springframework.stereotype.Repository;

/**
 * @author hlx
 * @date 2021-09-15
 */
@Repository
public interface FailMessageDao extends BaseMapper<PrivateFailMessage> {



}
