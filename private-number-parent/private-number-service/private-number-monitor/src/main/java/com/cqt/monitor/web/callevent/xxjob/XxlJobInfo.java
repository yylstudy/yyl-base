package com.cqt.monitor.web.callevent.xxjob;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * xxl-job info
 *
 * @author xuxueli
 * @date 2016-1-12 18:25:49
 */
@Data
public class XxlJobInfo implements Serializable {

	private static final long serialVersionUID = -3163477245198274642L;

	/**
	 * 主键ID
	 */
	private int id;

	/**
	 * 执行器主键ID
	 */
	private int jobGroup;
	private String jobDesc;

	private Date addTime;
	private Date updateTime;

	/**
	 * 负责人
	 */
	private String author;

	/**
	 * 报警邮件
	 */
	private String alarmEmail;

	/**
	 * 调度类型
	 */
	private String scheduleType;

	/**
	 * 调度配置，值含义取决于调度类型
	 */
	private String scheduleConf;

	/**
	 * 调度过期策略
	 */
	private String misfireStrategy;

	/**
	 * 执行器路由策略
	 */
	private String executorRouteStrategy;

	/**
	 * 执行器，任务Handler名称
	 */
	private String executorHandler;

	/**
	 * 执行器，任务参数
	 */
	private String executorParam;

	/**
	 * 阻塞处理策略
	 */
	private String executorBlockStrategy;

	/**
	 * 任务执行超时时间，单位秒
	 */
	private int executorTimeout;

	/**
	 * 失败重试次数
	 */
	private int executorFailRetryCount;

	/**
	 * GLUE类型	#com.xxl.job.core.glue.GlueTypeEnum
	 */
	private String glueType;

	/**
	 * GLUE源代码
	 */
	private String glueSource;

	/**
	 * GLUE备注
	 */
	private String glueRemark;

	/**
	 * GLUE更新时间
	 */
	private Date glueUpdatetime;

	/**
	 * 子任务ID，多个逗号分隔
	 */
	private String childJobId;

	/**
	 * 调度状态：0-停止，1-运行
	 */
	private int triggerStatus;

	/**
	 * 上次调度时间
	 */
	private long triggerLastTime;

	/**
	 * 下次调度时间
	 */
	private long triggerNextTime;

	public XxlJobInfo() {
	}

	public XxlJobInfo(int id) {
		this.id = id;
	}

	public XxlJobInfo(int jobGroup, String jobDesc, String scheduleConf, String executorHandler) {
		this.jobGroup = jobGroup;
		this.jobDesc = jobDesc;
		this.scheduleConf = scheduleConf;
		this.executorHandler = executorHandler;

		// 这里是一些固定属性
		this.author = "admin";
		this.scheduleType = "CRON";
		this.misfireStrategy = "DO_NOTHING";
		this.executorRouteStrategy = "FAILOVER";
		this.glueType = GlueTypeEnum.BEAN.getDesc();
		this.executorBlockStrategy = "DISCARD_LATER";
	}

	public XxlJobInfo(int jobGroup, String jobDesc, String scheduleConf, String executorHandler,String executorParam) {
		this.jobGroup = jobGroup;
		this.jobDesc = jobDesc;
		this.scheduleConf = scheduleConf;
		this.executorHandler = executorHandler;
		this.executorParam = executorParam;
		// 这里是一些固定属性
		this.author = "admin";
		this.scheduleType = "CRON";
		this.misfireStrategy = "DO_NOTHING";
		this.executorRouteStrategy = "FAILOVER";
		this.glueType = GlueTypeEnum.BEAN.getDesc();
		this.executorBlockStrategy = "DISCARD_LATER";
	}
}
