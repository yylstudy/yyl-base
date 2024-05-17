package com.cqt.ivr.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cqt.feign.freeswitch.FreeswitchApiFeignClient;
import com.cqt.ivr.config.nacos.DynamicConfig;
import com.cqt.ivr.entity.Elevaluebase;
import com.cqt.ivr.entity.GFLine;
import com.cqt.ivr.entity.GFNode;
import com.cqt.model.freeswitch.dto.api.LuaWriteDTO;
import com.cqt.model.freeswitch.vo.LuaWriteVO;
import com.cqt.starter.redis.util.RedissonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Parsefdata {

    private static final Logger log = LoggerFactory.getLogger(Parsefdata.class);

    private HashMap<String, GFNode> nodes;
    private HashMap<String, GFLine> lines;
    private HashMap<String, GFNode> unodes;
    private HashMap<String, GFLine> ulines;
    private ArrayList<GFNode> start;
    private ArrayList<GFNode> end;
    private StringBuffer sb;
    private ArrayList<String> allvar;
    private ArrayList<StringBuffer> allfunc;
    private HashMap<String, luafunc> routes;
    private String errmsg;
    private String companyCode;

    private final String endfunc = "function endfunc()\r\n"
            + "if not session:ready() then return end\r\n"
            //+"session:setVariable(\"userkey_f10\",dtmfdata)\r\n"
            + "ivr_tracks = ivr_tracks..\"  ——结束_\"" + "..os.date(\"%Y%m%d%H%M%S\",os.time())\r\n"
            + "session:setVariable(\"ivr_tracks\",ivr_tracks)\r\n"
            + "session:hangup()\r\n"
            + "end\r\n";

    static private class luafunc {
        public String funcname;
        public GFNode fnode;
        public HashMap<GFLine, GFNode> toroutes;

        public luafunc(String funcname, GFNode fnode, String prenode, HashMap<GFLine, GFNode> toroutes) {
            super();
            this.funcname = funcname;
            this.fnode = fnode;
            this.toroutes = toroutes;
        }
    }

    public Parsefdata(String companycode) {
        nodes = new HashMap<String, GFNode>();
        lines = new HashMap<String, GFLine>();
        unodes = new HashMap<String, GFNode>();
        ulines = new HashMap<String, GFLine>();
        start = new ArrayList<GFNode>();
        end = new ArrayList<GFNode>();
        sb = new StringBuffer();
        allvar = new ArrayList<String>();
        allfunc = new ArrayList<StringBuffer>();
        routes = new HashMap<String, luafunc>();
        errmsg = "";
        companyCode = companycode;
        //parsedata(gfdata,elevaluebase);
    }

    public String parsedata(String gfdata, ArrayList<Elevaluebase> elevaluebase, String LOG_TAG) {
        try {
            log.info(LOG_TAG + "gfdata:" + gfdata);
            //配置节点
            String nodes = gfdata.substring(gfdata.indexOf(",\"nodes\":{") + 10, gfdata.indexOf("},\"lines\":{"));
            //连接线
            String lines = gfdata.substring(gfdata.indexOf(",\"lines\":{") + 10, gfdata.indexOf("},\"areas\":{"));
            String[] t1 = nodes.split("\\},");
            String[] t2 = lines.split("\\},");
            for (int i = 0; i < t1.length; i++) {
                GFNode node = new GFNode();
                //System.out.println(t1[i]);
                node.setNodeid(t1[i].substring(1, t1[i].indexOf("\":{\"")));
                node.setType(getvaluebytag("type", t1[i]));
                node.setParas(new HashMap<String, String>());
                log.info(LOG_TAG + "node.getNodeid():" + node.getNodeid() + "  node.getType():" + node.getType());
                //boolean j = false, k=false;
                //节点匹配的配置详情数m
                int m = 0;
                ArrayList<Elevaluebase> checked = new ArrayList<Elevaluebase>();
                for (Elevaluebase t3 : elevaluebase) {
                    if (t3.getNodeid().equals(node.getNodeid())) {
                        if (m == 0) {
                            node.setApp(t3.getApp());
                        }
                        node.getParas().put(t3.getParaid(), t3.getParavalue());
                        m++;
                        checked.add(t3);
                    }
                }
                log.info(LOG_TAG + "node.getNodeid():" + node.getNodeid() + "  node.getApp():" + node.getApp());
                elevaluebase.removeAll(checked);
                if (node.getType().contains("chat") || node.getType().contains("node") || node.getType().contains("fork")) {
                    this.nodes.put(node.getNodeid(), node);
                } else if (node.getType().contains("start")) {
                    start.add(node);
                } else if (node.getType().contains("end")) {
                    end.add(node);
                }
            }
            for (int i = 0; i < t2.length; i++) {
                //System.out.println(t2[i]);
                GFLine line = new GFLine();
                line.setLineid(t2[i].substring(1, t2[i].indexOf("\":{\"")));
                line.setFrom(getvaluebytag("from", t2[i]));
                line.setTo(getvaluebytag("to", t2[i]));
                line.setParas(new HashMap<String, String>());
                log.info(LOG_TAG + "line.getLineid():" + line.getLineid() + "  line.getFrom():" + line.getFrom() + "  line.getTo():" + line.getTo());
                int m = 0;
                ArrayList<Elevaluebase> checked = new ArrayList<Elevaluebase>();
                for (Elevaluebase t3 : elevaluebase) {
                    if (t3.getNodeid().equals(line.getLineid())) {
                        if (m == 0) {
                            line.setApp(t3.getApp());
                        }
                        line.getParas().put(t3.getParaid(), t3.getParavalue());
                        m++;
                        checked.add(t3);
                        log.info(LOG_TAG + "  t3.getParaid():" + t3.getParaid() + "  t3.getParavalue():" + t3.getParavalue());
                    }
                }
                elevaluebase.removeAll(checked);
                this.lines.put(line.getLineid(), line);
                //System.out.println(n+":"+m);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.info(LOG_TAG + "数据解析错误:", e);
            errmsg = "数据解析错误！";
        }
        return errmsg;
    }

    private String getvaluebytag(String tag, String base) {
        int start = base.indexOf("\"" + tag + "\"");
        if (start < 0) {
            return null;
        }
        String substr = base.substring(start + 2 + tag.length());
        int end = substr.indexOf("\",\"");
        if (end < 1) {
            return null;
        }
        String value = substr.substring(2, end);
        return value;
    }

    private boolean checkstart() {
        if (start.size() != 1) {
            return false;
        }
        return true;
    }

    private void parseroutes(GFNode gfnode, GFNode prenode) {
        ArrayList<GFNode> nextnos = new ArrayList<GFNode>();
        luafunc func = new luafunc(gfnode.getNodeid(), gfnode, prenode == null ? null : prenode.getNodeid(), new HashMap<GFLine, GFNode>());
        unodes.put(gfnode.getNodeid(), gfnode);
        for (GFLine gfli : lines.values()) {
            if (gfli.getFrom().equals(gfnode.getNodeid())) {
                nextnos.add(nodes.get(gfli.getTo()));
                func.toroutes.put(gfli, nodes.get(gfli.getTo()));
                ulines.put(gfli.getLineid(), gfli);
            }
        }
        routes.put(gfnode.getNodeid(), func);
        for (String liname : ulines.keySet()) {
            lines.remove(liname);
        }
        for (GFNode nextno : nextnos) {
            if (nextno != null && !unodes.containsValue(nextno)) {
                parseroutes(nextno, gfnode);
            }
        }
    }

    private String getoperator(String id) {
        switch (id == null ? "" : id) {
            case "1":
                return ">";
            case "2":
                return "<";
            case "3":
                return "==";
            case "4":
                return "~=";
            case "5":
                return ">=";
            case "6":
                return "<=";
            default:
                return ">";
        }
    }

    private String checkStringForint(String nm) {
        try {
            Integer.parseInt(nm);
        } catch (NumberFormatException e) {
            return "1";
        }
        return nm;
    }

    private String checkStringforbs(String str) {
        if (str != null) {
            str = str.replaceAll("\\\\", "\\\\\\\\");
            str = str.replaceAll("\\\"", "\\\\\"");
        }
        return str;
    }

    private ArrayList<String[]> checkvar_cnts(String var_cnts) {
        ArrayList<String[]> ress = new ArrayList<String[]>();
        String[] cnts = var_cnts.trim().split("\\|");
        for (String cnt : cnts) {
            String[] res = new String[2];
            int index = cnt.indexOf("=");
            if (index > -1 && index < cnt.length() - 1) {
                res[0] = cnt.substring(0, index);
                res[1] = checkStringforbs(cnt.substring(index + 1));
                ress.add(res);
            }
        }
        return ress;
    }

    private String checkgotobyvar(StringBuffer fsb, HashMap<String, String> paras, String nextfunc, int k, boolean byfork) {
        String res = "";
        String gotobyvar_conditions = paras.get("gotobyvar_conditions");
        if (UT.zstr(gotobyvar_conditions)) {
            res = nextfunc;
        } else {
            ArrayList<String[]> ress = checkvar_cnts(gotobyvar_conditions);
            for (int i = 0; i < ress.size(); i++) {
                if (i == 0) {
                    if (k > 0) {
                        fsb.append("else");
                    }
                    fsb.append("if ");
                } else {
                    fsb.append(" and ");
                }
                if ("nil".equals(ress.get(i)[1])) {
                    fsb.append("session:getVariable(\"" + ress.get(i)[0] + "\") == " + ress.get(i)[1] + "");
                } else {
                    fsb.append("session:getVariable(\"" + ress.get(i)[0] + "\") == \"" + ress.get(i)[1] + "\"");
                }
            }
            if (ress.size() > 0) {
                fsb.append(" then\r\n");
            }
            fsb.append(nextfunc);
            if (ress.size() > 0) {
                if (!byfork) {
                    fsb.append("else endfunc()\r\n");
                    fsb.append("end\r\n");
                }
            } else {
                res = nextfunc;
            }
        }
        return res;
    }

    private String checkgotoiftime(StringBuffer fsb,HashMap<String, String> paras, String nextfunc, int k, DynamicConfig dynamicConfig, String LOG_TAG){
        String res = "";
        String tid = paras.get("timestrategy");
        if(tid==null||tid.equals("")){
            res = nextfunc;
        }else{
            String serviceTimeUrl = dynamicConfig.getServerTimeUrl() + tid;
            log.info(LOG_TAG + "serviceTimeUrl:" + serviceTimeUrl);
            fsb.append("ivr_httpapi(\"" + serviceTimeUrl + "\",\"POST\", \"\")\r\n");
            fsb.append("local workStatus = session:getVariable(\"workStatus\")\r\n");
            if(k>0){
                fsb.append("else");
            }
            fsb.append("if ");
            fsb.append("workStatus == \"1\"");
            fsb.append(" then\r\n");
            fsb.append(nextfunc);
        }
        log.info(LOG_TAG + "res:"+res);
        return res;
    }

    private void checkunfork(StringBuffer fsb, GFNode gfno, luafunc func, String nodeName) {
        String forelse = "";
        if (func.toroutes.size() > 1) {
            errmsg += (gfno.getNodeid() + ":逻辑错误(size>1)！" + " nodeName:" + nodeName);
            return;
        }
        if (func.toroutes.size() < 1) {
            forelse = "endfunc()\r\n";
        } else {
            Iterator<Map.Entry<GFLine, GFNode>> itr = func.toroutes.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<GFLine, GFNode> entry = itr.next();
                GFLine li = entry.getKey();
                GFNode no = entry.getValue();
                if (li == null) {
                    errmsg += (gfno.getNodeid() + ":逻辑错误(key为空)！" + " nodeName:" + nodeName);
                    return;
                }
                String nextfunc = (no == null ? "endfunc()\r\n" : (no.getNodeid() + "()\r\n"));
                if ("gotobyvar".equals(li.getApp())) {
//					String read_inputvarname = gfno.getParas().get("read_inputvarname");
//					if(!UT.zstr(read_inputvarname)){
//						fsb.append(read_inputvarname+" = tostring(digit)\r\n");
//					}
                    forelse = checkgotobyvar(fsb, li.getParas(), nextfunc, 0, false);
                } else {
                    forelse = nextfunc;
                }
            }
        }
        if (!forelse.equals("")) {
            fsb.append(forelse);
        }
    }

    private void createfunclua(luafunc func, String LOG_TAG, String flowdata, String companyCode, DynamicConfig dynamicConfig, RedissonUtil redissonUtilL) {
        JSONObject nodesObj = null;
        try {
            JSONObject jsonObj = JSON.parseObject(flowdata);
            String nodesStr = jsonObj.getString("nodes");
            log.info(LOG_TAG + "nodesStr:" + nodesStr);
            nodesObj = JSON.parseObject(nodesStr);
        } catch (Exception e) {
            log.info(LOG_TAG + "flowdata转json异常:", e);
        }
        StringBuffer fsb = new StringBuffer();
        fsb.append("function " + func.funcname + "()\r\n");
        fsb.append("if not session:ready() then return end\r\n");
        GFNode gfno = func.fnode;
        if (gfno == null) {
            gfno = start.get(0);
        }
        String forelse = "";
        log.info(LOG_TAG + "gfno:" + gfno);
        log.info(LOG_TAG + gfno.getNodeid() + ":" + gfno.getType());
        String nodeName = "";
        String nodeIdStr = "";
        if (nodesObj != null) {
            try {
                nodeIdStr = nodesObj.getString(gfno.getNodeid());
                JSONObject nodesIdObj = JSON.parseObject(nodeIdStr);
                nodeName = nodesIdObj.getString("name");
            } catch (Exception e) {
                log.info(LOG_TAG + "NodeidStr转json异常:", e);
            }
            log.info(LOG_TAG + "nodeName:" + nodeName);
        }
        if (start.get(0).getNodeid().equals(gfno.getNodeid())){
            fsb.append("local jumpNodeidS = session:getVariable(\"jump_nodeid\")\r\n");
            fsb.append("if jumpNodeidS ~= nil and jumpNodeidS ~= \"\" then\r\n");
            fsb.append("	local var_func_name = loadstring(jump_nodeid..\"()\")\r\n");
            fsb.append("	var_func_name()\r\n");
            fsb.append("	if not session:ready() then return end\r\n");
            fsb.append("	endfunc()\r\n");
            fsb.append("end\r\n");
            fsb.append("ivr_tracks = ivr_tracks..\"  ——开始_\"" + "..os.date(\"%Y%m%d%H%M%S\",os.time())\r\n");
        }else {
            fsb.append("ivr_tracks = ivr_tracks..\"  ——" + gfno.getNodeid() + "(" + nodeName + ")_\"" + "..os.date(\"%Y%m%d%H%M%S\",os.time())\r\n");
        }
        fsb.append("session:setVariable(\"ivr_tracks\",ivr_tracks)\r\n");
        if ("fork".equals(gfno.getType())) {
            //System.out.println(gfno.getApp());
            switch (gfno.getApp() == null ? "" : gfno.getApp()) {
                case "addme":
                    String varname = gfno.getParas().get("varname");
                    if (!UT.zstr(varname)) {
                        varname = "addme_" + gfno.getNodeid() + "_" + varname;
                        allvar.add(varname + " = 1\r\n");
                    }
                    int k = 0;
                    if (func.toroutes.size() < 1) {
                        forelse = "endfunc()\r\n";
                    } else {
                        Iterator<Map.Entry<GFLine, GFNode>> itr = func.toroutes.entrySet().iterator();
                        int n = 0;
                        while (itr.hasNext()) {
                            Map.Entry<GFLine, GFNode> entry = itr.next();
                            GFLine li = entry.getKey();
                            GFNode no = entry.getValue();
                            if (li == null) {
                                errmsg += (gfno.getNodeid() + ":逻辑错误(key为空)！" + " nodeName:" + nodeName);
                                return;
                            }
                            String nextfunc = (no == null ? "endfunc()\r\n" : (no.getNodeid() + "()\r\n"));
                            if ("gotoifaddme".equals(li.getApp())) {
                                if (UT.zstr(varname)) {
                                    errmsg += (gfno.getNodeid() + ":参数名为空！" + " nodeName:" + nodeName);
                                    return;
                                }
                                String gotoif_operator_addme = li.getParas().get("gotoif_operator_addme");
                                String gotoif_varname = li.getParas().get("gotoif_varname");
                                String gotoifreadkeyvalue_addme = li.getParas().get("gotoifreadkeyvalue_addme");
                                if (UT.zstr(gotoif_operator_addme) || UT.zstr(gotoif_varname) || UT.zstr(gotoifreadkeyvalue_addme)) {
                                    n++;
                                    log.info(LOG_TAG + gfno.getNodeid() + ":" + "gotoifaddme" + " nodeName:" + nodeName);
                                    forelse = nextfunc;
                                } else {
                                    gotoif_varname = "addme_" + gfno.getNodeid() + "_" + gotoif_varname;
                                    if (k > 0) {
                                        fsb.append("else");
                                    }
                                    fsb.append("if " + gotoif_varname + " " + getoperator(gotoif_operator_addme) + " " + checkStringForint(gotoifreadkeyvalue_addme) + " then\r\n");
                                    fsb.append(varname + " = " + varname + "+ 1\r\n");
                                    fsb.append(nextfunc);
                                    k++;
                                }
                            } else if ("gotobyvar".equals(li.getApp())) {
                                String forelse1 = checkgotobyvar(fsb, li.getParas(), nextfunc, k, true);
                                if (forelse1.equals("")) {
                                    k++;
                                } else {
                                    n++;
                                    log.info(LOG_TAG + gfno.getNodeid() + ":" + "gotobyvar" + " nodeName:" + nodeName);
                                    if (n == 1) {
                                        forelse = forelse1;
                                    }
                                }

                            } else if("gotoiftime".equals(li.getApp())){
                                String forelse1 = checkgotoiftime(fsb, li.getParas(), nextfunc, k, dynamicConfig, LOG_TAG);
                                if(forelse1.equals("")){
                                    k++;
                                } else{
                                    n++;
                                    log.info(LOG_TAG + gfno.getNodeid()+":"+"gotoiftime" + " nodeName:" + nodeName);
                                    if(n==1){
                                        forelse = forelse1;
                                    }
                                }
                            } else {
                                n++;
                                forelse = nextfunc;
                            }
                            if (n > 1) {
                                //System.out.println("n>1");
                                errmsg += (gfno.getNodeid() + ":逻辑错误(n>1)！" + " nodeName:" + nodeName);
                                return;
                            }
                        }
                    }
                    System.out.println("addme:" + forelse);
                    if (k > 0) {
                        fsb.append("else\r\n");
                    }
                    if (varname != null && !varname.equals("")) {
                        fsb.append(varname + " = " + varname + "+ 1\r\n");
                    }
                    fsb.append(forelse.equals("") ? "endfunc()\r\n" : forelse);
                    if (k > 0) {
                        fsb.append("end\r\n");
                    }
                    break;
                case "read":
                case "read_curl":
                    String read_playfile, read_max_digits, read_playtimes, read_timeout, userkey_cdrvarname, read_inputvarname, read_dtmfprefix;
                    if ("read".equals(gfno.getApp())) {
                        read_playfile = gfno.getParas().get("read_playfile");
                        read_max_digits = checkStringForint(gfno.getParas().get("read_max_digits"));
                        read_playtimes = checkStringForint(gfno.getParas().get("read_playtimes"));
                        read_timeout = checkStringForint(gfno.getParas().get("read_timeout"));
                        userkey_cdrvarname = gfno.getParas().get("userkey_cdrvarname");
                        read_inputvarname = gfno.getParas().get("read_inputvarname");
                        read_dtmfprefix = gfno.getParas().get("read_dtmfprefix");

                        fsb.append("local digit = ivr_playAndGetDigits_ex(1," + read_max_digits + "," + read_playtimes + "," + read_timeout + ",\"" + read_playfile + "\",nil,\"" + gfno.getNodeid() + "\",\"" + nodeName + "\",\"" + read_dtmfprefix + "\")\r\n");
                    } else {
                        read_playfile = gfno.getParas().get("readvoiceurl");
                        read_max_digits = checkStringForint(gfno.getParas().get("read_curl_max_digits"));
                        read_playtimes = checkStringForint(gfno.getParas().get("read_curl_playtimes"));
                        read_timeout = checkStringForint(gfno.getParas().get("read_curl_timeout"));
                        userkey_cdrvarname = gfno.getParas().get("read_curl_userkey_cdrvarname");
                        read_inputvarname = gfno.getParas().get("read_curl_inputvarname");
                        read_dtmfprefix = gfno.getParas().get("read_curl_dtmfprefix");
                        fsb.append("local digit = \"\"\r\n");
                        fsb.append("local getPlayFile = session:getVariable(\"" + read_playfile + "\")\r\n");
                        fsb.append("if getPlayFile ~= nil then\r\n");
                        fsb.append("    local pos,__ = string.find(getPlayFile,\"http\")\r\n");
                        fsb.append("    if pos and pos < 4 then\r\n");
                        fsb.append("        session:execute(\"playback\",\"space.wav\")\r\n");
                        fsb.append("		digit = ivr_playAndGetDigits_ex(1," + read_max_digits + "," + read_playtimes + "," + read_timeout + ",session:getVariable(\"" + read_playfile + "\"),nil,\"" + gfno.getNodeid() + "\",\"" + nodeName + "\",\"" + read_dtmfprefix + "\")\r\n");
                        fsb.append("    else\r\n");
                        fsb.append("        local tts_engine, tts_voice = \"\", \"\"\r\n");
                        fsb.append("        __,pos = string.find(getPlayFile, \":\")\r\n");
                        fsb.append("        if pos then\r\n");
                        fsb.append("            tts_voice = string.sub(getPlayFile, 1, pos - 1)\r\n");
                        fsb.append("            getPlayFile = string.sub(getPlayFile, pos+1)\r\n");
                        fsb.append("        end\r\n");
                        fsb.append("        __,pos = string.find(getPlayFile, \":\")\r\n");
                        fsb.append("        if pos then\r\n");
                        fsb.append("            tts_engine = string.sub(getPlayFile, 1, pos - 1)\r\n");
                        fsb.append("            getPlayFile = string.sub(getPlayFile, pos+1)\r\n");
                        fsb.append("            session:setVariable(\"tts_engine\", tts_engine)\r\n");
                        fsb.append("            session:setVariable(\"tts_voice\", tts_voice)\r\n");
                        fsb.append("			digit = ivr_playAndGetDigits_ex(1," + read_max_digits + "," + read_playtimes + "," + read_timeout + ",getPlayFile,nil,\"" + gfno.getNodeid() + "\",\"" + nodeName + "\",\"" + read_dtmfprefix + "\")\r\n");
                        fsb.append("        end\r\n");
                        fsb.append("    end\r\n");
                        fsb.append("end\r\n");
                    }

                    if (!UT.zstr(userkey_cdrvarname)) {
                        fsb.append("session:setVariable(\"" + userkey_cdrvarname + "\",digit)\r\n");
                    }
                    if (!UT.zstr(read_inputvarname)) {
                        fsb.append("session:setVariable(\"" + read_inputvarname + "\",digit)\r\n");
                    }
//				if(!UT.zstr(read_dtmfprefix)){
//					fsb.append("if dtmfdata == \"\" then\r\n");
//					fsb.append("dtmfdata = \"(\"..os.date(\"%H:%M:%S\")..\"~"+read_dtmfprefix+"~\"..digit..\")\"\r\n");
//					fsb.append("else\r\n");
//					fsb.append("dtmfdata = dtmfdata..\"(\"..os.date(\"%H:%M:%S\")..\"~"+read_dtmfprefix+"~\"..digit..\")\"\r\n");
//					fsb.append("end\r\n");
//					//fsb.append("session:setVariable(\""+readx_dtmfprefix+"\",digit)\r\n");
//					fsb.append("session:setVariable(\"userkey_f10\",dtmfdata)\r\n");
//				}


                    int k1 = 0;
                    if (func.toroutes.size() < 1) {
                        forelse = "endfunc()\r\n";
                    } else {
                        Iterator<Map.Entry<GFLine, GFNode>> itr1 = func.toroutes.entrySet().iterator();
                        int n = 0;
                        while (itr1.hasNext()) {
                            Map.Entry<GFLine, GFNode> entry = itr1.next();
                            GFLine li = entry.getKey();
                            GFNode no = entry.getValue();
                            if (li == null) {
                                errmsg += (gfno.getNodeid() + ":逻辑错误(key为空)！" + " nodeName:" + nodeName);
                                return;
                            }
                            String nextfunc = (no == null ? "endfunc()\r\n" : (no.getNodeid() + "()\r\n"));
                            if ("gotoifreadkey".equals(li.getApp())) {
                                String gotoifreadkeyvalue = li.getParas().get("gotoifreadkeyvalue");
                                String operator = li.getParas().get("operator");
                                if (UT.zstr(gotoifreadkeyvalue) || UT.zstr(operator)) {
                                    n++;
                                    forelse = nextfunc;
                                    //System.out.println(gfno.getNodeid()+":"+"gotoifreadkey");
                                } else {
                                    if (k1 > 0) {
                                        fsb.append("else");
                                    }
                                    String readkeyvalue = checkStringforbs(gotoifreadkeyvalue);
                                    fsb.append("if digit " + getoperator(operator) + " \"" + readkeyvalue + "\" then\r\n");
                                    fsb.append(nextfunc);
                                    k1++;
                                }
                            } else if ("gotobyvar".equals(li.getApp())) {
                                String forelse1 = checkgotobyvar(fsb, li.getParas(), nextfunc, k1, true);
                                if (forelse1.equals(""))
                                    k1++;
                                else {
                                    n++;
                                    log.info(LOG_TAG + gfno.getNodeid() + ":" + "gotobyvar" + " nodeName:" + nodeName);
                                    if (n == 1)
                                        forelse = forelse1;
                                }
                            } else {
                                n++;
                                forelse = nextfunc;
                            }
                            if (n > 1) {
                                errmsg += (gfno.getNodeid() + ":逻辑错误(n>1)！" + " nodeName:" + nodeName);
                                return;
                            }
                        }
                    }
                    System.out.println("read:" + forelse);
                    if (k1 > 0) {
                        fsb.append("else\r\n");
                    }
                    fsb.append(forelse.equals("") ? "endfunc()\r\n" : forelse);
                    if (k1 > 0) {
                        fsb.append("end\r\n");
                    }
                    break;

                case "httpmorepara":

                    break;
                case "httpapi":
                    String httpapi_inputvarname = gfno.getParas().get("httpapi_inputvarname");
                    String httpapi_method = gfno.getParas().get("httpapi_method");
                    String httpapi_url = gfno.getParas().get("httpapi_url");
                    fsb.append("ivr_httpapi(\"" + httpapi_url + "\",\"" + httpapi_method + "\",\"" + httpapi_inputvarname + "\")\r\n");

                    int k2 = 0;
                    if (func.toroutes.size() < 1) {
                        forelse = "endfunc()\r\n";
                    } else {
                        Iterator<Map.Entry<GFLine, GFNode>> itr2 = func.toroutes.entrySet().iterator();
                        int n = 0;
                        while (itr2.hasNext()) {
                            Map.Entry<GFLine, GFNode> entry = itr2.next();
                            GFLine li = entry.getKey();
                            GFNode no = entry.getValue();
                            if (li == null) {
                                errmsg += (gfno.getNodeid() + ":逻辑错误（key为空）！" + " nodeName:" + nodeName);
                                return;
                            }
                            String nextfunc = (no == null ? "endfunc()\r\n" : (no.getNodeid() + "()\r\n"));
                            if ("gotobyvar".equals(li.getApp())) {
                                String forelse1 = checkgotobyvar(fsb, li.getParas(), nextfunc, k2, true);
                                if (forelse1.equals(""))
                                    k2++;
                                else {
                                    n++;
                                    log.info(LOG_TAG + gfno.getNodeid() + ":" + "gotobyvar" + " nodeName:" + nodeName);
                                    if (n == 1)
                                        forelse = forelse1;
                                }
                                //20190717修改未完成
//						}else if("gotobyqueue".equals(li.getApp())) {
//							String forelse1 = checkgotobyqueue(fsb,li.getParas(),nextfunc,k2,true);
//						}else if("gotobyagent".equals(li.getApp())) {
//							String forelse1 = checkgotobyagent(fsb,li.getParas(),nextfunc,k2,true);
                            } else {
                                n++;
                                forelse = nextfunc;
                            }
                            if (n > 1) {
                                errmsg += (gfno.getNodeid() + ":逻辑错误（n>1）！" + " nodeName:" + nodeName);
                                return;
                            }
                        }
                    }
                    log.info(LOG_TAG + "httpapi:" + forelse);
                    if (k2 > 0) {
                        fsb.append("else\r\n");
                    }
                    fsb.append(forelse.equals("") ? "endfunc()\r\n" : forelse);
                    if (k2 > 0) {
                        fsb.append("end\r\n");
                    }
                    break;
                default:
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
            }
        } else {
            String queueTimeoutKey = "";
            String queueTimeoutValue = "";
            Integer queueTimeout = 60;
            Integer ringTimeout = 60;
            String ringTimeoutKey = "";
            String ringTimeoutValue = "";
            Integer leaveMessageTimeout = 180;
            //System.out.println(gfno.getNodeid()+":"+gfno.getApp());
            switch (gfno.getApp() == null ? "" : gfno.getApp()) {
                case "dialext":
                    String ext = gfno.getParas().get("ext");
                    ringTimeoutKey = "cloudcc:companyInfo:" + companyCode;
                    log.info(LOG_TAG + "ringTimeoutKey：" + ringTimeoutKey);
                    ringTimeoutValue = redissonUtilL.get(ringTimeoutKey);
                    ringTimeout = dynamicConfig.getQueueTimeout();
                    if (!UT.zstr(ringTimeoutValue)) {
                        JSONObject jsons = JSONObject.parseObject(ringTimeoutValue);
                        try {
                            ringTimeout = Integer.parseInt(jsons.get("ringTimeout").toString());
                        }catch (Exception e){
                            log.error(LOG_TAG + "获取ringTimeout失败：", e);
                        }
                    }
                    log.info(LOG_TAG + "ringTimeoutValue：" + ringTimeoutValue);
                    log.info(LOG_TAG + "ringTimeout:" + ringTimeout);
//                    fsb.append("ivr_dialext(\"" + ext + "\")\r\n");
                    fsb.append("ivr_dialext(\"" + ext + "\", \"" + ringTimeout + "\")\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "dialoutline":
                    String outline = checkStringforbs(gfno.getParas().get("outline"));
                    ringTimeout = dynamicConfig.getQueueTimeout();
                    log.info(LOG_TAG + "ringTimeout:" + ringTimeout);
//                    fsb.append("ivr_dialoutline(\"" + outline + "\")\r\n");
                    fsb.append("ivr_dialoutline(\"" + outline + "\", \"" + ringTimeout + "\")\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;

//                case "transferqueue":
//                    String numsubstr, substrstart, substrend, transfertype;
//                    transfertype = gfno.getParas().get("transfertype");
//                    numsubstr = gfno.getParas().get("numsubstr");
//                    String[] arr = numsubstr.split(",");
//                    substrstart = arr[0];
//                    substrend = arr[1];
//
//                    //fsb.append("freeswitch.consoleLog(\"NOTICE\", \"========transfertype========:\"..transfertype)\r\n");
//                    //fsb.append("freeswitch.consoleLog(\"NOTICE\", \"========numsubstr========:\"..numsubstr)\r\n");
//                    //fsb.append("freeswitch.consoleLog(\"NOTICE\", \"========substrstart========:\"..substrstart)\r\n");
//                    //fsb.append("freeswitch.consoleLog(\"NOTICE\", \"========substrend========: \"..substrend)\r\n");
//                    if ("1".equals(transfertype)) {
//                        fsb.append("areacode = string.sub(cr_destination," + substrstart + "," + substrend + ")\r\n");
//                    } else {
//                        fsb.append("local hcode = string.sub(real_caller," + substrstart + "," + substrend + ")\r\n");
//                        fsb.append("if not zstr(getdsntvs()) then\r\n");
//                        fsb.append("dbh = get_dbh(getdsntvs())\r\n");
//                        fsb.append("if dbh ~= nil then\r\n");
//                        fsb.append("	dbh:query(\"SELECT `areacode` FROM `tx_sys_hcode` WHERE `hcode` =\"..hcode..\"\",\r\n");
//                        fsb.append("		function(row)\r\n");
//                        fsb.append("			areacode = row[\"areacode\"]\r\n");
//                        fsb.append("			freeswitch.consoleLog(\"NOTICE\", \"areacode: \"..areacode)\r\n");
//                        fsb.append("		end)\r\n");
//                        fsb.append("	dbh:release()\r\n");
//                        fsb.append(" end\r\n");
//                        fsb.append("end\r\n");
//                    }
//                    fsb.append("if not zstr(getdsntvsco()) then\r\n");
//                    fsb.append("dbh = get_dbh(getdsntvsco())\r\n");
//                    fsb.append("if dbh ~= nil then\r\n");
//                    fsb.append("	dbh:query(\"SELECT `dpid` FROM `tx_company_area` WHERE `area_code` =\"..areacode..\"\", \r\n");
//                    fsb.append("		function(row)\r\n");
//                    fsb.append("			dpid = row[\"dpid\"]\r\n");
//                    fsb.append("			freeswitch.consoleLog(\"NOTICE\", \"adpid: \"..dpid)\r\n");
//                    fsb.append("		end)\r\n");
//                    fsb.append("	dbh:release()\r\n");
//                    fsb.append(" end\r\n");
//                    fsb.append("end\r\n");
//                    fsb.append("ivr_runcc(company_code..\"_\"..dpid)\r\n");
//                    //fsb.append("ivr_dialoutline(\""+outline+"\")\r\n");
//                    checkunfork(fsb, gfno, func, nodeName);
//                    break;
                case "evarnameintoqueue":
                    //根据配置变量名转指定技能组
                    String varname;
                    varname = gfno.getParas().get("disposevarname");
                    log.info(LOG_TAG + "获取的配置参数为：" + varname);
                    queueTimeoutKey = "cloudcc:skillInfo:" + varname;
                    log.info(LOG_TAG + "queueTimeoutKey：" + queueTimeoutKey);
                    queueTimeoutValue = redissonUtilL.get(queueTimeoutKey);
                    log.info(LOG_TAG + "queueTimeoutValue：" + queueTimeoutValue);
                    queueTimeout = dynamicConfig.getQueueTimeout();
                    if (!UT.zstr(queueTimeoutValue)) {
                        JSONObject jsons = JSONObject.parseObject(queueTimeoutValue);
                        try {
                            queueTimeout = Integer.parseInt(jsons.get("queueTimeout").toString());
                        }catch (Exception e){
                            log.error(LOG_TAG + "获取queueTimeout失败：", e);
                        }
                    }
                    fsb.append("local varname = session:getVariable(\"" + varname + "\")\r\n");
                    log.info(LOG_TAG + "queueTimeout:" + queueTimeout);
                    fsb.append("ivr_runcc(\"" + varname + "\",\"" + queueTimeout + "\",\"" + companyCode + "\",real_caller)\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "continueivr":
                    //接续ivr节点
                    String ivrvalue;
                    ivrvalue = gfno.getParas().get("ivrvalue");
                    log.info(LOG_TAG + "获取的配置参数为：" + ivrvalue);
                    String luaBaseUrl = dynamicConfig.getSbcluaurl();
                    if (luaBaseUrl.contains("%s")){
                        luaBaseUrl = String.format(luaBaseUrl,companyCode);
                    }
                    String luaUrl = luaBaseUrl + ivrvalue;
                    log.info(LOG_TAG + "luaUrl:" + luaUrl);
                    fsb.append("session:execute(\"lua\",\"" + luaUrl + ".lua\")\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "mrcptts":
                    //mrcptts节点
                    String voicelib;
                    String text;
                    voicelib = gfno.getParas().get("voicelib");
                    log.info(LOG_TAG + "获取的音库配置参数为：" + voicelib);
                    text = gfno.getParas().get("text");
                    log.info(LOG_TAG + "获取的内容配置参数为：" + text);
                    fsb.append("session:set_tts_params(\"unimrcp\", \"" + voicelib + "\")\r\n");
                    fsb.append("session:speak(\"" + text + "\")\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "setsessionvalue":
                    //设置通道变量
                    String keyandvalueStr;
                    keyandvalueStr = gfno.getParas().get("keyandvalue");
                    log.info(LOG_TAG + "获取的内容配置参数为：" + keyandvalueStr);
                    List<String> keyAndValueList = Arrays.asList(keyandvalueStr.split(","));
                    for (String keyandvalue : keyAndValueList) {
                        String[] keyAndValue = keyandvalue.split("=");
                        log.info(LOG_TAG + "key为：" + keyAndValue[0] + "    value为：" + keyAndValue[1]);
                        fsb.append("session:setVariable(\"" + keyAndValue[0] + "\",\"" + keyAndValue[1] + "\")\r\n");
                    }
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "jump_node":
                    //接续ivr脚本中的节点
                    String ivrnamevalue;
                    String jumpNodeid;
                    ivrnamevalue = gfno.getParas().get("ivrvalue");
                    jumpNodeid = gfno.getParas().get("jump_nodeid");
                    log.info(LOG_TAG + "获取的配置参数ivrnamevalue：" + ivrnamevalue + " ,jumpNodeid:" + jumpNodeid);
                    if (StringUtil.isNotEmpty(jumpNodeid)){
                        fsb.append("session:setVariable(\"jump_nodeid\",\""+jumpNodeid+"\")\r\n");
                    }else {
                        fsb.append("session:setVariable(\"jump_nodeid\",\"\")\r\n");
                    }
                    fsb.append("session:execute(\"lua\",\"src/"+ivrnamevalue+".lua\")\r\n");
                    checkunfork(fsb,gfno,func,nodeName);
                    break;
                case "queue":
                    String queuevalue = gfno.getParas().get("queuevalue");
                    // 获取参数转int，异常默认1
//                    String queuetimeout = checkStringForint(gfno.getParas().get("queuetimeout"));
                    queueTimeoutKey = "cloudcc:skillInfo:" + queuevalue;
                    log.info(LOG_TAG + "queueTimeoutKey：" + queueTimeoutKey);
                    queueTimeoutValue = redissonUtilL.get(queueTimeoutKey);
                    log.info(LOG_TAG + "queueTimeoutValue：" + queueTimeoutValue);
                    queueTimeout = dynamicConfig.getQueueTimeout();
                    if (!UT.zstr(queueTimeoutValue)) {
                        JSONObject jsons = JSONObject.parseObject(queueTimeoutValue);
                        try {
                            queueTimeout = Integer.parseInt(jsons.get("queueTimeout").toString());
                        }catch (Exception e){
                            log.error(LOG_TAG + "获取queueTimeout失败：", e);
                        }
                    }
                    log.info(LOG_TAG + "queueTimeout:" + queueTimeout);
//                    if (StringUtils.isEmpty(queuetimeout)){
//                        log.info(LOG_TAG + "dynamicConfig.getQueueTimeout():" + dynamicConfig.getQueueTimeout());
//                        queuetimeout = dynamicConfig.getQueueTimeout();
//                    }
                    fsb.append("ivr_runcc(\"" + queuevalue + "\",\"" + queueTimeout + "\",\"" + companyCode + "\",real_caller)\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "variablequeue":
                    String variablequeuevalue = gfno.getParas().get("variablequeuevalue");
                    //String variablequeuevalue = session:getVariable("variablequeuevalue");
                    // 获取参数转int，异常默认1
//                    String variablequeuetimeout = checkStringForint(gfno.getParas().get("variablequeuetimeout"));
                    queueTimeoutKey = "cloudcc:skillInfo:" + variablequeuevalue;
                    log.info(LOG_TAG + "queueTimeoutKey：" + queueTimeoutKey);
                    queueTimeoutValue = redissonUtilL.get(queueTimeoutKey);
                    log.info(LOG_TAG + "queueTimeoutValue：" + queueTimeoutValue);
                    queueTimeout = dynamicConfig.getQueueTimeout();
                    if (!UT.zstr(queueTimeoutValue)) {
                        JSONObject jsons = JSONObject.parseObject(queueTimeoutValue);
                        try {
                            queueTimeout = Integer.parseInt(jsons.get("queueTimeout").toString());
                        }catch (Exception e){
                            log.error(LOG_TAG + "获取queueTimeout失败：", e);
                        }
                    }
                    log.info(LOG_TAG + "queueTimeout:" + queueTimeout);
//                    if (StringUtils.isEmpty(variablequeuetimeout)){
//                        log.info(LOG_TAG + "dynamicConfig.getQueueTimeout():" + dynamicConfig.getQueueTimeout());
//                        variablequeuetimeout = dynamicConfig.getQueueTimeout();
//                    }
                    //fsb.append("ivr_runcc(\""+variablequeuevalue+"\")\r\n");
                    //fsb.append("freeswitch.consoleLog(\"NOTICE\", \"variablequeuevalue:"+variablequeuevalue+"\")\r\n");
                    fsb.append("ivr_runcc(session:getVariable(\"" + variablequeuevalue + "\",\"" + queueTimeout + "\",\"" + companyCode + "\",real_caller)\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "variabledialnum":
                    String variablenum = gfno.getParas().get("variablenum");
                    //fsb.append("freeswitch.consoleLog(\"NOTICE\", \"variablenum:"+variablenum+"\")\r\n");
                    ringTimeout = dynamicConfig.getQueueTimeout();
                    log.info(LOG_TAG + "ringTimeout:" + ringTimeout);
//                    fsb.append("ivr_dialoutline(session:getVariable(\"" + variablenum + "\"))\r\n");
                    fsb.append("ivr_dialoutline(session:getVariable(\"" + variablenum + "\"), \"" + ringTimeout + "\")\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "variabledialext":
                    String variableext = gfno.getParas().get("variableext");
                    //fsb.append("ivr_dialext(\""+variableext+"\")\r\n");
                    //fsb.append("freeswitch.consoleLog(\"NOTICE\", \"variableext:"+variableext+"\")\r\n");
                    ringTimeoutKey = "cloudcc:companyInfo:" + companyCode;
                    log.info(LOG_TAG + "ringTimeoutKey：" + ringTimeoutKey);
                    ringTimeoutValue = redissonUtilL.get(ringTimeoutKey);
                    ringTimeout = dynamicConfig.getQueueTimeout();
                    if (!UT.zstr(ringTimeoutValue)) {
                        JSONObject jsons = JSONObject.parseObject(ringTimeoutValue);
                        try {
                            ringTimeout = Integer.parseInt(jsons.get("ringTimeout").toString());
                        }catch (Exception e){
                            log.error(LOG_TAG + "获取ringTimeout失败：", e);
                        }
                    }
                    log.info(LOG_TAG + "ringTimeoutValue：" + ringTimeoutValue);
                    log.info(LOG_TAG + "ringTimeout:" + ringTimeout);
//                    fsb.append("ivr_dialext(company_code..\"_\"..session:getVariable(\"" + variableext + "\"))\r\n");
                    fsb.append("ivr_dialext(company_code..\"_\"..session:getVariable(\"" + variableext + "\"), \"" + ringTimeout + "\")\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "cdrsavedata":
                    String cdrsavedata_inputvarname = gfno.getParas().get("cdrsavedata_inputvarname");
                    String cdrsavedata_cdrvarname = gfno.getParas().get("cdrsavedata_cdrvarname");
                    //fsb.append("freeswitch.consoleLog(\"NOTICE\", \"cdrsavedata_inputvarname:"+cdrsavedata_inputvarname+"\")\r\n");
                    //fsb.append("freeswitch.consoleLog(\"NOTICE\", \"cdrsavedata_cdrvarname:"+cdrsavedata_cdrvarname+"\")\r\n");
                    if (!UT.zstr(cdrsavedata_cdrvarname)) {
                        fsb.append("session:setVariable(\"" + cdrsavedata_cdrvarname + "\",session:getVariable(\"" + cdrsavedata_inputvarname + "\"))\r\n");
                    }
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "readex":
                case "readex_curl":
                    String readex_playfile, readex_max_digits, readex_playtimes, readex_timeout, userkey_cdrvarname_ex, readex_usrinputvarname, readex_dtmfprefix;
                    if ("readex".equals(gfno.getApp())) {
                        readex_playfile = gfno.getParas().get("readex_playfile");
                        readex_max_digits = checkStringForint(gfno.getParas().get("readex_max_digits"));
                        readex_playtimes = checkStringForint(gfno.getParas().get("readex_playtimes"));
                        readex_timeout = checkStringForint(gfno.getParas().get("readex_timeout"));
                        userkey_cdrvarname_ex = gfno.getParas().get("userkey_cdrvarname_ex");
                        readex_usrinputvarname = gfno.getParas().get("readex_usrinputvarname");
                        readex_dtmfprefix = gfno.getParas().get("readx_dtmfprefix");

                        fsb.append("local digit = ivr_playAndGetDigits_ex(1," + readex_max_digits + "," + readex_playtimes + "," + readex_timeout + ",\"" + readex_playfile + "\",nil,\"" + gfno.getNodeid() + "\",\"" + nodeName + "\",\"" + readex_dtmfprefix + "\")\r\n");
                    } else {
                        readex_playfile = gfno.getParas().get("readexvoiceurl");
                        readex_max_digits = checkStringForint(gfno.getParas().get("readex_curl_max_digits"));
                        readex_playtimes = checkStringForint(gfno.getParas().get("readex_curl_playtimes"));
                        readex_timeout = checkStringForint(gfno.getParas().get("readex_curl_timeout"));
                        userkey_cdrvarname_ex = gfno.getParas().get("readex_curl_userkey_cdrvarname");
                        readex_usrinputvarname = gfno.getParas().get("readex_curl_inputvarname");
                        readex_dtmfprefix = gfno.getParas().get("readx_curl_dtmfprefix");
                        fsb.append("local digit = \"\"\r\n");
                        fsb.append("local getPlayFile = session:getVariable(\"" + readex_playfile + "\")\r\n");
                        fsb.append("if getPlayFile ~= nil then\r\n");
                        fsb.append("	local pos,__ = string.find(getPlayFile,\"http\")\r\n");
                        fsb.append("    if pos and pos < 4 then\r\n");
                        fsb.append("        session:execute(\"playback\",\"space.wav\")\r\n");
                        fsb.append("		digit = ivr_playAndGetDigits_ex(1," + readex_max_digits + "," + readex_playtimes + "," + readex_timeout + ",session:getVariable(\"" + readex_playfile + "\"),nil,\"" + gfno.getNodeid() + "\",\"" + nodeName + "\",\"" + readex_dtmfprefix + "\")\r\n");
                        fsb.append("    else\r\n");
                        fsb.append("        local tts_engine, tts_voice = \"\", \"\"\r\n");
                        fsb.append("        __,pos = string.find(getPlayFile, \":\")\r\n");
                        fsb.append("        if pos then\r\n");
                        fsb.append("            tts_voice = string.sub(getPlayFile, 1, pos - 1)\r\n");
                        fsb.append("            getPlayFile = string.sub(getPlayFile, pos+1)\r\n");
                        fsb.append("        end\r\n");
                        fsb.append("        __,pos = string.find(getPlayFile, \":\")\r\n");
                        fsb.append("        if pos then\r\n");
                        fsb.append("            tts_engine = string.sub(getPlayFile, 1, pos - 1)\r\n");
                        fsb.append("            getPlayFile = string.sub(getPlayFile, pos+1)\r\n");
                        fsb.append("            session:setVariable(\"tts_engine\", tts_engine)\r\n");
                        fsb.append("            session:setVariable(\"tts_voice\", tts_voice)\r\n");
                        fsb.append("			digit = ivr_playAndGetDigits_ex(1," + readex_max_digits + "," + readex_playtimes + "," + readex_timeout + ",getPlayFile,nil,\"" + gfno.getNodeid() + "\",\"" + nodeName + "\",\"" + readex_dtmfprefix + "\")\r\n");
                        fsb.append("        end\r\n");
                        fsb.append("    end\r\n");
                        fsb.append("end\r\n");
                    }

                    if (!UT.zstr(userkey_cdrvarname_ex)) {
                        fsb.append("session:setVariable(\"" + userkey_cdrvarname_ex + "\",digit)\r\n");
                    }
                    if (!UT.zstr(readex_usrinputvarname)) {
                        fsb.append("session:setVariable(\"" + readex_usrinputvarname + "\",digit)\r\n");
                    }
//				if(!UT.zstr(readex_dtmfprefix)){
//					fsb.append("if dtmfdatas == \"\" then\r\n");
//					fsb.append("dtmfdatas = \"(\"..os.date(\"%H:%M:%S\")..\"~"+readex_dtmfprefix+"~\"..digit..\")\"\r\n");
//					fsb.append("else\r\n");
//					fsb.append("dtmfdatas = dtmfdatas..\"(\"..os.date(\"%H:%M:%S\")..\"~"+readex_dtmfprefix+"~\"..digit..\")\"\r\n");
//					fsb.append("end\r\n");
//					//fsb.append("session:setVariable(\""+readx_dtmfprefix+"\",digit)\r\n");
//					fsb.append("session:setVariable(\"userkey_f10\",dtmfdatas)\r\n");
//				}
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "playback":
                    String playfile = gfno.getParas().get("playfile");
                    fsb.append("session:execute(\"playback\",\"space.wav\")\r\n");
                    fsb.append("session:execute(\"playback\",\"" + playfile + "\")\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "playvoicequeue":
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "record_sms":
                    String endKey = gfno.getParas().get("end_key");
                    if (StringUtil.isEmpty(endKey)){
                        endKey = "#";
                    }
                    log.info(LOG_TAG + "endKey：" + endKey);
                    String beginVoice = gfno.getParas().get("begin_voice");
//                    if (beginVoice.contains("/")){
//                        beginVoice = beginVoice.substring(beginVoice.lastIndexOf("/") + 1);
//                    }
                    if (StringUtil.isEmpty(beginVoice)){
                        beginVoice = dynamicConfig.getDefaultleavemsgstartvidiau();
                        if (beginVoice.contains("%s")){
                            beginVoice  = String.format(beginVoice, companyCode);
                        }
                    }
                    log.info(LOG_TAG + "beginVoice：" + beginVoice);
                    leaveMessageTimeout = dynamicConfig.getLeaveMessageTimeout();
                    log.info(LOG_TAG + "leaveMessageTimeout：" + leaveMessageTimeout);
                    fsb.append("ivr_vc_recorder(\"" + leaveMessageTimeout  + "\",\"" + endKey  + "\",\"" + beginVoice + "\")\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                case "playback_curl":
                    String r_playfile = gfno.getParas().get("voiceurl");
                    fsb.append("local getPlayFile = session:getVariable(\"" + r_playfile + "\")\r\n");
                    fsb.append("if getPlayFile ~= nil then\r\n");
                    fsb.append("	local pos,__ = string.find(getPlayFile,\"http\")\r\n");
                    fsb.append("    if pos and pos < 4 then\r\n");
                    fsb.append("        session:execute(\"playback\",\"space.wav\")\r\n");
                    fsb.append("        session:execute(\"playback\",session:getVariable(\"" + r_playfile + "\"))\r\n");
                    fsb.append("    else\r\n");
                    fsb.append("        local tts_engine, tts_voice = \"\", \"\"\r\n");
                    fsb.append("        __,pos = string.find(getPlayFile, \":\")\r\n");
                    fsb.append("        if pos then\r\n");
                    fsb.append("            tts_voice = string.sub(getPlayFile, 1, pos - 1)\r\n");
                    fsb.append("            getPlayFile = string.sub(getPlayFile, pos+1)\r\n");
                    fsb.append("        end\r\n");
                    fsb.append("        __,pos = string.find(getPlayFile, \":\")\r\n");
                    fsb.append("        if pos then\r\n");
                    fsb.append("            tts_engine = string.sub(getPlayFile, 1, pos - 1)\r\n");
                    fsb.append("            getPlayFile = string.sub(getPlayFile, pos+1)\r\n");
                    fsb.append("            session:set_tts_params(tts_engine, tts_voice)\r\n");
                    fsb.append("            __,pos = string.find(getPlayFile, \":\")\r\n");
                    fsb.append("        	if pos then\r\n");
                    fsb.append("            	getPlayFile = string.sub(getPlayFile, pos+1)\r\n");
                    fsb.append("        	end\r\n");
                    fsb.append("            session:speak(getPlayFile)\r\n");
                    fsb.append("        end\r\n");
                    fsb.append("    end\r\n");
                    fsb.append("end\r\n");
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
                default:
                    checkunfork(fsb, gfno, func, nodeName);
                    break;
            }
        }
        fsb.append("end\r\n");
        allfunc.add(fsb);
    }

    public String createivr(String ivrid, String ivrname, String id, String LOG_TAG, String flowdata, String companyCode, FreeswitchApiFeignClient freeswitchApiFeignClient, DynamicConfig dynamicConfig, RedissonUtil redissonUtilL) {
        log.info(LOG_TAG + "ivrid:" + ivrid + "id:" + ivrid);
        if (!checkstart()) {
            errmsg += ("开始节点异常，请确保start节点有且只有一个！");
            return errmsg;
        }
        sb.append("package.path = freeswitch.getGlobalVariable(\"script_dir\")..\"/?.lua\"\r\n");
        sb.append("require \"base\"\r\n");
        sb.append("cjson = require \"cjson\"\r\n");

        //2019190411
        sb.append("require \"curl\"\r\n");
        //sb.append("read_digits_index = 0\r\n");
        sb.append("ivrid = \"" + id + "\"\r\n");
        sb.append("server_name = freeswitch.getGlobalVariable(\"server_name\")\r\n");
        sb.append("local real_caller = session:getVariable(\"caller_id_number\")\r\n");
        sb.append("local cr_destination = session:getVariable(\"destination_number\")\r\n");
        sb.append("local company_code = session:setVariable(\"company_code\",\"" + companyCode + "\")\r\n");
        sb.append("local company_code = session:getVariable(\"company_code\")\r\n");
        sb.append("local uuid = session:getVariable(\"uuid\")\r\n");
        sb.append("local dpid = \"\"\r\n");

        sb.append("local in_ivr_time = session:getVariable(\"in_ivr_time\")\r\n");
        sb.append("if in_ivr_time == nil or in_ivr_time ==\"\" then\r\n");
        sb.append("	in_ivr_time = os.date(\"%Y%m%d%H%M%S\",os.time())\r\n");
        sb.append("	session:setVariable(\"in_ivr_time\",in_ivr_time)\r\n");
        sb.append("end\r\n");

        sb.append("local ivr_tracks = session:getVariable(\"ivr_tracks\")\r\n");
        sb.append("if ivr_tracks == nil or ivr_tracks ==\"\" then\r\n");
        sb.append("	ivr_tracks =  \"" + ivrid + ".lua(" + ivrname + ")\"\r\n");
        sb.append("else\r\n");
        sb.append("	ivr_tracks = ivr_tracks..\"  ——" + ivrid + ".lua(" + ivrname + ")\"\r\n");
        sb.append("end\r\n");
        sb.append("session:setVariable(\"ivr_tracks\",ivr_tracks)\r\n");
//		sb.append("local dtmfdata = \"\"\r\n");
//		sb.append("local dtmfdatas = \"\"\r\n");
        //sb.append("local areacode = \"\"\r\n");
        //sb.append("local hcode = \"\"\r\n");
        //sb.append("local dpid = \"\"\r\n");
        log.info(LOG_TAG + "start.get(0):" + start.get(0));
        parseroutes(start.get(0), null);
        try {
            log.info(LOG_TAG + "routes.values():" + JSON.toJSONString(routes.values()));
        } catch (Exception e) {
            log.info(LOG_TAG + "routes.values()转json失败：", e);
        }
        for (luafunc value : routes.values()) {
            if (!"".equals(errmsg)) {
                log.info(LOG_TAG + "errmsg:" + errmsg);
                return errmsg;
            }
            try {
                log.info(LOG_TAG + "value:" + JSON.toJSONString(value));
            } catch (Exception e) {
                log.info(LOG_TAG + "value转json失败：", e);
            }
            createfunclua(value, LOG_TAG, flowdata, companyCode, dynamicConfig, redissonUtilL);
        }
        for (String var : allvar) {
            sb.append(var);
        }
        for (StringBuffer sbf : allfunc) {
            log.info(LOG_TAG + "sbf:" + sbf);
            sb.append(sbf);
        }
        sb.append(endfunc);
        sb.append(start.get(0).getNodeid() + "()\r\n");
        //System.out.println(sb);
//		try {
//			String outputPath = "/usr/local/freeswitch/scripts/src/" + ivrid + ".lua";
//			FileWriter fw = new FileWriter(outputPath);
//			PrintWriter pw = new PrintWriter(fw);
//			pw.println(sb);
//			pw.flush();
//			pw.close();
//			fw.close();
//		} catch (IOException e) {
//			log.error(LOG_TAG + "IO异常:", e);
//		}finally{
//		};
        try {
            LuaWriteDTO luaWriteDTO = new LuaWriteDTO();
            luaWriteDTO.setCompanyCode(companyCode);
            InputStream byteArrayInputStream = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
            byte[] bytes = StreamTransUtil.input2byte(byteArrayInputStream);
            String s = StreamTransUtil.toHexString(bytes);
            luaWriteDTO.setContent(s);
            luaWriteDTO.setFileName(ivrid);
            log.info(LOG_TAG + "lua脚本同步底层接口：" + luaWriteDTO);
            LuaWriteVO luaWriteVO = freeswitchApiFeignClient.luaWrite(luaWriteDTO);
            log.info(LOG_TAG + "lua脚本同步底层接口返回结果：" + luaWriteVO);
            errmsg += luaWriteVO.toString() + "，";

        } catch (Exception e) {
            log.error(LOG_TAG + "上传底层接口异常", e);
            errmsg += ("上传底层接口异常！");
        }
        return errmsg;
    }
}
