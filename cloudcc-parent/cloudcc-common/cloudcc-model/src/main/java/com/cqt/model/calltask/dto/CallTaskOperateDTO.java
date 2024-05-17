package com.cqt.model.calltask.dto;

import com.cqt.base.enums.calltask.CallTaskEnum;
import com.cqt.model.calltask.entity.IvrOutboundTask;
import com.cqt.model.calltask.entity.PredictOutboundTask;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author linshiqiang
 * date:  2023-10-25 14:48
 * 任务操作参数
 */
@Data
public class CallTaskOperateDTO {

    @ApiModelProperty("任务id")
    @NotEmpty(message = "[taskId]不能为空")
    private String taskId;

    /**
     * xxl job 任务id
     * 更新, 删除 启动 暂停 必传
     */
    private Integer jobId;

    /**
     * @see CallTaskEnum
     */
    @ApiModelProperty("任务类型: IVR, PREDICT_TASK")
    @NotEmpty(message = "[taskType]不能为空")
    private String taskType;

    private IvrOutboundTask ivrInfo;

    private PredictOutboundTask predictInfo;

}
