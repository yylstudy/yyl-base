package com.cqt.unicom.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-05-17 14:11
 */
@Data
@Component
@ConfigurationProperties(prefix = "unicom.digit")
public class DigitEventInfoProperties {

    /**
     * 不能为空	控制指示
     * 二进制每一位标识特定的含义，最终转换成十进制作为控制指示的值。
     * BIT0：
     * 1：中断语音通知播放
     * 0：不中断语音通知播放
     * BIT1：
     * 1：循环连续收号
     * 0：不循环收号
     * BIT2：
     * 1：首位数字上报
     * 0：首位数字不上
     */
    private String control = "111";

    /**
     * 不能为空	最小收集数字个数
     */
    private Integer minCollect = 1;

    /**
     * 不能为空	最大收集数字个数
     */
    private Integer maxCollect = 4;

    /**
     * 不能为空	等待收集数字完成的总时长
     * 0：总时长没有限制
     */
    private Integer maxInteractTime = 0;

    /**
     * 不能为空	等待首位数字超时时间
     * 0：标识缺省值5s
     * 其他值：具体的秒数
     */
    private Integer initInterDgtTime = 0;

    /**
     * 不能为空	两个数字输入之间的间隔时间。
     * 超时，会上报给后台已收到的号码串
     */
    private Integer normInterDgtTime = 5;

    /**
     * 不能为空	应答结束数字
     * 0：不需要（收号到maxCollect的时候自动结束，或者收号间隔超过normInterDgtTime的时候自动结束）
     * 1：*号键
     * 2：#号键
     * 3：*号或#号键
     */
    private Integer enterDgtMask = 0;

    /**
     * 不能为空	收号方式
     * 1：对2G资源，收号方式取值为1时，字段可选，其它取值字段必选；
     * 2：对3G资源，承载类型为TDM（无RTP连接），收号方式取值为3时，字段可选；其它取值，字段必选；
     * 3：对3G资源，有RTP连接时，收号方式已经在RTP激活连接时设置，字段可选。
     */
    private Integer digitCollectionType = 1;
}
