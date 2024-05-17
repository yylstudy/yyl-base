package com.cqt.model.cdr.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class CdrDatapushPushEntity {
    private Long id;

    private String first_real_caller;

    private String first_real_callee;

    private String real_caller;

    private String real_callee;

    private String caller_id_number;

    private String callee_id_number;

    private String cr_destination;

    private String start_stamp;

    private String answer_stamp;

    private String end_stamp;

    private Integer linktimes;

    private Integer billsec;

    private Integer ivrtime;

    private Integer ringtimes;

    private String first_bridge_uuid;

    private String uuid;

    private String record_filepath;

    private String record_start;

    private String leavemsg_filepath;

    private String leavemsg_start;

    private String callout_hungup_cause;

    private String hangup_cause;

    private String ivrid_e;

    private String ivrid_s;

    private String satisfaction_ivrid;

    private String company_code;

    private Integer a_calltype;

    private Integer b_calltype;

    private Integer first_a_calltype;

    private Integer first_b_calltype;

    private String call_tracks;

    private String type;

    private String platform_number;

    private String cc_sys_agent_id;

    private String f_uuid;

    private String satisfaction_f1;

    private String satisfaction_f2;

    private String satisfaction_f3;

    private String satisfaction_f4;

    private String satisfaction_f5;

    private String satisfaction_f6;

    private String satisfaction_f7;

    private String satisfaction_f8;

    private String satisfaction_f9;

    private String satisfaction_f10;

    private String userkey_f1;

    private String userkey_f2;

    private String userkey_f3;

    private String userkey_f4;

    private String userkey_f5;

    private String userkey_f6;

    private String userkey_f7;

    private String userkey_f8;

    private String userkey_f9;

    private String local_ip_v4;

    private String caller_id_name;

    private String usrphone;

    private String direction;

    private String usrphone_prefix;

    private Integer duration;

    private Integer ringbacktimes;

    private Integer first_bridge_on;

    private String bridge_stamp;

    private String context;

    private String bleg_uuid;

    private String accountcode;

    private String read_codec;

    private String write_codec;

    private String sip_hangup_disposition;

    private String ani;

    private String short_channel_name;

    private String channel_register_user;

    private String cc_queue;

    private String cc_agent_name;

    private String cc_agent;

    private String cc_side;

    private Long cc_queue_answered_epoch;

    private Integer cc_queue_times;

    private Long cc_queue_terminated_epoch;

    @JsonProperty("cc_queue_Joined_epoch")
    private Long cc_queue_joined_epoch;

    private Long cc_queue_canceled_epoch;

    private Integer cc_ringbacktimes;

    private String obpjnum;

    private Integer pjjobid;

    private String obc_taskid;

    private String obc_prjcode;

    private String server_name;

    private Integer ifqa;

    private String callin_process_mode;

    private String caseid;

    private String cusid;

    private String ifplayedleavemsg;

    private String relative_uuid;

    private Integer reqstate;

    private Integer reqcount;

    private String userkey_f10;

    private String area_code;

    public CdrDatapushPushEntity() {
        super();
    }

    @Override
    public String toString() {
        return "CdrDatapushPushEntity{" +
                "first_real_caller='" + first_real_caller + '\'' +
                ", first_real_callee='" + first_real_callee + '\'' +
                ", real_caller='" + real_caller + '\'' +
                ", real_callee='" + real_callee + '\'' +
                ", caller_id_number='" + caller_id_number + '\'' +
                ", callee_id_number='" + callee_id_number + '\'' +
                ", cr_destination='" + cr_destination + '\'' +
                ", start_stamp='" + start_stamp + '\'' +
                ", answer_stamp='" + answer_stamp + '\'' +
                ", end_stamp='" + end_stamp + '\'' +
                ", linktimes=" + linktimes +
                ", billsec=" + billsec +
                ", ivrtime=" + ivrtime +
                ", ringtimes=" + ringtimes +
                ", first_bridge_uuid='" + first_bridge_uuid + '\'' +
                ", uuid='" + uuid + '\'' +
                ", record_filepath='" + record_filepath + '\'' +
                ", record_start='" + record_start + '\'' +
                ", leavemsg_filepath='" + leavemsg_filepath + '\'' +
                ", leavemsg_start='" + leavemsg_start + '\'' +
                ", callout_hungup_cause='" + callout_hungup_cause + '\'' +
                ", hangup_cause='" + hangup_cause + '\'' +
                ", ivrid_e='" + ivrid_e + '\'' +
                ", ivrid_s='" + ivrid_s + '\'' +
                ", satisfaction_ivrid='" + satisfaction_ivrid + '\'' +
                ", company_code='" + company_code + '\'' +
                ", a_calltype=" + a_calltype +
                ", b_calltype=" + b_calltype +
                ", first_a_calltype=" + first_a_calltype +
                ", first_b_calltype=" + first_b_calltype +
                ", call_tracks='" + call_tracks + '\'' +
                ", type='" + type + '\'' +
                ", platform_number='" + platform_number + '\'' +
                ", cc_sys_agent_id='" + cc_sys_agent_id + '\'' +
                ", f_uuid='" + f_uuid + '\'' +
                ", satisfaction_f1='" + satisfaction_f1 + '\'' +
                ", satisfaction_f2='" + satisfaction_f2 + '\'' +
                ", satisfaction_f3='" + satisfaction_f3 + '\'' +
                ", satisfaction_f4='" + satisfaction_f4 + '\'' +
                ", satisfaction_f5='" + satisfaction_f5 + '\'' +
                ", satisfaction_f6='" + satisfaction_f6 + '\'' +
                ", satisfaction_f7='" + satisfaction_f7 + '\'' +
                ", satisfaction_f8='" + satisfaction_f8 + '\'' +
                ", satisfaction_f9='" + satisfaction_f9 + '\'' +
                ", satisfaction_f10='" + satisfaction_f10 + '\'' +
                ", userkey_f1='" + userkey_f1 + '\'' +
                ", userkey_f2='" + userkey_f2 + '\'' +
                ", userkey_f3='" + userkey_f3 + '\'' +
                ", userkey_f4='" + userkey_f4 + '\'' +
                ", userkey_f5='" + userkey_f5 + '\'' +
                ", userkey_f6='" + userkey_f6 + '\'' +
                ", userkey_f7='" + userkey_f7 + '\'' +
                ", userkey_f8='" + userkey_f8 + '\'' +
                ", userkey_f9='" + userkey_f9 + '\'' +
                ", local_ip_v4='" + local_ip_v4 + '\'' +
                ", caller_id_name='" + caller_id_name + '\'' +
                ", usrphone='" + usrphone + '\'' +
                ", direction='" + direction + '\'' +
                ", usrphone_prefix='" + usrphone_prefix + '\'' +
                ", duration=" + duration +
                ", ringbacktimes=" + ringbacktimes +
                ", first_bridge_on=" + first_bridge_on +
                ", bridge_stamp='" + bridge_stamp + '\'' +
                ", context='" + context + '\'' +
                ", bleg_uuid='" + bleg_uuid + '\'' +
                ", accountcode='" + accountcode + '\'' +
                ", read_codec='" + read_codec + '\'' +
                ", write_codec='" + write_codec + '\'' +
                ", sip_hangup_disposition='" + sip_hangup_disposition + '\'' +
                ", ani='" + ani + '\'' +
                ", short_channel_name='" + short_channel_name + '\'' +
                ", channel_register_user='" + channel_register_user + '\'' +
                ", cc_queue='" + cc_queue + '\'' +
                ", cc_agent_name='" + cc_agent_name + '\'' +
                ", cc_agent='" + cc_agent + '\'' +
                ", cc_side='" + cc_side + '\'' +
                ", cc_queue_answered_epoch=" + cc_queue_answered_epoch +
                ", cc_queue_times=" + cc_queue_times +
                ", cc_queue_terminated_epoch=" + cc_queue_terminated_epoch +
                ", cc_queue_joined_epoch=" + cc_queue_joined_epoch +
                ", cc_queue_canceled_epoch=" + cc_queue_canceled_epoch +
                ", cc_ringbacktimes=" + cc_ringbacktimes +
                ", obpjnum='" + obpjnum + '\'' +
                ", pjjobid=" + pjjobid +
                ", obc_taskid='" + obc_taskid + '\'' +
                ", obc_prjcode='" + obc_prjcode + '\'' +
                ", server_name='" + server_name + '\'' +
                ", ifqa=" + ifqa +
                ", callin_process_mode='" + callin_process_mode + '\'' +
                ", caseid='" + caseid + '\'' +
                ", cusid='" + cusid + '\'' +
                ", ifplayedleavemsg='" + ifplayedleavemsg + '\'' +
                ", relative_uuid='" + relative_uuid + '\'' +
                ", reqstate=" + reqstate +
                ", reqcount=" + reqcount +
                ", userkey_f10='" + userkey_f10 + '\'' +
                ", area_code='" + area_code + '\'' +
                '}';
    }

    public CdrDatapushPushEntity(String first_real_caller, String first_real_callee, String real_caller, String real_callee, String caller_id_number, String callee_id_number, String cr_destination, String start_stamp, String answer_stamp, String end_stamp, Integer linktimes, Integer billsec, Integer ivrtime, Integer ringtimes, String first_bridge_uuid, String uuid, String record_filepath, String record_start, String leavemsg_filepath, String leavemsg_start, String callout_hungup_cause, String hangup_cause, String ivrid_e, String ivrid_s, String satisfaction_ivrid, String company_code, Integer a_calltype, Integer b_calltype, Integer first_a_calltype, Integer first_b_calltype, String call_tracks, String type, String platform_number, String cc_sys_agent_id, String f_uuid, String satisfaction_f1, String satisfaction_f2, String satisfaction_f3, String satisfaction_f4, String satisfaction_f5, String satisfaction_f6, String satisfaction_f7, String satisfaction_f8, String satisfaction_f9, String satisfaction_f10, String userkey_f1, String userkey_f2, String userkey_f3, String userkey_f4, String userkey_f5, String userkey_f6, String userkey_f7, String userkey_f8, String userkey_f9, String local_ip_v4, String caller_id_name, String usrphone, String direction, String usrphone_prefix, Integer duration, Integer ringbacktimes, Integer first_bridge_on, String bridge_stamp, String context, String bleg_uuid, String accountcode, String read_codec, String write_codec, String sip_hangup_disposition, String ani, String short_channel_name, String channel_register_user, String cc_queue, String cc_agent_name, String cc_agent, String cc_side, Long cc_queue_answered_epoch, Integer cc_queue_times, Long cc_queue_terminated_epoch, Long cc_queue_joined_epoch, Long cc_queue_canceled_epoch, Integer cc_ringbacktimes, String obpjnum, Integer pjjobid, String obc_taskid, String obc_prjcode, String server_name, Integer ifqa, String callin_process_mode, String caseid, String cusid, String ifplayedleavemsg, String relative_uuid, Integer reqstate, Integer reqcount, String userkey_f10, String area_code) {
        this.first_real_caller = first_real_caller;
        this.first_real_callee = first_real_callee;
        this.real_caller = real_caller;
        this.real_callee = real_callee;
        this.caller_id_number = caller_id_number;
        this.callee_id_number = callee_id_number;
        this.cr_destination = cr_destination;
        this.start_stamp = start_stamp;
        this.answer_stamp = answer_stamp;
        this.end_stamp = end_stamp;
        this.linktimes = linktimes;
        this.billsec = billsec;
        this.ivrtime = ivrtime;
        this.ringtimes = ringtimes;
        this.first_bridge_uuid = first_bridge_uuid;
        this.uuid = uuid;
        this.record_filepath = record_filepath;
        this.record_start = record_start;
        this.leavemsg_filepath = leavemsg_filepath;
        this.leavemsg_start = leavemsg_start;
        this.callout_hungup_cause = callout_hungup_cause;
        this.hangup_cause = hangup_cause;
        this.ivrid_e = ivrid_e;
        this.ivrid_s = ivrid_s;
        this.satisfaction_ivrid = satisfaction_ivrid;
        this.company_code = company_code;
        this.a_calltype = a_calltype;
        this.b_calltype = b_calltype;
        this.first_a_calltype = first_a_calltype;
        this.first_b_calltype = first_b_calltype;
        this.call_tracks = call_tracks;
        this.type = type;
        this.platform_number = platform_number;
        this.cc_sys_agent_id = cc_sys_agent_id;
        this.f_uuid = f_uuid;
        this.satisfaction_f1 = satisfaction_f1;
        this.satisfaction_f2 = satisfaction_f2;
        this.satisfaction_f3 = satisfaction_f3;
        this.satisfaction_f4 = satisfaction_f4;
        this.satisfaction_f5 = satisfaction_f5;
        this.satisfaction_f6 = satisfaction_f6;
        this.satisfaction_f7 = satisfaction_f7;
        this.satisfaction_f8 = satisfaction_f8;
        this.satisfaction_f9 = satisfaction_f9;
        this.satisfaction_f10 = satisfaction_f10;
        this.userkey_f1 = userkey_f1;
        this.userkey_f2 = userkey_f2;
        this.userkey_f3 = userkey_f3;
        this.userkey_f4 = userkey_f4;
        this.userkey_f5 = userkey_f5;
        this.userkey_f6 = userkey_f6;
        this.userkey_f7 = userkey_f7;
        this.userkey_f8 = userkey_f8;
        this.userkey_f9 = userkey_f9;
        this.local_ip_v4 = local_ip_v4;
        this.caller_id_name = caller_id_name;
        this.usrphone = usrphone;
        this.direction = direction;
        this.usrphone_prefix = usrphone_prefix;
        this.duration = duration;
        this.ringbacktimes = ringbacktimes;
        this.first_bridge_on = first_bridge_on;
        this.bridge_stamp = bridge_stamp;
        this.context = context;
        this.bleg_uuid = bleg_uuid;
        this.accountcode = accountcode;
        this.read_codec = read_codec;
        this.write_codec = write_codec;
        this.sip_hangup_disposition = sip_hangup_disposition;
        this.ani = ani;
        this.short_channel_name = short_channel_name;
        this.channel_register_user = channel_register_user;
        this.cc_queue = cc_queue;
        this.cc_agent_name = cc_agent_name;
        this.cc_agent = cc_agent;
        this.cc_side = cc_side;
        this.cc_queue_answered_epoch = cc_queue_answered_epoch;
        this.cc_queue_times = cc_queue_times;
        this.cc_queue_terminated_epoch = cc_queue_terminated_epoch;
        this.cc_queue_joined_epoch = cc_queue_joined_epoch;
        this.cc_queue_canceled_epoch = cc_queue_canceled_epoch;
        this.cc_ringbacktimes = cc_ringbacktimes;
        this.obpjnum = obpjnum;
        this.pjjobid = pjjobid;
        this.obc_taskid = obc_taskid;
        this.obc_prjcode = obc_prjcode;
        this.server_name = server_name;
        this.ifqa = ifqa;
        this.callin_process_mode = callin_process_mode;
        this.caseid = caseid;
        this.cusid = cusid;
        this.ifplayedleavemsg = ifplayedleavemsg;
        this.relative_uuid = relative_uuid;
        this.reqstate = reqstate;
        this.reqcount = reqcount;
        this.userkey_f10 = userkey_f10;
        this.area_code = area_code;
    }

    public String getArea_code() {
        return area_code;
    }

    public void setArea_code(String area_code) {
        this.area_code = area_code;
    }
    //    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }

    public String getFirst_real_caller() {
        return first_real_caller;
    }

    public void setFirst_real_caller(String first_real_caller) {
        this.first_real_caller = first_real_caller == null ? null : first_real_caller.trim();
    }

    public String getFirst_real_callee() {
        return first_real_callee;
    }

    public void setFirst_real_callee(String first_real_callee) {
        this.first_real_callee = first_real_callee == null ? null : first_real_callee.trim();
    }

    public String getReal_caller() {
        return real_caller;
    }

    public void setReal_caller(String real_caller) {
        this.real_caller = real_caller == null ? null : real_caller.trim();
    }

    public String getReal_callee() {
        return real_callee;
    }

    public void setReal_callee(String real_callee) {
        this.real_callee = real_callee == null ? null : real_callee.trim();
    }

    public String getCaller_id_number() {
        return caller_id_number;
    }

    public void setCaller_id_number(String caller_id_number) {
        this.caller_id_number = caller_id_number == null ? null : caller_id_number.trim();
    }

    public String getCallee_id_number() {
        return callee_id_number;
    }

    public void setCallee_id_number(String callee_id_number) {
        this.callee_id_number = callee_id_number == null ? null : callee_id_number.trim();
    }

    public String getCr_destination() {
        return cr_destination;
    }

    public void setCr_destination(String cr_destination) {
        this.cr_destination = cr_destination == null ? null : cr_destination.trim();
    }

    public String getStart_stamp() {
        return start_stamp;
    }

    public void setStart_stamp(String start_stamp) {
        this.start_stamp = start_stamp == null ? null : start_stamp.trim();
    }

    public String getAnswer_stamp() {
        return answer_stamp;
    }

    public void setAnswer_stamp(String answer_stamp) {
        this.answer_stamp = answer_stamp == null ? null : answer_stamp.trim();
    }

    public String getEnd_stamp() {
        return end_stamp;
    }

    public void setEnd_stamp(String end_stamp) {
        this.end_stamp = end_stamp == null ? null : end_stamp.trim();
    }

    public Integer getLinktimes() {
        return linktimes;
    }

    public void setLinktimes(Integer linktimes) {
        this.linktimes = linktimes;
    }

    public Integer getBillsec() {
        return billsec;
    }

    public void setBillsec(Integer billsec) {
        this.billsec = billsec;
    }

    public Integer getIvrtime() {
        return ivrtime;
    }

    public void setIvrtime(Integer ivrtime) {
        this.ivrtime = ivrtime;
    }

    public Integer getRingtimes() {
        return ringtimes;
    }

    public void setRingtimes(Integer ringtimes) {
        this.ringtimes = ringtimes;
    }

    public String getFirst_bridge_uuid() {
        return first_bridge_uuid;
    }

    public void setFirst_bridge_uuid(String first_bridge_uuid) {
        this.first_bridge_uuid = first_bridge_uuid == null ? null : first_bridge_uuid.trim();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid == null ? null : uuid.trim();
    }

    public String getRecord_filepath() {
        return record_filepath;
    }

    public void setRecord_filepath(String record_filepath) {
        this.record_filepath = record_filepath == null ? null : record_filepath.trim();
    }

    public String getRecord_start() {
        return record_start;
    }

    public void setRecord_start(String record_start) {
        this.record_start = record_start == null ? null : record_start.trim();
    }

    public String getLeavemsg_filepath() {
        return leavemsg_filepath;
    }

    public void setLeavemsg_filepath(String leavemsg_filepath) {
        this.leavemsg_filepath = leavemsg_filepath == null ? null : leavemsg_filepath.trim();
    }

    public String getLeavemsg_start() {
        return leavemsg_start;
    }

    public void setLeavemsg_start(String leavemsg_start) {
        this.leavemsg_start = leavemsg_start == null ? null : leavemsg_start.trim();
    }

    public String getCallout_hungup_cause() {
        return callout_hungup_cause;
    }

    public void setCallout_hungup_cause(String callout_hungup_cause) {
        this.callout_hungup_cause = callout_hungup_cause == null ? null : callout_hungup_cause.trim();
    }

    public String getHangup_cause() {
        return hangup_cause;
    }

    public void setHangup_cause(String hangup_cause) {
        this.hangup_cause = hangup_cause == null ? null : hangup_cause.trim();
    }

    public String getIvrid_e() {
        return ivrid_e;
    }

    public void setIvrid_e(String ivrid_e) {
        this.ivrid_e = ivrid_e == null ? null : ivrid_e.trim();
    }

    public String getIvrid_s() {
        return ivrid_s;
    }

    public void setIvrid_s(String ivrid_s) {
        this.ivrid_s = ivrid_s == null ? null : ivrid_s.trim();
    }

    public String getSatisfaction_ivrid() {
        return satisfaction_ivrid;
    }

    public void setSatisfaction_ivrid(String satisfaction_ivrid) {
        this.satisfaction_ivrid = satisfaction_ivrid == null ? null : satisfaction_ivrid.trim();
    }

    public String getCompany_code() {
        return company_code;
    }

    public void setCompany_code(String company_code) {
        this.company_code = company_code == null ? null : company_code.trim();
    }

    public Integer getA_calltype() {
        return a_calltype;
    }

    public void setA_calltype(Integer a_calltype) {
        this.a_calltype = a_calltype;
    }

    public Integer getB_calltype() {
        return b_calltype;
    }

    public void setB_calltype(Integer b_calltype) {
        this.b_calltype = b_calltype;
    }

    public Integer getFirst_a_calltype() {
        return first_a_calltype;
    }

    public void setFirst_a_calltype(Integer first_a_calltype) {
        this.first_a_calltype = first_a_calltype;
    }

    public Integer getFirst_b_calltype() {
        return first_b_calltype;
    }

    public void setFirst_b_calltype(Integer first_b_calltype) {
        this.first_b_calltype = first_b_calltype;
    }

    public String getCall_tracks() {
        return call_tracks;
    }

    public void setCall_tracks(String call_tracks) {
        this.call_tracks = call_tracks == null ? null : call_tracks.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getPlatform_number() {
        return platform_number;
    }

    public void setPlatform_number(String platform_number) {
        this.platform_number = platform_number == null ? null : platform_number.trim();
    }

    public String getCc_sys_agent_id() {
        return cc_sys_agent_id;
    }

    public void setCc_sys_agent_id(String cc_sys_agent_id) {
        this.cc_sys_agent_id = cc_sys_agent_id == null ? null : cc_sys_agent_id.trim();
    }

    public String getF_uuid() {
        return f_uuid;
    }

    public void setF_uuid(String f_uuid) {
        this.f_uuid = f_uuid == null ? null : f_uuid.trim();
    }

    public String getSatisfaction_f1() {
        return satisfaction_f1;
    }

    public void setSatisfaction_f1(String satisfaction_f1) {
        this.satisfaction_f1 = satisfaction_f1 == null ? null : satisfaction_f1.trim();
    }

    public String getSatisfaction_f2() {
        return satisfaction_f2;
    }

    public void setSatisfaction_f2(String satisfaction_f2) {
        this.satisfaction_f2 = satisfaction_f2 == null ? null : satisfaction_f2.trim();
    }

    public String getSatisfaction_f3() {
        return satisfaction_f3;
    }

    public void setSatisfaction_f3(String satisfaction_f3) {
        this.satisfaction_f3 = satisfaction_f3 == null ? null : satisfaction_f3.trim();
    }

    public String getSatisfaction_f4() {
        return satisfaction_f4;
    }

    public void setSatisfaction_f4(String satisfaction_f4) {
        this.satisfaction_f4 = satisfaction_f4 == null ? null : satisfaction_f4.trim();
    }

    public String getSatisfaction_f5() {
        return satisfaction_f5;
    }

    public void setSatisfaction_f5(String satisfaction_f5) {
        this.satisfaction_f5 = satisfaction_f5 == null ? null : satisfaction_f5.trim();
    }

    public String getSatisfaction_f6() {
        return satisfaction_f6;
    }

    public void setSatisfaction_f6(String satisfaction_f6) {
        this.satisfaction_f6 = satisfaction_f6 == null ? null : satisfaction_f6.trim();
    }

    public String getSatisfaction_f7() {
        return satisfaction_f7;
    }

    public void setSatisfaction_f7(String satisfaction_f7) {
        this.satisfaction_f7 = satisfaction_f7 == null ? null : satisfaction_f7.trim();
    }

    public String getSatisfaction_f8() {
        return satisfaction_f8;
    }

    public void setSatisfaction_f8(String satisfaction_f8) {
        this.satisfaction_f8 = satisfaction_f8 == null ? null : satisfaction_f8.trim();
    }

    public String getSatisfaction_f9() {
        return satisfaction_f9;
    }

    public void setSatisfaction_f9(String satisfaction_f9) {
        this.satisfaction_f9 = satisfaction_f9 == null ? null : satisfaction_f9.trim();
    }

    public String getSatisfaction_f10() {
        return satisfaction_f10;
    }

    public void setSatisfaction_f10(String satisfaction_f10) {
        this.satisfaction_f10 = satisfaction_f10 == null ? null : satisfaction_f10.trim();
    }

    public String getUserkey_f1() {
        return userkey_f1;
    }

    public void setUserkey_f1(String userkey_f1) {
        this.userkey_f1 = userkey_f1 == null ? null : userkey_f1.trim();
    }

    public String getUserkey_f2() {
        return userkey_f2;
    }

    public void setUserkey_f2(String userkey_f2) {
        this.userkey_f2 = userkey_f2 == null ? null : userkey_f2.trim();
    }

    public String getUserkey_f3() {
        return userkey_f3;
    }

    public void setUserkey_f3(String userkey_f3) {
        this.userkey_f3 = userkey_f3 == null ? null : userkey_f3.trim();
    }

    public String getUserkey_f4() {
        return userkey_f4;
    }

    public void setUserkey_f4(String userkey_f4) {
        this.userkey_f4 = userkey_f4 == null ? null : userkey_f4.trim();
    }

    public String getUserkey_f5() {
        return userkey_f5;
    }

    public void setUserkey_f5(String userkey_f5) {
        this.userkey_f5 = userkey_f5 == null ? null : userkey_f5.trim();
    }

    public String getUserkey_f6() {
        return userkey_f6;
    }

    public void setUserkey_f6(String userkey_f6) {
        this.userkey_f6 = userkey_f6 == null ? null : userkey_f6.trim();
    }

    public String getUserkey_f7() {
        return userkey_f7;
    }

    public void setUserkey_f7(String userkey_f7) {
        this.userkey_f7 = userkey_f7 == null ? null : userkey_f7.trim();
    }

    public String getUserkey_f8() {
        return userkey_f8;
    }

    public void setUserkey_f8(String userkey_f8) {
        this.userkey_f8 = userkey_f8 == null ? null : userkey_f8.trim();
    }

    public String getUserkey_f9() {
        return userkey_f9;
    }

    public void setUserkey_f9(String userkey_f9) {
        this.userkey_f9 = userkey_f9 == null ? null : userkey_f9.trim();
    }

    public String getLocal_ip_v4() {
        return local_ip_v4;
    }

    public void setLocal_ip_v4(String local_ip_v4) {
        this.local_ip_v4 = local_ip_v4 == null ? null : local_ip_v4.trim();
    }

    public String getCaller_id_name() {
        return caller_id_name;
    }

    public void setCaller_id_name(String caller_id_name) {
        this.caller_id_name = caller_id_name == null ? null : caller_id_name.trim();
    }

    public String getUsrphone() {
        return usrphone;
    }

    public void setUsrphone(String usrphone) {
        this.usrphone = usrphone == null ? null : usrphone.trim();
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction == null ? null : direction.trim();
    }

    public String getUsrphone_prefix() {
        return usrphone_prefix;
    }

    public void setUsrphone_prefix(String usrphone_prefix) {
        this.usrphone_prefix = usrphone_prefix == null ? null : usrphone_prefix.trim();
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getRingbacktimes() {
        return ringbacktimes;
    }

    public void setRingbacktimes(Integer ringbacktimes) {
        this.ringbacktimes = ringbacktimes;
    }

    public Integer getFirst_bridge_on() {
        return first_bridge_on;
    }

    public void setFirst_bridge_on(Integer first_bridge_on) {
        this.first_bridge_on = first_bridge_on;
    }

    public String getBridge_stamp() {
        return bridge_stamp;
    }

    public void setBridge_stamp(String bridge_stamp) {
        this.bridge_stamp = bridge_stamp == null ? null : bridge_stamp.trim();
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context == null ? null : context.trim();
    }

    public String getBleg_uuid() {
        return bleg_uuid;
    }

    public void setBleg_uuid(String bleg_uuid) {
        this.bleg_uuid = bleg_uuid == null ? null : bleg_uuid.trim();
    }

    public String getAccountcode() {
        return accountcode;
    }

    public void setAccountcode(String accountcode) {
        this.accountcode = accountcode == null ? null : accountcode.trim();
    }

    public String getRead_codec() {
        return read_codec;
    }

    public void setRead_codec(String read_codec) {
        this.read_codec = read_codec == null ? null : read_codec.trim();
    }

    public String getWrite_codec() {
        return write_codec;
    }

    public void setWrite_codec(String write_codec) {
        this.write_codec = write_codec == null ? null : write_codec.trim();
    }

    public String getSip_hangup_disposition() {
        return sip_hangup_disposition;
    }

    public void setSip_hangup_disposition(String sip_hangup_disposition) {
        this.sip_hangup_disposition = sip_hangup_disposition == null ? null : sip_hangup_disposition.trim();
    }

    public String getAni() {
        return ani;
    }

    public void setAni(String ani) {
        this.ani = ani == null ? null : ani.trim();
    }

    public String getShort_channel_name() {
        return short_channel_name;
    }

    public void setShort_channel_name(String short_channel_name) {
        this.short_channel_name = short_channel_name == null ? null : short_channel_name.trim();
    }

    public String getChannel_register_user() {
        return channel_register_user;
    }

    public void setChannel_register_user(String channel_register_user) {
        this.channel_register_user = channel_register_user == null ? null : channel_register_user.trim();
    }

    public String getCc_queue() {
        return cc_queue;
    }

    public void setCc_queue(String cc_queue) {
        this.cc_queue = cc_queue == null ? null : cc_queue.trim();
    }

    public String getCc_agent_name() {
        return cc_agent_name;
    }

    public void setCc_agent_name(String cca_gent_name) {
        this.cc_agent_name = cc_agent_name == null ? null : cc_agent_name.trim();
    }

    public String getCc_agent() {
        return cc_agent;
    }

    public void setCc_agent(String cc_agent) {
        this.cc_agent = cc_agent == null ? null : cc_agent.trim();
    }

    public String getCc_side() {
        return cc_side;
    }

    public void setCc_side(String cc_side) {
        this.cc_side = cc_side == null ? null : cc_side.trim();
    }

    public Long getCc_queue_answered_epoch() {
        return cc_queue_answered_epoch;
    }

    public void setCc_queue_answered_epoch(Long cc_queue_answered_epoch) {
        this.cc_queue_answered_epoch = cc_queue_answered_epoch;
    }

    public Integer getCc_queue_times() {
        return cc_queue_times;
    }

    public void setCc_queue_times(Integer cc_queue_times) {
        this.cc_queue_times = cc_queue_times;
    }

    public Long getCc_queue_terminated_epoch() {
        return cc_queue_terminated_epoch;
    }

    public void setCc_queue_terminated_epoch(Long cc_queue_terminated_epoch) {
        this.cc_queue_terminated_epoch = cc_queue_terminated_epoch;
    }

    public Long getCc_queue_Joined_epoch() {
        return cc_queue_joined_epoch;
    }

    public void setCc_queue_joined_epoch(Long cc_queue_joined_epoch) {
        this.cc_queue_joined_epoch = cc_queue_joined_epoch;
    }

    public Long getCc_queue_canceled_epoch() {
        return cc_queue_canceled_epoch;
    }

    public void setCc_queue_canceled_epoch(Long cc_queue_canceled_epoch) {
        this.cc_queue_canceled_epoch = cc_queue_canceled_epoch;
    }

    public Integer getCc_ringbacktimes() {
        return cc_ringbacktimes;
    }

    public void setCc_ringbacktimes(Integer cc_ringbacktimes) {
        this.cc_ringbacktimes = cc_ringbacktimes;
    }

    public String getObpjnum() {
        return obpjnum;
    }

    public void setObpjnum(String obpjnum) {
        this.obpjnum = obpjnum == null ? null : obpjnum.trim();
    }

    public Integer getPjjobid() {
        return pjjobid;
    }

    public void setPjjobid(Integer pjjobid) {
        this.pjjobid = pjjobid;
    }

    public String getObc_taskid() {
        return obc_taskid;
    }

    public void setObc_taskid(String obc_taskid) {
        this.obc_taskid = obc_taskid == null ? null : obc_taskid.trim();
    }

    public String getObc_prjcode() {
        return obc_prjcode;
    }

    public void setObc_prjcode(String obc_prjcode) {
        this.obc_prjcode = obc_prjcode == null ? null : obc_prjcode.trim();
    }

    public String getServer_name() {
        return server_name;
    }

    public void setServer_name(String server_name) {
        this.server_name = server_name == null ? null : server_name.trim();
    }

    public Integer getIfqa() {
        return ifqa;
    }

    public void setIfqa(Integer ifqa) {
        this.ifqa = ifqa;
    }

    public String getCallin_process_mode() {
        return callin_process_mode;
    }

    public void setCallin_process_mode(String callinProcessMode) {
        this.callin_process_mode = callin_process_mode == null ? null : callin_process_mode.trim();
    }

    public String getCaseid() {
        return caseid;
    }

    public void setCaseid(String caseid) {
        this.caseid = caseid == null ? null : caseid.trim();
    }

    public String getCusid() {
        return cusid;
    }

    public void setCusid(String cusid) {
        this.cusid = cusid == null ? null : cusid.trim();
    }

    public String getIfplayedleavemsg() {
        return ifplayedleavemsg;
    }

    public void setIfplayedleavemsg(String ifplayedleavemsg) {
        this.ifplayedleavemsg = ifplayedleavemsg == null ? null : ifplayedleavemsg.trim();
    }

    public String getRelative_uuid() {
        return relative_uuid;
    }

    public void setRelative_uuid(String relative_uuid) {
        this.relative_uuid = relative_uuid == null ? null : relative_uuid.trim();
    }

    public Integer getReqstate() {
        return reqstate;
    }

    public void setReqstate(Integer reqstate) {
        this.reqstate = reqstate;
    }

    public Integer getReqcount() {
        return reqcount;
    }

    public void setReqcount(Integer reqcount) {
        this.reqcount = reqcount;
    }

    public String getUserkey_f10() {
        return userkey_f10;
    }

    public void setUserkey_f10(String userkey_f10) {
        this.userkey_f10 = userkey_f10 == null ? null : userkey_f10.trim();
    }
}