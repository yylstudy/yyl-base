package com.cqt.broadnet.web.x.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.broadnet.web.x.mapper.TelCodeMapper;
import com.cqt.broadnet.web.x.service.TelCodeService;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * @author linshiqiang
 * date:  2023-02-17 9:53
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelCodeServiceImpl implements TelCodeService {

    private final TelCodeMapper telCodeMapper;

    private final RedissonUtil redissonUtil;

    @Override
    public String getAreaCode(String telCode) {
        String key = PrivateCacheUtil.getAreaCodeOfTelCodeKey(telCode);
        String areaCodeVal = redissonUtil.getString(key);
        if (StrUtil.isNotEmpty(areaCodeVal)) {
            return areaCodeVal;
        }
        String areaCode = telCodeMapper.getAreaCodeByTelCode(telCode);
        if (StrUtil.isEmpty(areaCode)) {
            return "";
        }
        redissonUtil.setString(key, areaCode);
        return areaCode;
    }

    @Override
    public String getChargeType(String aNumber, String bNumber) {
        String chargeType = "1";
        //判断是否为95/96 10/11/12  95/96  市话号码
        if (checkfree(aNumber) || checkfree(bNumber)) {
            return "0";
        }
        //判断是否为手机号 如果是则从缓存中取手机H码
        String aHcode = checkMobilePhone(aNumber);
        //上一步不为手机号 则从缓存中取固话H码
        if ("".equals(aHcode)) {
            aHcode = checkTelPhone(aNumber);
        }
        String bHcode = checkMobilePhone(bNumber);
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

    /**
     * 获取手机号码H码
     */
    public String checkMobilePhone(String number) {
        if (number == null) {
            return "";
        }
        String hcode = "";
        String pattern = "^0?1[3|4|5|6|7|8|9][\\d]{9}";
        try {
            boolean isPhone = Pattern.matches(pattern, number);
            if (isPhone) {
                if (number.startsWith("0")) {
                    hcode = getAreaCode(number.substring(1, 8));
                } else {
                    hcode = getAreaCode(number.substring(0, 7));
                }
            }
        } catch (Exception e) {
            log.error("checkMobilePhone error: ", e);
        }
        return hcode;
    }

    /**
     * 获取固话H码
     */
    public static String checkTelPhone(String number) {
        if (number == null) {
            return "";
        }
        String hcode = "";
        String pattern = "^0((10)|((2|3|5|6|7|8|9)[0-9])|(4|[1-9])])[\\d]{7,}";
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

    /**
     * 判断是否为400 800 10/11/12短号  95/96号码 如果有则为市话
     */
    public static boolean checkfree(String number) {
        if (number == null) {
            return false;
        }
        String pattern = "^(((9)[5-6])|((1)[0-2])|(400))[\\d]{1,}";
        return Pattern.matches(pattern, number);
    }
}
