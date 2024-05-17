package com.cqt.ivr.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cqt.feign.freeswitch.FreeswitchApiFeignClient;
import com.cqt.ivr.config.nacos.DynamicConfig;
import com.cqt.ivr.entity.Elevaluebase;
import com.cqt.ivr.entity.Ivrbase;
import com.cqt.ivr.entity.ReqParabase;
import com.cqt.ivr.mapper.IvrFsdtbMapper;
import com.cqt.ivr.service.IIvrFsdtbService;
import com.cqt.ivr.utils.IvrSqldo;
import com.cqt.ivr.utils.Parsefdata;
import com.cqt.ivr.utils.StringUtil;
import com.cqt.ivr.utils.UT;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * @author xinson
 * @since 2023-07-13
 */
@Service
@Slf4j
public class IvrFsdtbServiceImpl implements IIvrFsdtbService {

    @Resource
    private DynamicConfig dynamicConfig;

    @Resource
    private IvrFsdtbMapper ivrFsdtbMapper;

    @Resource
    private  RedissonUtil redissonUtilL;

    @Resource
    private IvrSqldo ivrSqldo;

    @Override
    public ArrayList<String> getAllCompanyCode() {
        return ivrFsdtbMapper.getAllCompanyCode();
    }

    @Resource
    FreeswitchApiFeignClient freeswitchApiFeignClient;

