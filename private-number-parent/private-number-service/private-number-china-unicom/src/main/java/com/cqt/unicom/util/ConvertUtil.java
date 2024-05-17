package com.cqt.unicom.util;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.enums.ResultCodeEnum;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * @author huweizhong
 * date  2023/7/13 16:03
 */
@Service
@Slf4j
public class ConvertUtil {

    @Autowired
    private RedissonUtil redissonUtil;

    public String getChargeType(String Anumber, String Bnumber) {
        log.info("Anumber:"+Anumber+"===========Bnumber"+Bnumber);
        String ChargeType = "1";
        //判断是否为95/96 10/11/12  95/96  市话号码
        if(checkfree(Anumber)||checkfree(Bnumber)){
            ChargeType = "0";
            return ChargeType;
        }
        String AHcode = "";
        String BHcode = "";
        //判断是否为手机号 如果是则从缓存中取手机H码
        AHcode = checkMobilePhone(Anumber);
        log.info("AHcode:"+AHcode);
        //上一步不为手机号 则从缓存中取固话H码
        if("".equals(AHcode)){
            AHcode =  checkTelPhone(Anumber);
        }
        BHcode = checkMobilePhone(Bnumber);
        if("".equals(BHcode)){
            BHcode =  checkTelPhone(Bnumber);
        }
        if(AHcode!=null&&BHcode!=null&&!"".equals(AHcode)&&!"".equals(BHcode)){
            if(AHcode.equals(BHcode)){
                ChargeType = "0";
            }
        }
        return ChargeType;
    }

    public static boolean checkfree(String number){
        if(number==null){
            return false;
        }
        String pattern = "^(((9)[5-6])|((1)[0-2])|(400))[\\d]{1,}";
        boolean isFree = Pattern.matches(pattern, number);
        return isFree;
    }

    /**
     * 获取固话H码
     * @author yy
     * @version 2018-02-09
     */
    public static String checkTelPhone(String number){
        if(number==null){
            return "";
        }
        String hcode = "";
        String pattern = "^(010|02\\d|0[3-9]\\d{2})?\\d{6,8}$";
        boolean isTelPhone = Pattern.matches(pattern, number);
        if(isTelPhone){
            if(number.startsWith("01")||number.startsWith("02")){
                hcode = number.substring(0, 3);
            }else{
                hcode = number.substring(0, 4);
            }
        }
        return hcode;
    }

    /**
     * 获取手机号码H码
     * @author yy
     * @version 2018-02-09
     */

    public  String checkMobilePhone(String number){


        if(number==null){
            return "";
        }
        String hcode = "";
        String pattern = "^0?1[3|4|5|6|7|8|9][\\d]{9}";
        try {
            boolean isPhone = Pattern.matches(pattern, number);
            if(isPhone){
                if(number.startsWith("0")){
                    hcode = redissonUtil.getString("h_"+number.substring(1, 8));
                    log.info("获取到手机H码："+hcode);

                }else{
                    try {
                        hcode = redissonUtil.getString("h_"+number.substring(0, 7));
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        log.error("报错号码："+number.substring(0, 7));
                    }

                }
                if(hcode==null||"".equals(hcode)){
                    log.error("获取到不存在H码的手机号码"+number);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hcode;
    }


    /**
     * 判断number是否86开头,是的话去掉前两位
     **/
    public  String getNumberUn86(String number) {
        if (StrUtil.isEmpty(number)){
            return number;
        }
        if (number.startsWith("86")) {
            return number.substring(2);
        }
        return number;
    }

    public  String acrCallId(String startTime) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        return "C" + startTime + uuid;

    }
    public  String videoCallFlag(String recordUrl, Integer callDuration) {
        if (StringUtils.isNotBlank(recordUrl)) {
            return String.valueOf(callDuration);
        }
        return "0";
    }

    /**
     * (通话)结束码和结束理由
     **/
    public ResultCodeEnum cnResultCode(Integer finishState) {
        if (finishState == 1) {
            return ResultCodeEnum.one;
        }
        if (finishState == 2) {
            return ResultCodeEnum.two;
        }
        if (finishState == 3) {
            return ResultCodeEnum.three;
        }
        if (finishState == 4) {
            return ResultCodeEnum.four;
        }
        if (finishState == 5) {
            return ResultCodeEnum.five;
        }
        if (finishState == 6) {
            return ResultCodeEnum.six;
        }
        if (finishState == 7) {
            return ResultCodeEnum.seven;
        }
        if (finishState == 8) {
            return ResultCodeEnum.eight;
        }
        if (finishState == 9) {
            return ResultCodeEnum.nine;
        }
        if (finishState == 10) {
            return ResultCodeEnum.ten;
        }
        if (finishState == 11) {
            return ResultCodeEnum.eleven;
        }
        if (finishState == 12) {
            return ResultCodeEnum.twelve;
        }
        if (finishState == 91) {
            return ResultCodeEnum.ninetyOne;
        }
        if (finishState == 20) {
            return ResultCodeEnum.twenty;
        }
        return ResultCodeEnum.ninetyNine;
    }
}
