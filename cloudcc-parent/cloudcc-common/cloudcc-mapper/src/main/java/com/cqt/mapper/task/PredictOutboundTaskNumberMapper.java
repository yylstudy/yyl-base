package com.cqt.mapper.task;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.calltask.entity.PredictOutboundTaskNumber;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预测外呼任务-号码(PredictOutboundTaskNumber)表数据库访问层
 *
 * @author linshiqiang
 * @since 2023-10-27 10:35:00
 */
@Mapper
public interface PredictOutboundTaskNumberMapper extends BaseMapper<PredictOutboundTaskNumber> {

}

