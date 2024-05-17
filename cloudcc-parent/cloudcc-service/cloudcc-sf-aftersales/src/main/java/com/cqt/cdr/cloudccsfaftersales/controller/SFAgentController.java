package com.cqt.cdr.cloudccsfaftersales.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cqt.cdr.cloudccsfaftersales.entity.agent.*;
import com.cqt.cdr.cloudccsfaftersales.entity.vo.ActionForward;
import com.cqt.cdr.cloudccsfaftersales.entity.vo.ActionQueryForward;
import com.cqt.cdr.cloudccsfaftersales.util.SFUtils;
import com.cqt.feign.client.AfterSaleClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cqt.cdr.cloudccsfaftersales.util.SFUtils.*;

@RestController
@RequestMapping("/SF/agent-info")
@Slf4j
public class SFAgentController {
    @Resource
    AfterSaleClient afterSaleClient;

    /**
     * 插入坐席
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("add")
    public ActionForward agentInsert(HttpServletRequest request) throws Exception {
        ActionForward auth = auth(request);
        if (auth != null) {
            return auth;
        }
        String receiveJsonStr = SFUtils.getJsonByRequest(request);
        if ("".equals(receiveJsonStr) || receiveJsonStr == null) {
            receiveJsonStr = "{}";
        }
        JSONObject jsonObjexpress = null;
        try {
            jsonObjexpress = JSONObject.parseObject(receiveJsonStr);
        } catch (Exception e) {
            log.error("操作失败，请检查参数是否已填写！:", e);
            return ActionForward.getActionForward("操作失败，请检查参数是否已填写！");
        }
        String chineseName = "";
        String employeeId = "";
        String department = "";
        String seat_area = "";
        String seat_pwd = "";
        String system_id = null;
        String corpid = null;
        String displaynum = null;
        int checkseatgroup;
        try {
            seat_pwd = jsonObjexpress.getString("seat_pwd").replace(" ", "");
            seat_area = jsonObjexpress.getString("seat_area").replace(" ", "");
            chineseName = jsonObjexpress.getString("seat_name").replace(" ", "");
            employeeId = jsonObjexpress.getString("seatId").replace(" ", "");
            department = jsonObjexpress.getString("seat_group").replace(" ", "");
            system_id = jsonObjexpress.getString("system_id").replace(" ", "");
        } catch (Exception e) {
            log.error("字段信息输入错误！:", e);
            return ActionForward.getActionForward("字段信息输入错误！");
        }

        AgentInfoAddDTO agentInfoAddDTO = new AgentInfoAddDTO();
        // 坐席信息
        agentInfoAddDTO.setAgentId(employeeId);
        agentInfoAddDTO.setAgentName(chineseName);
        agentInfoAddDTO.setPassword(seat_pwd);
        agentInfoAddDTO.setState(1);
        agentInfoAddDTO.setDisplayNumber(displaynum);
        ArrayList<String> roleIds = new ArrayList<>();
        // 默认话务员
        roleIds.add("1686272543079890946");
        agentInfoAddDTO.setRoleIdList(roleIds);
        afterSaleClient.addAgent(agentInfoAddDTO,"090008");
        log.info("agentInsert创建坐席end");
        return ActionForward.getActionForward("agentInsert创建坐席end");
    }
//    @PostMapping("add")
//    public ActionForward agentInsert(@Validated @RequestBody AgentInfoAddDTO agentInfoAddDTO, HttpServletRequest request, HttpServletResponse response) throws Exception {
//        ActionForward auth = auth(request);
//        if (auth != null) {
//            return auth;
//        }
//        String receiveJsonStr = SFUtils.getJsonByRequest(request);
//        if ("".equals(receiveJsonStr) || receiveJsonStr == null) {
//            receiveJsonStr = "{}";
//        }
//        JSONObject jsonObjexpress = null;
//        try {
//            jsonObjexpress = JSONObject.parseObject(receiveJsonStr);
//        } catch (Exception e) {
//            log.error("操作失败，请检查参数是否已填写！:", e);
//            return ActionForward.getActionForward("操作失败，请检查参数是否已填写！");
//        }
//        String chineseName = "";
//        String employeeId = "";
//        String department = "";
//        String seat_area = "";
//        String seat_pwd = "";
//        String system_id = null;
//        String corpid = null;
//        String displaynum = null;
//        int checkseatgroup;
//        try {
//            seat_pwd = jsonObjexpress.getString("seat_pwd").replace(" ", "");
//            seat_area = jsonObjexpress.getString("seat_area").replace(" ", "");
//            chineseName = jsonObjexpress.getString("seat_name").replace(" ", "");
//            employeeId = jsonObjexpress.getString("seatId").replace(" ", "");
//            department = jsonObjexpress.getString("seat_group").replace(" ", "");
//            system_id = jsonObjexpress.getString("system_id").replace(" ", "");
//        } catch (Exception e) {
//            log.error("字段信息输入错误！:", e);
//            return ActionForward.getActionForward("字段信息输入错误！");
//        }
//        if (department == null || department.equals("")) {
//            department = "0";
//        }
//        try {
//            checkseatgroup = Integer.parseInt(department);
//        } catch (Exception e) {
//            log.error("seat_group输入错误必须为数字！:", e);
//            return ActionForward.getActionForward("seat_group输入错误必须为数字！:");
//        }
//        if (checkseatgroup < 0 || checkseatgroup > 65535) {
//            log.error("seat_group必须为0-65535的数字");
//            return ActionForward.getActionForward("seat_group输入错误必须为数字！");
//        }
//        if (seat_pwd == null || seat_pwd.equals("") || seat_pwd.length() > 64) {
//            log.error("seat_pwd字段不允许为空且不得超过64位！");
//            return ActionForward.getActionForward("seat_pwd字段不允许为空且不得超过64位！");
//        }
//        if (chineseName == null || chineseName.equals("") || chineseName.length() > 64) {
//            log.error("seat_name字段不允许为空且不得超过64位！");
//            return ActionForward.getActionForward("seat_name字段不允许为空且不得超过64位！");
//        }
//        if (employeeId == null || employeeId.equals("") || employeeId.length() > 9) {
//            log.error("seatId字段不允许为空且不得超过9位！");
//            return ActionForward.getActionForward("seatId字段不允许为空且不得超过9位！");
//        }
//        if (cheackseaid(employeeId)) {
//            log.error("seatId字段不允许出现中文！");
//            return ActionForward.getActionForward("seatId字段不允许出现中文！");
//        }
//        if (seat_area == null || seat_area.equals("") || seat_area.length() > 64) {
//            log.error("操作失败,不允许seat_area为空且不超过64位");
//            return ActionForward.getActionForward("操作失败,不允许seat_area为空且不超过64位");
//        }
//
//
//        // 坐席信息
//        agentInfoAddDTO.setAgentId(employeeId);
//        agentInfoAddDTO.setAgentName(chineseName);
//        agentInfoAddDTO.setPassword(seat_pwd);
//        agentInfoAddDTO.setState(1);
//        agentInfoAddDTO.setDisplayNumber(displaynum);
//        ArrayList<String> roleIds = new ArrayList<>();
//        // 默认话务员
//        roleIds.add("1686272543079890946");
//        agentInfoAddDTO.setRoleIdList(roleIds);
//        afterSaleClient.addAgent(agentInfoAddDTO);
//        log.info("agentInsert创建坐席end");
//        return ActionForward.getActionForward("agentInsert创建坐席end");
//    }

    /**
     * 更新坐席信息
     *
     * @return
     * @throws IOException
     */
    @PostMapping("update")
    public ActionForward agentUpdate(HttpServletRequest request) throws IOException {
        ActionForward auth = auth(request);
        if (auth != null) {
            return auth;
        }
        String receiveJsonStr = SFUtils.getJsonByRequest(request);
        if ("".equals(receiveJsonStr) || receiveJsonStr == null) {
            receiveJsonStr = "{}";
        }
        JSONObject jsonObjexpress = null;
        try {
            jsonObjexpress = JSONObject.parseObject(receiveJsonStr);
        } catch (Exception e) {
            log.error("操作失败，请检查参数是否已填写！:", e);
            return ActionForward.getActionForward("操作失败，请检查参数是否已填写！");
        }
        String chineseName = "";
        String employeeId = "";
        String department = "";
        String seat_area = "";
        String seat_pwd = "";
        String system_id = null;
        String corpid = null;
        String displaynum = null;
        int checkseatgroup;
        try {
            seat_pwd = jsonObjexpress.getString("seat_pwd").replace(" ", "");
            seat_area = jsonObjexpress.getString("seat_area").replace(" ", "");
            chineseName = jsonObjexpress.getString("seat_name").replace(" ", "");
            employeeId = jsonObjexpress.getString("seatId").replace(" ", "");
            department = jsonObjexpress.getString("seat_group").replace(" ", "");
            system_id = jsonObjexpress.getString("system_id").replace(" ", "");
        } catch (Exception e) {
            log.error("字段信息输入错误！:", e);
            return ActionForward.getActionForward("字段信息输入错误！");
        }
        if (department == null || department.equals("")) {
            department = "0";
        }
        // 坐席信息
        AgentInfoEditDTO agentInfoEditDTO = new AgentInfoEditDTO();
        agentInfoEditDTO.setAgentId(employeeId);
        agentInfoEditDTO.setAgentName(chineseName);
        agentInfoEditDTO.setPassword(seat_pwd);
        agentInfoEditDTO.setState(1);
        agentInfoEditDTO.setDisplayNumber(displaynum);
        ArrayList<String> roleIds = new ArrayList<>();
        // 默认话务员
        roleIds.add("1686272543079890946");
        agentInfoEditDTO.setRoleIdList(roleIds);
        afterSaleClient.edit(agentInfoEditDTO,"090008");
        log.info("agentInsert创建坐席end");
        return ActionForward.getActionForward("agentInsert创建坐席end");
    }

//    @PostMapping("update")
//    public ActionForward agentUpdate(HttpServletRequest request) throws IOException {
//        ActionForward auth = auth(request);
//        if (auth != null) {
//            return auth;
//        }
//        String receiveJsonStr = SFUtils.getJsonByRequest(request);
//        if ("".equals(receiveJsonStr) || receiveJsonStr == null) {
//            receiveJsonStr = "{}";
//        }
//        JSONObject jsonObjexpress = null;
//        try {
//            jsonObjexpress = JSONObject.parseObject(receiveJsonStr);
//        } catch (Exception e) {
//            log.error("操作失败，请检查参数是否已填写！:", e);
//            return ActionForward.getActionForward("操作失败，请检查参数是否已填写！");
//        }
//        String chineseName = "";
//        String employeeId = "";
//        String department = "";
//        String seat_area = "";
//        String seat_pwd = "";
//        String system_id = null;
//        String corpid = null;
//        String displaynum = null;
//        int checkseatgroup;
//        try {
//            seat_pwd = jsonObjexpress.getString("seat_pwd").replace(" ", "");
//            seat_area = jsonObjexpress.getString("seat_area").replace(" ", "");
//            chineseName = jsonObjexpress.getString("seat_name").replace(" ", "");
//            employeeId = jsonObjexpress.getString("seatId").replace(" ", "");
//            department = jsonObjexpress.getString("seat_group").replace(" ", "");
//            system_id = jsonObjexpress.getString("system_id").replace(" ", "");
//        } catch (Exception e) {
//            log.error("字段信息输入错误！:", e);
//            return ActionForward.getActionForward("字段信息输入错误！");
//        }
//        if (department == null || department.equals("")) {
//            department = "0";
//        }
//        try {
//            checkseatgroup = Integer.parseInt(department);
//        } catch (Exception e) {
//            log.error("seat_group输入错误必须为数字！:", e);
//            return ActionForward.getActionForward("seat_group输入错误必须为数字！:");
//        }
//        if (checkseatgroup < 0 || checkseatgroup > 65535) {
//            log.error("seat_group必须为0-65535的数字");
//            return ActionForward.getActionForward("seat_group输入错误必须为数字！");
//        }
//        if (seat_pwd == null || seat_pwd.equals("") || seat_pwd.length() > 64) {
//            log.error("seat_pwd字段不允许为空且不得超过64位！");
//            return ActionForward.getActionForward("seat_pwd字段不允许为空且不得超过64位！");
//        }
//        if (chineseName == null || chineseName.equals("") || chineseName.length() > 64) {
//            log.error("seat_name字段不允许为空且不得超过64位！");
//            return ActionForward.getActionForward("seat_name字段不允许为空且不得超过64位！");
//        }
//        if (employeeId == null || employeeId.equals("") || employeeId.length() > 9) {
//            log.error("seatId字段不允许为空且不得超过9位！");
//            return ActionForward.getActionForward("seatId字段不允许为空且不得超过9位！");
//        }
//        if (cheackseaid(employeeId)) {
//            log.error("seatId字段不允许出现中文！");
//            return ActionForward.getActionForward("seatId字段不允许出现中文！");
//        }
//        if (seat_area == null || seat_area.equals("") || seat_area.length() > 64) {
//            log.error("操作失败,不允许seat_area为空且不超过64位");
//            return ActionForward.getActionForward("操作失败,不允许seat_area为空且不超过64位");
//        }
//
//        // 坐席信息
//        AgentInfoEditDTO agentInfoEditDTO = new AgentInfoEditDTO();
//        agentInfoEditDTO.setAgentId(employeeId);
//        agentInfoEditDTO.setAgentName(chineseName);
//        agentInfoEditDTO.setPassword(seat_pwd);
//        agentInfoEditDTO.setState(1);
//        agentInfoEditDTO.setDisplayNumber(displaynum);
//        ArrayList<String> roleIds = new ArrayList<>();
//        // 默认话务员
//        roleIds.add("1686272543079890946");
//        agentInfoEditDTO.setRoleIdList(roleIds);
//        afterSaleClient.edit(agentInfoEditDTO);
//        log.info("agentInsert创建坐席end");
//        return ActionForward.getActionForward("agentInsert创建坐席end");
//    }

