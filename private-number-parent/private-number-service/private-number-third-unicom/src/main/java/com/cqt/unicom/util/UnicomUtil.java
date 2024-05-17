package com.cqt.unicom.util;

import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.enums.CdrTypeCodeEnum;
import com.cqt.common.util.ThirdUtils;
import com.cqt.model.push.entity.Callstat;
import com.cqt.model.unicom.entity.*;
import com.cqt.unicom.config.cache.UnicomLocalCacheService;
import com.cqt.unicom.mapper.HCodeDao;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author zhengsuhao
 * @date 2022/12/6
 */
@Slf4j
public class UnicomUtil {


    public static Map<String, String> hcodeCache = new HashMap<>();




    @ApiOperation("集团报文时间戳转为客户报文格式")
    public static String timestampConversion(String timestamp) {
        return timestamp.replaceAll("[^0-9.]", "");
    }

    @ApiOperation("计算通话时长")
    public static int talkTime(String starttime, String endTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long timeLag;
        try {
            Date beginDate = simpleDateFormat.parse(starttime);
            long begin = beginDate.getTime();
            Date endDate = simpleDateFormat.parse(endTime);
            long end = endDate.getTime();
            timeLag = (end - begin) / 1000;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return Math.toIntExact(timeLag);
    }

    @ApiOperation("截取录音路径")
    public static String extractMediaUrl(String url) {
        for (int i = 0; i < 3; i++) {
            url = url.substring(url.indexOf("/") + 1);
        }
        return url;
    }

    @ApiOperation("截取媒体服务器路径")
    public static String extractServerUrl(String url) {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            index = url.indexOf("/", index + 1);
        }
        url = url.substring(0, index + 1);
        return url;
    }

    @ApiOperation("通话结束原因对照")
    public static String searchCalledRelCause(String cause) {
        String result;
        switch (cause) {
            case "16":
                result = "1";
                break;
            case "17":
                result = "2";
                break;
            case "27":
                result = "3";
                break;
            case "19":
                result = "4";
                break;
            case "20":
                result = "4";
                break;
            case "204":
                result = "5";
                break;
            case "1":
                result = "6";
                break;
            case "4":
                result = "7";
                break;
            case "31":
                result = "1";
                break;
            case "21":
                result = "11";
                break;
            default:
                result = "99";

        }
        return result;
    }



    @ApiOperation("(短信)结束码和结束理由")
    public static SmsCodeEnum smsResultCodxe(String finishState) {
        if ("1".equals(finishState)) {
            return SmsCodeEnum.one;
        }
        if ("2".equals(finishState)) {
            return SmsCodeEnum.two;
        }
        if ("3".equals(finishState)) {
            return SmsCodeEnum.three;
        }
        if ("4".equals(finishState)) {
            return SmsCodeEnum.four;
        }
        if ("5".equals(finishState)) {
            return SmsCodeEnum.five;
        }
        return SmsCodeEnum.ninety_nine;

    }

    @ApiOperation("(通话)结束码和结束理由")
    public static ResultCodeEnum resultCode(String finishState) {
        if ("1".equals(finishState)) {
            return ResultCodeEnum.one;
        }
        if ("2".equals(finishState)) {
            return ResultCodeEnum.two;
        }
        if ("3".equals(finishState)) {
            return ResultCodeEnum.three;
        }
        if ("4".equals(finishState)) {
            return ResultCodeEnum.four;
        }
        if ("5".equals(finishState)) {
            return ResultCodeEnum.five;
        }
        if ("6".equals(finishState)) {
            return ResultCodeEnum.six;
        }
        if ("7".equals(finishState)) {
            return ResultCodeEnum.seven;
        }
        if ("8".equals(finishState)) {
            return ResultCodeEnum.eight;
        }
        if ("9".equals(finishState)) {
            return ResultCodeEnum.nine;
        }
        if ("10".equals(finishState)) {
            return ResultCodeEnum.ten;
        }
        if ("11".equals(finishState)) {
            return ResultCodeEnum.eleven;
        }
        if ("20".equals(finishState)) {
            return ResultCodeEnum.twenty;
        }
        return ResultCodeEnum.ninety_nine;
    }


