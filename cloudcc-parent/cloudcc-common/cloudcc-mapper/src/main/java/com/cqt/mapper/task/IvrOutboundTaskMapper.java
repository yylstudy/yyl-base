package com.cqt.mapper.task;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.calltask.entity.IvrOutboundTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author linshiqiang
 * date:  2023-10-24 14:40
 */
@Mapper
public interface IvrOutboundTaskMapper extends BaseMapper<IvrOutboundTask> {

    /**
     * 更新任务状态
     *
     * @param taskId    任务id
     * @param taskState 任务状态 1-草稿 2-暂停  3-启用  4-已结束
     */
    @Update("update cloudcc_ivr_outbound_task set task_state = #{taskState} where task_id = #{taskId}")
    void updateTaskState(@Param("taskId") String taskId, @Param("taskState") Integer taskState);

    /**
     * 更新xxl job 任务id
     *
     * @param taskId 任务id
     * @param jobId  xxljob 任务id
     */
    @Update("update cloudcc_ivr_outbound_task set job_id = #{jobId} where task_id = #{taskId}")
    Integer updateJobId(@Param("taskId") String taskId, @Param("jobId") Integer jobId);
}
