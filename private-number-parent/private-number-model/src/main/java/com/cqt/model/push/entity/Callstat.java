package com.cqt.model.push.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Callstat implements Serializable {

    private static final long serialVersionUID = -3194561125734158179L;
    private String tablename;

    /**
     * 14位时间搓
     */
    private String streamnumber;


    private String servicekey;//固定900007

    private Integer callcost;//固定0

    private String calledpartynumber;//X

    private String callingpartynumber;//A

    private String chargemode;//0

    private String specificchargedpar;//X

    private String translatednumber;//B

    private String startdateandtime;

    private String stopdateandtime;

    private String duration;

    private String chargeclass;//102

    private String transparentparamet;//BINDID

    private String calltype;//

    private String callersubgroup;//""

    private String calleesubgroup;//""

    private String acrcallid;//C+14位时间戳+16位

    private String oricallednumber;//A

    private String oricallingnumber;//B

    private String callerpnp;//""

    private String calleepnp;//""

    private String reroute;//"1"

    private String groupnumber;//VCCID

    private String callcategory;//1

    private String chargetype;//市话长途，B和x判断

    private String userpin;//""

    private String acrtype;//1

    private String videocallflag;//有录音地址写通话时长，默认为0

    private String serviceid;//""

    private String forwardnumber;//通话id

    private String extforwardnumber;//振铃时间

    private String srfmsgid;//录音地址

    private String msserver;//""

    private String begintime;//呼叫开始时间
    /**
     * 结束码
     **/
    private String releasecause;//

    /**
     * 结束原因
     **/
    private String releasereason;

    private String areanumber;

    private String dtmfkey;

    private boolean execute;

    @JsonProperty("bNumFail")
    private String bNumFail;

    private String recordPush;


    //冗余字段
    private String key1;//录音下载地址
    private String key2;//呼入时间
    private String key3;
    private String key4;
    private String key5;

}