    @ApiOperation("拼接iccp话单报文")
    public static Callstat buildCallStat(CustomerReceivesDataInfo customerReceivesDataInfo, String xAreaCode, String calledCode,String supplierId,String chargeType) {
        ResultCodeEnum resultCodeEnum = UnicomUtil.resultCode(customerReceivesDataInfo.getCalledRelCause());
        //获取额外参数
        String paraMap = customerReceivesDataInfo.getKey7();
        String businessId;
        //判断是否为json
        try {
            JSONObject jsonObject = JSONObject.parseObject(paraMap);
            businessId = jsonObject.getString("businessId");
        } catch (Exception e) {
            businessId = "";
        }
        Callstat callstat = new Callstat();
        //通话开始时间，通话发生时间
        callstat.setStreamnumber(ThirdUtils.parseTime(customerReceivesDataInfo.getAbStartCallTime(), customerReceivesDataInfo.getAbStopCallTime()));
        callstat.setServiceid(StringUtils.isBlank(businessId) ? "" : businessId);
        callstat.setServicekey("900007");
        callstat.setCallersubgroup("");
        callstat.setCalleesubgroup("");
        callstat.setCallerpnp("");
        callstat.setCalleepnp("");
        callstat.setMsserver("");
        callstat.setAreanumber(calledCode);
        callstat.setDtmfkey("");
        callstat.setRecordPush("");
        callstat.setCalltype("0");
        callstat.setCallcost(0);
        callstat.setCalledpartynumber(ThirdUtils.getNumberUn86(customerReceivesDataInfo.getDisplayNumber()));
        callstat.setCallingpartynumber(ThirdUtils.getNumberUn86(customerReceivesDataInfo.getCallInNum()));
        callstat.setChargemode("1");
        callstat.setSpecificchargedpar(ThirdUtils.getNumberUn86(customerReceivesDataInfo.getDisplayNumber()));
        callstat.setTranslatednumber(ThirdUtils.getNumberUn86(customerReceivesDataInfo.getCalledNum()));
        //通话开始时间，通话发生时间
        callstat.setStartdateandtime(ThirdUtils.getTime(customerReceivesDataInfo.getAbStartCallTime(), customerReceivesDataInfo.getAbStopCallTime()));
        callstat.setStopdateandtime(ThirdUtils.getTime(customerReceivesDataInfo.getAbStopCallTime(), customerReceivesDataInfo.getAbStartCallTime()));
        callstat.setDuration(String.valueOf(customerReceivesDataInfo.getDuration()));
        callstat.setChargeclass("102");
        //绑定ID
        callstat.setTransparentparamet(customerReceivesDataInfo.getMessageId());
        callstat.setAcrcallid(ThirdUtils.acrCallId(customerReceivesDataInfo.getAbStartCallTime()));
        callstat.setOricallednumber(ThirdUtils.getNumberUn86(customerReceivesDataInfo.getCallInNum()));
        callstat.setOricallingnumber(ThirdUtils.getNumberUn86(customerReceivesDataInfo.getCalledNum()));
        callstat.setReroute("1");
        callstat.setGroupnumber(customerReceivesDataInfo.getVccId());
        callstat.setCallcategory("1");
        callstat.setChargetype(chargeType);
        callstat.setAcrtype("1");
        callstat.setVideocallflag(ThirdUtils.videoCallFlag(customerReceivesDataInfo.getSrfmsgid(), customerReceivesDataInfo.getDuration()));
        callstat.setForwardnumber(customerReceivesDataInfo.getAcrCallId());
        callstat.setExtforwardnumber(StringUtils.isBlank(customerReceivesDataInfo.getCallRingTime()) ? "" : customerReceivesDataInfo.getCallRingTime());
        callstat.setSrfmsgid(StringUtils.isBlank(customerReceivesDataInfo.getSrfmsgid()) ? "" : customerReceivesDataInfo.getSrfmsgid());
        callstat.setBegintime(customerReceivesDataInfo.getAbStartCallTime());
        callstat.setReleasecause(String.valueOf(resultCodeEnum.getCode()));
        callstat.setReleasereason(resultCodeEnum.getDesc());
//        callstat.setKey5(CdrTypeCodeEnum.supplier.getCode());
        callstat.setUserpin(supplierId);
        callstat.setKey3(xAreaCode);
        //callstat.setCalledareacode(xAreaCode);
        callstat.setKey2(customerReceivesDataInfo.getAbStartCallTime());
        callstat.setKey1("");
        callstat.setKey4("");
        callstat.setBNumFail("");
        return callstat;
    }

    @ApiOperation("返回集团成功报文")
    public static void responToClient(HttpServletResponse response, Object data) {
        try {
            // 通过设置响应头控制浏览器以UTF-8的编码显示数据，如果不加这句话，那么浏览器显示的将是乱码
            response.setHeader("content-type", "application/json");
            // 指定以UTF-8编码进行转换
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(JSONUtil.parse(data));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            log.error("返回报文失败：", e);
        }
    }

    @ApiOperation("数组转对象")
    public static <T> T mapToObject(Map source, Class<T> target) throws InstantiationException, IllegalAccessException {
        Field[] fields = target.getDeclaredFields();
        T o = target.newInstance();
        for (Field field : fields) {
            Object val;
            if ((val = source.get(field.getName())) != null) {
                field.setAccessible(true);
                field.set(o, val);
            }
        }
        return o;
    }


    public static String playbackCompiler(String callerIvr, String calledIvr, String callerIvrBefore) {
        Map<String, String> map = UnicomLocalCacheService.AUDIO_CODE_CACHE;
        if (!StringUtil.isBlank(map.get(callerIvr))) {
            callerIvr = map.get(callerIvr);
        }
        if (!StringUtil.isBlank(map.get(calledIvr))) {
            calledIvr = map.get(calledIvr);
        }
        if (!StringUtil.isBlank(map.get(callerIvrBefore))) {
            callerIvrBefore = map.get(callerIvrBefore);
        }
        if(StringUtils.isBlank (callerIvr) && StringUtils.isBlank (calledIvr) && StringUtils.isBlank (callerIvrBefore)){
            return "0,0,0";
        }
        return callerIvrBefore + "@" + callerIvr + ";" + calledIvr + "," + callerIvrBefore + "@" + callerIvr + ";" + calledIvr;
    }




}
