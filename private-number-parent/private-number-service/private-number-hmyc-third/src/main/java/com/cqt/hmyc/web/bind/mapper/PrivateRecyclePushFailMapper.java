package com.cqt.hmyc.web.bind.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.bind.entity.PrivateRecyclePushFail;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author linshiqiang
 * @date 2022/2/21 16:12
 * 号码回收 推mq延时队列异常, 保存数据
 */
@Mapper
public interface PrivateRecyclePushFailMapper extends BaseMapper<PrivateRecyclePushFail> {
}
