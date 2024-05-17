package com.cqt.model.hmbc.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 话单简单实体定义
 *
 * @author scott
 * @date 2022年08月10日 10:11
 */
@Data
@ApiModel(value = "话单简单实体定义")
public class CdrRecordSimpleEntity implements Serializable {

    private static final long serialVersionUID = 2208731810407612964L;

    /**
     * 话单callId
     */
    @ApiModelProperty(value = "话单callId")
    private String acrCallId;

    /**
     * 被叫号码
     */
    @ApiModelProperty(value = "被叫号码")
    private String calledNum;

    /**
     * 参见主叫结束原因
     * 1、正常接通
     * 2、呼叫遇忙；
     * 3、用户不在服务区；
     * 4、用户无应答；空号识别没有到，根据结束时间-振铃时间>50s 判断为无应答
     * 5、用户关机；
     * 6、空号；
     * 7、停机；
     * 8、号码过期
     * 9、主叫应答，被叫应答前挂机(振铃后挂机)  有振铃时间
     * 91、主叫应答，被叫应答前挂机(振铃前挂机) 无振铃时间
     * 10、正在通话中
     * 11、 拒接
     * 1).空号识别为(呼叫遇忙或者正在通话中，根据结束时间-开始时间>16s,判断为拒接
     * 2).空号识别没识别到，22<结束时间-振铃时间<50,判断为拒接
     * 12、请不要挂机
     * 99、其他
     * 20：主动取消呼叫
     */
    @ApiModelProperty(value = "结束原因值")
    private Integer releaseCause;

    /**
     * 企业vccId
     */
    @ApiModelProperty(value = "企业vccId")
    private String vccId;

    /**
     * 开始呼叫时间
     * yyyyMMddHHmmss
     */
    @DateTimeFormat(pattern = "yyyyMMddHHmmss")
    @ApiModelProperty(value = "开始呼叫时间")
    private Date callOutTime;
}
