package com.cqt.cdr.util;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * 市话长途工具集.
 * @author yy
 * @version 2018-02-09
 */
@Slf4j
public class LocalOrLongUtils {
	private static Logger logger = LoggerFactory.getLogger(LocalOrLongUtils.class);
	public  static  Map<String, String> hcodeCache =  new HashMap<String, String>();







	/**
	 * 获取号码类型是市话还是长途
	 * @return 0 市话   1 长途
	 * @author yy
	 * @version 2018-02-09
	 */
	public static String getChargeType(String Anumber, String Bnumber, RedissonUtil redissonUtilL) {
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
		AHcode = checkMobilePhone(Anumber, redissonUtilL);
		log.info("AHcode:"+AHcode);
		//上一步不为手机号 则从缓存中取固话H码
		if("".equals(AHcode)){
			AHcode =  checkTelPhone(Anumber);
		}
		BHcode = checkMobilePhone(Bnumber, redissonUtilL);
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
	/**
	 * 获取手机号码H码
	 * @author yy
	 * @version 2018-02-09
	 */
	public static String checkMobilePhone(String number, RedissonUtil redissonUtilL){
		if(number==null){
			return "";
		}
		String hcode = "";
		String pattern = "^0?1[3|4|5|6|7|8|9][\\d]{9}";
		boolean isPhone = Pattern.matches(pattern, number);
		if(isPhone){
			if(number.startsWith("0")){
				hcode = redissonUtilL.getHashByItem("cloudcc:all_tel_area_code", number.substring(1, 8));
			}else{
				try {
					hcode = redissonUtilL.getHashByItem("cloudcc:all_tel_area_code", number.substring(0, 7));
				} catch (Exception e) {
					logger.error(e.getMessage());
					logger.error("报错号码："+number.substring(0, 7));
				}

			}
			if(hcode==null||"".equals(hcode)){
				logger.error("获取到不存在H码的手机号码"+number);
			}
		}
		return hcode;
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
	 * 判断是否为400 800 10/11/12短号  95/96号码 如果有则为市话
	 * @author yy
	 * @version 2018-02-09
	 */
	public static boolean checkfree(String number){
		if(number==null){
			return false;
		}
		String pattern = "^(((9)[5-6])|((1)[0-2])|(400))[\\d]{1,}";
		boolean isFree = Pattern.matches(pattern, number);
		return isFree;
	}

	/**
	 * 获取区号
	 * @param number
	 * @return
	 */
	public static String getNumberCode(String number, RedissonUtil redissonUtilL){
		String hcode=checkTelPhone(number);
		if(!StringUtils.isNotEmpty(hcode)){
			hcode=checkMobilePhone(number, redissonUtilL);
		}
		return  hcode;
	}
}