    @Override
    public String tojson(Map<String, String[]> reqpara, Map<String, Object> data, HttpServletResponse response, String LOG_TAG) {
        JSONObject result = new JSONObject();
        String status = "false";
        String info = "";
        String errMsg = "";
        if (reqpara != null) {
            ReqParabase para = new ReqParabase();
            log.info(LOG_TAG + "reqpara:" + reqpara);
            UT.df_setbean(reqpara, para);
            log.info(LOG_TAG + "para:" + para);
            try {
                //respan_all据了解不使用
                if ("respan_all".equals(para.getAction())){
                    ArrayList<String> contents = new ArrayList<String>();
                    try {
                        if (UT.zstr(para.getCompany_ids())){
                            contents = ivrFsdtbMapper.getAllCompanyCode();
                        }else {
                            String[] companyCodeArr = para.getCompany_ids().split(",");
                            contents = new ArrayList<>(Arrays.asList(companyCodeArr));
                        }
                    }catch (Exception e){
                        errMsg = "数据库查无企业数据库信息";
                        log.error(LOG_TAG + "企业信息查询数据库异常：", e);
                    }
                    if("".equals(errMsg)){
                        //System.out.println("company_size:"+contents.size());
                        if("ivr".equals(para.getType())){
                            //delete("C:/Users/Administrator/Desktop/test/h/src/",null);
                            for (int i = 0; i < contents.size(); i++) {
                                String companyCode = contents.get(i);
                                if (!UT.zstr(companyCode)){
                                    JSONObject jobj = this.tojsonGFD(null, companyCode, LOG_TAG);
                                    status = jobj.getString("success");
                                    if("true".equals(status)){
                                        info += jobj.getString("info");
                                    } else {
                                        errMsg += jobj.getString("error");
                                    }
                                }
                            }
                        }else{
                            errMsg += "type error";
                        }
                    }
                } else if (UT.zstr(para.getCompany_code())){
                    errMsg += "企业code为空";
                }else{
                    ArrayList<String> contents = new ArrayList<String>();
                    try {
                        String[] companyCodeArr = para.getCompany_code().split(",");
                        contents = new ArrayList<>(Arrays.asList(companyCodeArr));
                    }catch (Exception e){
                        errMsg = "数据库查无企业数据库信息";
                        log.error(LOG_TAG + "企业信息查询数据库异常：", e);
                    }
                    if("".equals(errMsg)){
                        if(contents.size()<1){
                            errMsg += "企业不存在";
                        }else {
                            //System.out.println(cpn_db_prefix+dbinfo.getCompany_code());
                            if ("ivr".equals(para.getAction())){
                                if("save".equals(para.getType())){
                                    String[] ivrids = null;
                                    if(!UT.zstr(para.getIvr_ids())){
                                        ivrids = para.getIvr_ids().split(",");
                                    }
                                    JSONObject jobj = this.tojsonGFD("all".equals(para.getIvr_ids())?null:ivrids, contents.get(0), LOG_TAG);
                                    //System.out.println(para.getIvr_ids());
                                    status = jobj.getString("success");
                                    if("true".equals(status)){
                                        info = jobj.getString("info");
                                    } else{
                                        errMsg += jobj.getString("error");
                                    }
                                }else if("delete".equals(para.getType())){
                                    String[] ivrids = null;
                                    if(!UT.zstr(para.getIvr_ids())){
                                        ivrids = para.getIvr_ids().split(",");
                                    }
                                    if(ivrids==null||"all".equals(para.getIvr_ids())){
                                        //delete("C:/Users/Administrator/Desktop/test/h/src/",dbinfo.getCompany_code()+"_");
                                        errMsg += "not support delete all";
                                    } else {
                                        for (int i = 0; i < ivrids.length; i++) {
                                            //delete("C:/Users/Administrator/Desktop/test/h/src/"+ivrids[i]+".lua",null);
                                            //需调用底层接口删除脚本
                                        }
                                    }
                                }else{
                                    errMsg += "type error";
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                //e.printStackTrace();
                status = "false";
                errMsg += e.getLocalizedMessage();
                log.error(LOG_TAG + "创建ivr异常：", e);
            }
        }
        if("".equals(errMsg)){
            status = "true";
        } else {
            status = "false";
        }
        result.put("success", status);
        result.put("info", info);
        result.put("error", errMsg);

        return result.toJSONString();
    }

    public JSONObject tojsonGFD(String[] ivrids, String companyCode, String LOG_TAG){
        ArrayList<Ivrbase> contents = new ArrayList<Ivrbase>();
        String errormsg = "";
        JSONObject result = new JSONObject();
        try {
            contents = ivrFsdtbMapper.ivrbaseSelect(ivrSqldo.flowdataselsql(ivrids, LOG_TAG));
        }catch (Exception e){
            log.error(LOG_TAG + "查找ivrbase异常：", e);
            errormsg += UT.errormsg(e);
        }
        log.info(LOG_TAG + "Ivrbase contents:" + contents);
        if(errormsg.equals("") && contents.size() > 0){
            ArrayList<Elevaluebase> elevaluebase = new ArrayList<Elevaluebase>();
            try {
                elevaluebase = ivrFsdtbMapper.elevaluebaseSelect(ivrSqldo.elevaluesselsql(ivrids, LOG_TAG));
            }catch (Exception e){
                log.error(LOG_TAG + "查找ivrbase异常：", e);
                errormsg += UT.errormsg(e);
            }
            log.info(LOG_TAG + "elevaluebase:" + elevaluebase);
            String errmsg = "";
            if(errormsg.equals("") && elevaluebase.size() > 0){
                //System.out.println(mapper.writeValueAsString(contents));
                StringBuffer info = new StringBuffer();
                for(Ivrbase content:contents){
                    ArrayList<Elevaluebase> tmp = new ArrayList<Elevaluebase>();
                    for(Elevaluebase t:elevaluebase){
                        if(t.getIvrid().equals(content.getIvrid())){
                            tmp.add(t);
                        }
                    }
                    Parsefdata pd = new Parsefdata(companyCode);
                    log.info(LOG_TAG + "content.getFlowdata():" + content.getFlowdata());
                    errmsg = pd.parsedata(content.getFlowdata(), tmp, LOG_TAG);
                    if("".equals(errmsg)){
                        errmsg = pd.createivr(content.getIvrid(), content.getIvrname(), content.getIvrid(), LOG_TAG, content.getFlowdata(), companyCode,freeswitchApiFeignClient, dynamicConfig, redissonUtilL);
                    }
                    if(!errmsg.contains("异常")){
                        info.append(content.getIvrid()+":success:"+errmsg);
                    } else {
                        info.append(content.getIvrid()+":failed:"+errmsg);
                    }
                    info.append("||");
                }
                log.info(LOG_TAG + "info:" + info);
                if(!errmsg.contains("异常")){
                    this.setjsonres(result,"true",info.toString(),errormsg);
                }else {
                    this.setjsonres(result,"false","",errmsg);
                }
            }else {
                if (StringUtil.isEmpty(errormsg)){
                    errormsg = "ivrids错误，查找不到相关信息";
                }
                log.info(LOG_TAG + "err:"+errormsg);
                this.setjsonres(result,"false","",errormsg);
            }
        }else{
            if (StringUtil.isEmpty(errormsg)){
                errormsg = "ivrids错误，查找不到相关信息";
            }
            log.info(LOG_TAG + "err:"+errormsg);
            this.setjsonres(result,"false","",errormsg);
        }
        return result;
    }

    private void setjsonres(JSONObject result, String success, String info, String error){
        result.put("success", success);
        result.put("info", info);
        result.put("data", "");
        result.put("errorcode", "");
        result.put("error", error);
    }

}
