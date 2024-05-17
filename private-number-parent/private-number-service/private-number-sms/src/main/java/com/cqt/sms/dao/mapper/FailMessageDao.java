package com.cqt.sms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.push.entity.PrivateFailMessage;
import org.springframework.stereotype.Repository;

@Repository
public interface FailMessageDao extends BaseMapper<PrivateFailMessage> {


}