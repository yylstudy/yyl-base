package com.cqt.monitor.web.callevent.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
@JsonInclude
public class Callstat implements Serializable {

	private String tablename;//表名

    private String streamnumber;//序列号

    private String servicekey;//业务关键字

    private Integer callcost;//通话费用

    private String calledpartynumber;//被叫号码

    private String callingpartynumber;//主叫号码

    private String chargemode;//计费模式

    private String specificchargedpar;//计费号码

    private String translatednumber;//翻译号码

    private String startdateandtime;//通话开始时间

    private String stopdateandtime;//通话结束时间

    private String duration;//通话时长

    private String chargeclass;//翻译号码

    private String transparentparamet;//messageid

    private String calltype;//呼叫类型

    private String callersubgroup;//坐席工号

    private String calleesubgroup;//网关名称

    private String acrcallid;//当前呼叫的CallID

    private String oricallednumber;//原始被叫

    private String oricallingnumber;//原始主叫

    private String callerpnp;//主叫短号

    private String calleepnp;//被叫短号

    private String reroute;//重路由类型

    private String groupnumber;//VCCID

    private String callcategory;//点击拨号的呼叫顺序：1：第一通呼叫， 2：第二通呼叫；

    private String chargetype;//话单类型：0：市话 1：国内长途2：国际长途

    private String userpin;//计费码

    private String acrtype;//呼叫类型，指发端或终端

    private String videocallflag;//录音时长

    private String serviceid;//

    private String forwardnumber;//uuid

    private String extforwardnumber;

   private String srfmsgid;//录音地址

    private String msserver;//录音设备

    private String begintime;//呼叫开始时间

    private String releasecause;//结束码

    private String releasereason;//结束原因值

    private String areanumber;//主叫区号

    private String dtmfkey;//按键

    private  boolean Execute;//判断话单是否重复

    private String bNumFail;//是否需要虚拟B路话单

    private String recordPush;

    //冗余字段
    private String key1;
    private String key2;
    private String key3;
    private String key4;
    private String key5;

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getKey3() {
        return key3;
    }

    public void setKey3(String key3) {
        this.key3 = key3;
    }

    public String getKey4() {
        return key4;
    }

    public void setKey4(String key4) {
        this.key4 = key4;
    }

    public String getKey5() {
        return key5;
    }

    public void setKey5(String key5) {
        this.key5 = key5;
    }

    public String getRecordPush() {
        return recordPush;
    }

    public void setRecordPush(String recordPush) {
        this.recordPush = recordPush;
    }


    public boolean isExecute() {
		return Execute;
	}

	public String getbNumFail() {
		return bNumFail;
	}

	public void setbNumFail(String bNumFail) {
		this.bNumFail = bNumFail;
	}

	public boolean Execute() {
		return Execute;
	}

	public void setExecute(boolean execute) {
		Execute = execute;
	}

	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public String getStreamnumber() {
		return streamnumber;
	}

	public void setStreamnumber(String streamnumber) {
		this.streamnumber = streamnumber;
	}

	public String getServicekey() {
		return servicekey;
	}

	public void setServicekey(String servicekey) {
		this.servicekey = servicekey;
	}

	public Integer getCallcost() {
		return callcost;
	}

	public void setCallcost(Integer callcost) {
		this.callcost = callcost;
	}

	public String getCalledpartynumber() {
		return calledpartynumber;
	}

	public void setCalledpartynumber(String calledpartynumber) {
		this.calledpartynumber = calledpartynumber;
	}

	public String getCallingpartynumber() {
		return callingpartynumber;
	}

	public void setCallingpartynumber(String callingpartynumber) {
		this.callingpartynumber = callingpartynumber;
	}

	public String getChargemode() {
		return chargemode;
	}

	public void setChargemode(String chargemode) {
		this.chargemode = chargemode;
	}

	public String getSpecificchargedpar() {
		return specificchargedpar;
	}

	public void setSpecificchargedpar(String specificchargedpar) {
		this.specificchargedpar = specificchargedpar;
	}

	public String getTranslatednumber() {
		return translatednumber;
	}

	public void setTranslatednumber(String translatednumber) {
		this.translatednumber = translatednumber;
	}

	public String getStartdateandtime() {
		return startdateandtime;
	}

	public void setStartdateandtime(String startdateandtime) {
		this.startdateandtime = startdateandtime;
	}

	public String getStopdateandtime() {
		return stopdateandtime;
	}

