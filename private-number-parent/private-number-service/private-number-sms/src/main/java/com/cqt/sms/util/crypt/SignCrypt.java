package com.cqt.sms.util.crypt;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 签名工具类
 *
 * @author youngder
 */
public class SignCrypt {

    private static Pattern p = Pattern.compile("\t|\r|\n");

    /**
     * @param appSecret 加密key
     * @return String
     */
    public static String createSign(TreeMap<String, String> params, String appSecret) {
        try {
            return HmacMD5.hmacMd5(sortAndFormat(params).getBytes("UTF-8"), appSecret.getBytes("UTF-8"));
            //			return MD5Util.encryptMD5( sortAndFormat(params));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 参数排序及格式化
     *
     * @param params
     * @return
     */
    public static String sortAndFormat(TreeMap<String, String> params) {
        StringBuffer sb = new StringBuffer();
        int flag = 0;
        for (String key : params.keySet()) {
            if (flag == 0) {
                sb.append(key + "=" + params.get(key));
            } else {
                sb.append("&" + key + "=" + params.get(key));
            }

            flag++;
        }
        return sb.toString();
    }

    public static Map toMap(Object bean) {
        Class<? extends Object> clazz = bean.getClass();
        Map<Object, Object> returnMap = new HashMap<Object, Object>();
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor descriptor = propertyDescriptors[i];
                String propertyName = descriptor.getName();
                if (!propertyName.equals("class")) {
                    Method readMethod = descriptor.getReadMethod();
                    Object result = null;
                    result = readMethod.invoke(bean, new Object[0]);
                    if (null != propertyName) {
                        propertyName = propertyName.toString();
                    }
                    if (null != result) {
                        result = result.toString();
                    }
                    returnMap.put(propertyName, result);
                }
            }
        } catch (IntrospectionException e) {
            System.out.println("分析类属性失败");
        } catch (IllegalAccessException e) {
            System.out.println("实例化 JavaBean 失败");
        } catch (IllegalArgumentException e) {
            System.out.println("映射错误");
        } catch (InvocationTargetException e) {
            System.out.println("调用属性的 setter 方法失败");
        }
        return returnMap;
    }

    /**
     * 验证签名
     *
     * @param sign
     * @param appSecret
     * @param params
     * @return
     */
    public static boolean verifySign(String sign, TreeMap<String, String> params, String appSecret) {
        System.out.println("==========" + createSign(params, appSecret));
        return sign.equals(createSign(params, appSecret));
    }

    /**
     * 去除回车符、换行符、制表符
     *
     * @param content
     * @return
     */
    private static String replace_ENTER_NEWLINE_TAB_Symbol(String content) {

        if (content == null) {
            return null;
        } else {
            Matcher m = p.matcher(content);

            return m.replaceAll("");
        }

    }

    public static void main(String[] args) {
        System.out.println(UUID.randomUUID().toString());
    }

}