    /**
     * 删除坐席
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @PostMapping("delete")
    public ActionForward agentDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ActionForward auth = auth(request);
        if (auth != null) {
            return auth;
        }
        String receiveJsonStr = getJsonByRequest(request);
        if ("".equals(receiveJsonStr) || receiveJsonStr == null) {
            receiveJsonStr = "{}";
        }
        JSONObject jsonObjexpress = null;
        try {
            jsonObjexpress = JSONObject.parseObject(receiveJsonStr);
        } catch (Exception e) {
            log.error("操作失败，请检查参数是否已填写！:", e);
            return ActionForward.getActionForward("操作失败，请检查参数是否已填写！");
        }
        String seatId = null;
        try {
            seatId = jsonObjexpress.getString("seatId").replace(" ", "");
        } catch (Exception e) {
            log.error("字段信息输入错误！:", e);
            return ActionForward.getActionForward("字段信息输入错误！");
        }
        log.info("agentDelete删除坐席end");
        afterSaleClient.batchDelete(seatId,"090008");
        return ActionForward.getActionForward("agentDelete删除坐席end");
    }


    /**
     * 搜素
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping("search")
    public ActionForward agentSearch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionForward auth = auth(request);
        if (auth != null) {
            return auth;
        }
        String receiveJsonStr = getJsonByRequest(request);
        if ("".equals(receiveJsonStr) || receiveJsonStr == null) {
            receiveJsonStr = "{}";
        }
        JSONObject jsonObjexpress = null;
        try {
            jsonObjexpress = JSONObject.parseObject(receiveJsonStr);
        } catch (Exception e) {
            log.error("操作失败，请检查参数是否已填写！:", e);
            return ActionForward.getActionForward("操作失败，请检查参数是否已填写！");
        }
        String seatId = null;
        try {
            seatId = jsonObjexpress.getString("seatId").replace(" ", "");
        } catch (Exception e) {
            log.error("字段信息输入错误！:", e);
            return ActionForward.getActionForward("字段信息输入错误！");
        }
        AgentInfoQueryDTO agentInfoQueryDTO = new AgentInfoQueryDTO();
        agentInfoQueryDTO.setStartAgentId(Long.parseLong(seatId));
        List<AgentInfoQueryVO> records = afterSaleClient.pageQuery(agentInfoQueryDTO,"090008").getResult().getRecords();
        AgentInfoQueryVO agentInfoQueryVO = records.get(0);
//        Map<String, String> ret =  new HashMap<String, String>();
        ActionQueryForward actionQueryForward = new ActionQueryForward();
        actionQueryForward.setResultno("0");
        actionQueryForward.setResultmsg("操作成功");
        if (agentInfoQueryVO != null) {
            actionQueryForward.setSeat_group(agentInfoQueryVO.getDepartIdList().toString());
            actionQueryForward.setSeat_id(seatId);
            actionQueryForward.setSeat_name(agentInfoQueryVO.getAgentName());
//            ret.put("resultno", "-1");
//            ret.put("resultmsg", "操作成功");
//            ret.put("seat_name", agentInfoQueryVO.getAgentName());
//            ret.put("seat_id",seatId);
////            ret.put("seat_area",seat_area);
////            ret.put("callback_enable", String.valueOf(webEmployeeInfo.getSkillLevel()));
//            ret.put("seat_group", agentInfoQueryVO.getDepartIdList().toString());
////            ret.put("callback_time",employeeGrp.getOrderIndex()+"");
//            ret.put("resultno", "0");
        }
        return actionQueryForward;
    }


    private static ActionForward auth(HttpServletRequest request) {
        ActionForward actionForward = null;
        String aup = "";
        try {
            aup = request.getParameter("aup");
        } catch (Exception e) {
            log.error(aup + "失败！鉴权信息验证失败:" + e + ",收到的鉴权信息为:" + aup);
            return actionForward = ActionForward.getActionForward("鉴权信息验证失败");
        }
        log.info(aup + "agentDelete删除坐席start");
        // 校验请求中的expressKey
        if ("".equals(aup) || aup == null) {
            log.error(aup + "失败！鉴权信息不能为空！");
            return actionForward = ActionForward.getActionForward("失败！鉴权信息不能为空！");
        }
        aup = aup.replaceAll(" ", "");
        log.info(aup + "这里是坐席删除，获取到鉴权信息为：" + aup);
        boolean expressKeyFlag = checkexpress(aup);
        if (!expressKeyFlag) {
            log.error(aup + "失败！鉴权信息验证失败:" + ",收到的鉴权信息为:" + aup);
            return actionForward = ActionForward.getActionForward("鉴权信息验证失败！");
        }
        return actionForward;
    }


//
//    public ActionForward groupInsert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
//
//    }
//
//    public ActionForward groupUpdate(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
//    }

}
