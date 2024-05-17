package com.cqt.mapper.task;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.calltask.entity.PredictOutboundTaskAgent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预测外呼任务-坐席(PredictOutboundTaskAgent)表数据库访问层
 *
 * @author linshiqiang
 * @since 2023-10-27 13:54:04
 */
@Mapper
public interface PredictOutboundTaskAgentMapper extends BaseMapper<PredictOutboundTaskAgent> {

}

