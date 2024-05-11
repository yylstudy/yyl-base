package com.alibaba.otter.manager.deployer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.statistics.delay.DelayStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.ThroughputStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputCondition;
import com.alibaba.otter.shared.arbitrate.ArbitrateViewService;
import com.alibaba.otter.shared.arbitrate.model.MainStemEventData;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.statistics.delay.DelayStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputStat;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputType;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/3/31 14:34
 */

public class LinkcircleMonitor {
    public String getMonitorStr(){
        Long delayTime = ApplicationContextHodler.getApplicationContext().getEnvironment().getProperty("otter.delayTime",Long.class,1800000L);
        Long lastSyncTime = ApplicationContextHodler.getApplicationContext().getEnvironment().getProperty("otter.lastSyncTime",Long.class,720L);
        StringBuilder stringBuilder = new StringBuilder();
        List<MonitorChannel> monitorChannels = getMonitorChannel();
        for(MonitorChannel monitorChannel:monitorChannels){
            if(monitorChannel.getState()==ChannelStatus.STOP){
                continue;
            }
            if(monitorChannel.getState()!= ChannelStatus.RUNNING){
                stringBuilder.append("channel名字为：")
                        .append(monitorChannel.getName())
                        .append("运行状态为：")
                        .append(monitorChannel.getState().getMessage())
                        .append("，请检查！\r\n");
            }else{
                for(MonitorPipeline monitorPipeline:monitorChannel.getPipelines()){
                    String pipelineStr = "";
                    if(monitorPipeline.getPipelineStatus()!= PipelineStatus.WORKING){
                        pipelineStr+="pipeline状态为："+monitorPipeline.getPipelineStatus().getMessage()+";";
                    }
                    if(monitorPipeline.getDelayTime()>delayTime){
                        pipelineStr+="pipeline延迟时间为："+monitorPipeline.getDelayTime()/1000+"秒;";
                    }
                    ThroughputStatService throughputStatService = ApplicationContextHodler.getBean("throughputStatService");
                    ThroughputCondition condition = new ThroughputCondition();
                    condition.setPipelineId(monitorPipeline.getId());
                    condition.setType(ThroughputType.ROW);
                    ThroughputStat throughputStat = throughputStatService.findThroughputStatByPipelineId(condition);
                    long syncTime = System.currentTimeMillis()-throughputStat.getGmtModified().getTime();
                    if(syncTime>lastSyncTime*60*1000){
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        pipelineStr+="pipeline最后同步时间为："+ sdf.format(throughputStat.getGmtModified())+";";
                    }
                    if(!StringUtils.isEmpty(pipelineStr)){
                        stringBuilder.append("channel名字为：")
                                .append(monitorChannel.getName())
                                .append("运行状态为：")
                                .append(monitorChannel.getState().getMessage())
                                .append("，")
                                .append("pipeline名字为：")
                                .append(monitorPipeline.getName());
                        stringBuilder.append(pipelineStr);
                        stringBuilder.append("请检查！");
                    }
                }
            }
        }
        return stringBuilder.toString();
    }


    public List<MonitorChannel> getMonitorChannel(){
        ChannelService channelService = ApplicationContextHodler.getBean("channelService");
        ArbitrateViewService arbitrateViewService = ApplicationContextHodler.getBean("arbitrateViewService");
        DelayStatService delayStatService = ApplicationContextHodler.getBean("delayStatService");
        List<Channel> channels = channelService.listAll();
        List<MonitorChannel> monitorChannels = new ArrayList<MonitorChannel>();
        for(Channel channel:channels){
            MonitorChannel monitorChannel = new MonitorChannel();
            BeanUtils.copyProperties(channel,monitorChannel);
            if(channel.getStatus().isStart()){
                monitorChannel.setState(ChannelStatus.RUNNING);
            }else if(channel.getStatus().isPause()){
                monitorChannel.setState(ChannelStatus.PAUSE);
            }else{
                monitorChannel.setState(ChannelStatus.STOP);
            }
            List<Pipeline> pipelines = channel.getPipelines();
            List<MonitorPipeline> monitorPipelines = new ArrayList<MonitorPipeline>();
            for (Pipeline pipeline : pipelines) {
                MonitorPipeline monitorPipeline = new MonitorPipeline();
                BeanUtils.copyProperties(pipeline,monitorPipeline);
                DelayStat delayStat = delayStatService.findRealtimeDelayStat(pipeline.getId());
                if (delayStat.getDelayNumber() == null) {
                    monitorPipeline.setDelayNumber(0L);
                    monitorPipeline.setDelayTime(0L);
                }else{
                    monitorPipeline.setDelayNumber(delayStat.getDelayNumber());
                    monitorPipeline.setDelayTime(delayStat.getDelayTime());
                }
                MainStemEventData mainStemEventData = arbitrateViewService.mainstemData(channel.getId(), pipeline.getId());
                if(mainStemEventData==null){
                    monitorPipeline.setPipelineStatus(PipelineStatus.UNWORK);
                }else if(mainStemEventData.getStatus().isTaking()){
                    monitorPipeline.setPipelineStatus(PipelineStatus.POSITIONING);
                }else{
                    monitorPipeline.setPipelineStatus(PipelineStatus.WORKING);
                }
                monitorPipelines.add(monitorPipeline);
            }
            monitorChannel.setPipelines(monitorPipelines);
            monitorChannels.add(monitorChannel);
        }
        return monitorChannels;
    }
}
