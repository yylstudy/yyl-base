package com.cqt.model.hmbc.dto;

import com.cqt.model.hmbc.entity.PrivateDialTestTimingConf;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 每个定时隐私号拨测任务
 *
 * @author Xienx
 * @date 2022年08月03日 15:35
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class HmbcTaskInfo implements Serializable {

    private static final long serialVersionUID = 4793635691686172925L;

    /**
     * 本次拨测任务的id
     */
    private String taskRecordId;

    /**
     * jobId
     */
    @ApiModelProperty(value = "jobId")
    private Integer jobId;

    /**
     * 企业vccId
     */
    @ApiModelProperty(value = "企业vccId")
    private String vccId;

    /**
     * 企业名称
     */
    @ApiModelProperty(value = "企业名称")
    private String vccName;

    /**
     * 本轮拨测任务的号码总数
     */
    @ApiModelProperty(value = "本轮拨测任务的号码总数")
    private Integer totalCount;

    /**
     * 企业拨测结果推送URL
     */
    @ApiModelProperty(value = "企业拨测结果推送URL")
    private String pushUrl;

    /**
     * 企业拨测结果推送类型（-1：不推送, 0：仅推送异常结果，1：推送全部结果）
     */
    @ApiModelProperty(value = "-1：不推送, 企业拨测结果推送类型（0：仅推送异常结果，1：推送全部结果）")
    private Integer pushType;

    /**
     * 是否需要推送拨测结果
     */
    private boolean pushRequired;

    /**
     * 拨测的号码信息
     */
    @JsonIgnore
    private List<DialTestNumberDTO> numberInfos;

    public HmbcTaskInfo(PrivateDialTestTimingConf timingConf) {
        this.jobId = timingConf.getJobId();
        this.vccId = timingConf.getVccId();
        this.pushUrl = timingConf.getPushUrl();
        this.pushType = timingConf.getPushType();
    }

    @Override
    public String toString() {
        return String.format("taskRecordId => %s, vccId: %s, jobId: %s", taskRecordId, vccId, jobId);
    }
}
