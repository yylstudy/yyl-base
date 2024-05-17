/**
 * 
 */
package com.cqt.hmyc.web.bind.service.impl;

import com.cqt.hmyc.web.bind.mapper.HCodeDao;
import com.cqt.hmyc.web.bind.service.LocalOrLongService;
import com.cqt.hmyc.web.model.Hcode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**  
 * <p>Title: LocalOrLongServiceImpl.java</p>  
 * @author guojianyan  
 * @date 2018年4月9日  
 * @version 1.0  
 */
@Service("localOrLongService")
public class LocalOrLongServiceImpl implements LocalOrLongService {

	public static Map<String, String> hcodeCache = new HashMap<>(10240);
	@Resource
	private HCodeDao hd;

	private static final Logger log = LoggerFactory.getLogger(LocalOrLongServiceImpl.class);

	@PostConstruct
	public void init(){
		try {
			log.info("H码开始初始化");
			List<Hcode> list = hd.inithcode();
			for (Hcode hcode : list) {
				hcodeCache.put(hcode.getTelcode(), hcode.getAreacode());
			}
			log.info("H码开始初始化成功");
		} catch (Exception e) {
			log.error("H码开始初始化失败", e);

		}
	}

	@Override
	public String getChargeType(String Anumber, String Bnumber) {
		log.info("Anumber:" + Anumber + "===========Bnumber" + Bnumber);
		String ChargeType = "1";
		//判断是否为95/96 10/11/12  95/96  市话号码
		if (checkfree(Anumber) || checkfree(Bnumber)) {
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

	/**
	 * 获取手机号码H码
	 * @author yy
	 * @version 2018-02-09
	 */
	@Override
	public  String checkMobilePhone(String number){


		if(number==null){
			return "";
		}
		String hcode = "";
		String pattern = "^0?1[3|4|5|6|7|8|9][\\d]{9}";
		try {
			boolean isPhone = Pattern.matches(pattern, number);
			if (isPhone) {
				if (number.startsWith("0")) {
					hcode = hcodeCache.get(number.substring(1, 8));
					log.info("获取到手机H码：" + hcode);

				} else {
					try {
						hcode = hcodeCache.get(number.substring(0, 7));
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
	 *
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

}
