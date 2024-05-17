package com.cqt.unicom.common;

import cn.hutool.core.util.StrUtil;
import com.cqt.model.unicom.entity.Hcode;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.unicom.mapper.HCodeDao;
import com.cqt.unicom.util.UnicomUtil;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @program: private-number-parent
 * @description:
 * @author: yy
 * @create: 2023-05-15 16:03
 **/

@Component
@Slf4j
@RequiredArgsConstructor
public class HcodeCommon {

    private final RedissonUtil redissonUtil;

    public  String getChargeType(String aNumber, String bNumber) {
        log.info("Anumber:" + aNumber + "===========Bnumber" + bNumber);
        String chargeType = "1";
        //判断是否为95/96 10/11/12  95/96  市话号码
        if (checkfree(aNumber) || checkfree(bNumber)) {
            chargeType = "0";
            return chargeType;
        }
        String aHcode;
        String bHcode;
        //判断是否为手机号 如果是则从缓存中取手机H码
        aHcode = checkMobilePhone(aNumber);
        log.info("AHcode:" + aHcode);
        //上一步不为手机号 则从缓存中取固话H码
        if ("".equals(aHcode)) {
            aHcode = checkTelPhone(aNumber);
        }
        bHcode = checkMobilePhone(bNumber);
        if ("".equals(bHcode)) {
            bHcode = checkTelPhone(bNumber);
        }
        if (aHcode != null && bHcode != null && !"".equals(aHcode) && !"".equals(bHcode)) {
            if (aHcode.equals(bHcode)) {
                chargeType = "0";
            }
        }
        return chargeType;
    }

    @ApiOperation("获取手机号码H码")
    private  String checkMobilePhone(String number) {
        if (number == null) {
            return "";
        }
        String hcode = "";
        String pattern = "^0?1[3|4|5|6|7|8|9][\\d]{9}";
        try {
            boolean isPhone = Pattern.matches(pattern, number);
            if (isPhone) {
                if (number.startsWith("0")) {
                    hcode = redissonUtil.getString ("h_"+number.substring(1, 8));
                    log.info("获取到手机H码：" + hcode);

                } else {
                    try {
                        hcode =redissonUtil.getString("h_"+number.substring(0, 7));
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        log.error("报错号码：" + number.substring(0, 7));
                    }

                }
                if (hcode == null || "".equals(hcode)) {
                    log.error("获取到不存在H码的手机号码" + number);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hcode;
    }





    @ApiOperation("获取固话H码")
    private  String checkTelPhone(String number) {
        if (number == null) {
            return "";
        }
        String hcode = "";
        String pattern = "^(010|02\\d|0[3-9]\\d{2})?\\d{6,8}$";
        boolean isTelPhone = Pattern.matches(pattern, number);
        if (isTelPhone) {
            if (number.startsWith("01") || number.startsWith("02")) {
                hcode = number.substring(0, 3);
            } else {
                hcode = number.substring(0, 4);
            }
        }
        return hcode;
    }


    @ApiOperation("判断是否为400 800 10/11/12短号  95/96号码 如果有则为市话")
    private static boolean checkfree(String number) {
        if (number == null) {
            return false;
        }
        String pattern = "^(((9)[5-6])|((1)[0-2])|(400))[\\d]{1,}";
        return Pattern.matches(pattern, number);
    }

    /**
     * 获取区号
     * @param number
     * @return
     */
    public   String getNumberCode(String number){
        if(StrUtil.isBlank (number)){
            return "";
        }
        String hcode=checkTelPhone(number);
        if(StrUtil.isBlank (hcode)){
            hcode=checkMobilePhone(number);
        }
        return  hcode;
    }

}
