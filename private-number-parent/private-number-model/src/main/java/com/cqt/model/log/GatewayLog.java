package com.cqt.model.log;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author linshiqiang
 * @date 2022/2/7 10:12
 */
@Data
@TableName("gateway_request_log")
public class GatewayLog {

    @TableId(type = IdType.ASSIGN_ID)
    private String logId;

    /**
     * 企业id
     */
    private String vccId;

    /**
     * 地市编码
     */
    private String areaCode;

    /**
     * 当前请求的ip
     */
    private String currentHost;

    /**
     * 访问实例
     */
    private String targetServer;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求体
     */
    private String requestBody;

    /**
     * 响应结果
     */
    private String responseData;

    /**
     * 请求ip
     */
    private String sourceIp;

    /**
     * http 响应码
     */
    private Integer httpStatus;

    /**
     * 请求时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date requestTime;

    /**
     * 响应时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date responseTime;

    /**
     * 执行时间
     */
    private long executeTime;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 转发的url
     */
    private String forwardUrl;

    /**
     * 是否进入熔断 1 是
     */
    private Integer breakFlag;

    private String requestId;

    private String bindId;
}