	public void setStopdateandtime(String stopdateandtime) {
		this.stopdateandtime = stopdateandtime;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getChargeclass() {
		return chargeclass;
	}

	public void setChargeclass(String chargeclass) {
		this.chargeclass = chargeclass;
	}

	public String getTransparentparamet() {
		return transparentparamet;
	}

	public void setTransparentparamet(String transparentparamet) {
		this.transparentparamet = transparentparamet;
	}

	public String getCalltype() {
		return calltype;
	}

	public void setCalltype(String calltype) {
		this.calltype = calltype;
	}

	public String getCallersubgroup() {
		return callersubgroup;
	}

	public void setCallersubgroup(String callersubgroup) {
		this.callersubgroup = callersubgroup;
	}

	public String getCalleesubgroup() {
		return calleesubgroup;
	}

	public void setCalleesubgroup(String calleesubgroup) {
		this.calleesubgroup = calleesubgroup;
	}

	public String getAcrcallid() {
		return acrcallid;
	}

	public void setAcrcallid(String acrcallid) {
		this.acrcallid = acrcallid;
	}

	public String getOricallednumber() {
		return oricallednumber;
	}

	public void setOricallednumber(String oricallednumber) {
		this.oricallednumber = oricallednumber;
	}

	public String getOricallingnumber() {
		return oricallingnumber;
	}

	public void setOricallingnumber(String oricallingnumber) {
		this.oricallingnumber = oricallingnumber;
	}

	public String getCallerpnp() {
		return callerpnp;
	}

	public void setCallerpnp(String callerpnp) {
		this.callerpnp = callerpnp;
	}

	public String getCalleepnp() {
		return calleepnp;
	}

	public void setCalleepnp(String calleepnp) {
		this.calleepnp = calleepnp;
	}

	public String getReroute() {
		return reroute;
	}

	public void setReroute(String reroute) {
		this.reroute = reroute;
	}

	public String getGroupnumber() {
		return groupnumber;
	}

	public void setGroupnumber(String groupnumber) {
		this.groupnumber = groupnumber;
	}

	public String getCallcategory() {
		return callcategory;
	}

	public void setCallcategory(String callcategory) {
		this.callcategory = callcategory;
	}

	public String getChargetype() {
		return chargetype;
	}

	public void setChargetype(String chargetype) {
		this.chargetype = chargetype;
	}

	public String getUserpin() {
		return userpin;
	}

	public void setUserpin(String userpin) {
		this.userpin = userpin;
	}

	public String getAcrtype() {
		return acrtype;
	}

	public void setAcrtype(String acrtype) {
		this.acrtype = acrtype;
	}

	public String getVideocallflag() {
		return videocallflag;
	}

	public void setVideocallflag(String videocallflag) {
		this.videocallflag = videocallflag;
	}

	public String getServiceid() {
		return serviceid;
	}

	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}

	public String getForwardnumber() {
		return forwardnumber;
	}

	public void setForwardnumber(String forwardnumber) {
		this.forwardnumber = forwardnumber;
	}

	public String getExtforwardnumber() {
		return extforwardnumber;
	}

	public void setExtforwardnumber(String extforwardnumber) {
		this.extforwardnumber = extforwardnumber;
	}

	public String getSrfmsgid() {
		return srfmsgid;
	}

	public void setSrfmsgid(String srfmsgid) {
		this.srfmsgid = srfmsgid;
	}

	public String getMsserver() {
		return msserver;
	}

	public void setMsserver(String msserver) {
		this.msserver = msserver;
	}

	public String getBegintime() {
		return begintime;
	}

	public void setBegintime(String begintime) {
		this.begintime = begintime;
	}

	public String getReleasecause() {
		return releasecause;
	}

	public void setReleasecause(String releasecause) {
		this.releasecause = releasecause;
	}

	public String getReleasereason() {
		return releasereason;
	}

	public void setReleasereason(String releasereason) {
		this.releasereason = releasereason;
	}

	public String getAreanumber() {
		return areanumber;
	}

	public void setAreanumber(String areanumber) {
		this.areanumber = areanumber;
	}

	public String getDtmfkey() {
		return dtmfkey;
	}

	public void setDtmfkey(String dtmfkey) {
		this.dtmfkey = dtmfkey;
	}

    @Override
    public String toString() {
        return "Callstat{" +
                "tablename='" + tablename + '\'' +
                ", streamnumber='" + streamnumber + '\'' +
                ", servicekey='" + servicekey + '\'' +
                ", callcost=" + callcost +
                ", calledpartynumber='" + calledpartynumber + '\'' +
                ", callingpartynumber='" + callingpartynumber + '\'' +
                ", chargemode='" + chargemode + '\'' +
                ", specificchargedpar='" + specificchargedpar + '\'' +
                ", translatednumber='" + translatednumber + '\'' +
                ", startdateandtime='" + startdateandtime + '\'' +
                ", stopdateandtime='" + stopdateandtime + '\'' +
                ", duration='" + duration + '\'' +
                ", chargeclass='" + chargeclass + '\'' +
                ", transparentparamet='" + transparentparamet + '\'' +
                ", calltype='" + calltype + '\'' +
                ", callersubgroup='" + callersubgroup + '\'' +
                ", calleesubgroup='" + calleesubgroup + '\'' +
                ", acrcallid='" + acrcallid + '\'' +
                ", oricallednumber='" + oricallednumber + '\'' +
                ", oricallingnumber='" + oricallingnumber + '\'' +
                ", callerpnp='" + callerpnp + '\'' +
                ", calleepnp='" + calleepnp + '\'' +
                ", reroute='" + reroute + '\'' +
                ", groupnumber='" + groupnumber + '\'' +
                ", callcategory='" + callcategory + '\'' +
                ", chargetype='" + chargetype + '\'' +
                ", userpin='" + userpin + '\'' +
                ", acrtype='" + acrtype + '\'' +
                ", videocallflag='" + videocallflag + '\'' +
                ", serviceid='" + serviceid + '\'' +
                ", forwardnumber='" + forwardnumber + '\'' +
                ", extforwardnumber='" + extforwardnumber + '\'' +
                ", srfmsgid='" + srfmsgid + '\'' +
                ", msserver='" + msserver + '\'' +
                ", begintime='" + begintime + '\'' +
                ", releasecause='" + releasecause + '\'' +
                ", releasereason='" + releasereason + '\'' +
                ", areanumber='" + areanumber + '\'' +
                ", dtmfkey='" + dtmfkey + '\'' +
                ", Execute=" + Execute +
                ", bNumFail='" + bNumFail + '\'' +
                '}';
    }
}
