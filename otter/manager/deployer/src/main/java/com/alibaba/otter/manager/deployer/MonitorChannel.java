package com.alibaba.otter.manager.deployer;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/3/31 14:10
 */

public class MonitorChannel implements Serializable {
    private static final long serialVersionUID = 2345662422309356370L;
    private Long              id;                                       // 唯一标示id
    private String            name;
    private ChannelStatus state;
    private String            description;                              // 描述信息
    private List<MonitorPipeline> pipelines;
    private Date gmtCreate;
    private Date              gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChannelStatus getState() {
        return state;
    }

    public void setState(ChannelStatus state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MonitorPipeline> getPipelines() {
        return pipelines;
    }

    public void setPipelines(List<MonitorPipeline> pipelines) {
        this.pipelines = pipelines;
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
}
