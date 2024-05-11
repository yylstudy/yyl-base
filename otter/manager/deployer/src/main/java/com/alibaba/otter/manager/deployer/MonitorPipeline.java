/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.manager.deployer;

import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;

import java.io.Serializable;
import java.util.Date;

/**
 * 同步任务数据对象
 * 
 * @author jianghang 2011-8-31 下午07:35:38
 */
public class MonitorPipeline implements Serializable {

    private static final long   serialVersionUID = 5055655233043393285L;
    private Long                id;
    private Long                channelId;
    private String              name;
    private String              description;
    private Date                gmtCreate;
    private Date                gmtModified;
    private PipelineStatus pipelineStatus;
    private Long              delayNumber;                             // 延迟数量
    private Long              delayTime;                               // 延迟时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public PipelineStatus getPipelineStatus() {
        return pipelineStatus;
    }

    public void setPipelineStatus(PipelineStatus pipelineStatus) {
        this.pipelineStatus = pipelineStatus;
    }

    public Long getDelayNumber() {
        return delayNumber;
    }

    public void setDelayNumber(Long delayNumber) {
        this.delayNumber = delayNumber;
    }

    public Long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }
}
