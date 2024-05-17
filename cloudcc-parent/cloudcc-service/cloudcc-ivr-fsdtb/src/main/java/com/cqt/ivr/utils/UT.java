package com.cqt.ivr.utils;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class UT {

	private static final Logger log = LoggerFactory.getLogger(UT.class);

	public static String captureName(String name) {
        char[] cs=name.toCharArray();
        cs[0]-=32;
        return String.valueOf(cs);
	}
	
	public static boolean zstr(String str){
		if(str == null){
			return true;
		}
		else{
			return "".equals(str)?true:false;
		}
	}

	public static String errormsg(Exception e){
		//return e.getClass().getName();
		return e.getLocalizedMessage();
	}
	
	public static void printstr(PrintWriter pw, String str, String LOG_TAG){
		log.info(LOG_TAG + "响应xml：" + str);
		pw.print(str);
		pw.flush();
		pw.close();
		pw = null;
		log.info(LOG_TAG + "响应结束");
	}

	
	public static <T> void df_setbean(Map<String,String[]> map,T t){
		Set<String> set = map.keySet();
        for (Iterator<String> it = set.iterator();it.hasNext();) {

            String key = it.next();
            String cnt = "";
            
            for(int i=0;i<map.get(key).length;i++){
            	if(i>0){
					cnt+=",";
				}
            	cnt+=map.get(key)[i];
            }
            if(key.lastIndexOf("[]")>0){
				key = key.substring(0, key.lastIndexOf("[]"));
			}
            //System.out.println(key+":"+cnt);
        	try {
				t.getClass().getDeclaredMethod("set"+ UT.captureName(key), String.class).invoke(t,cnt);
			} catch (Exception e) {
				//e.printStackTrace();
			} 
        }
	}

	public static String selcdt(HashMap<String, String> cdt,int i){
		String sqlcmd = "";
		String t1;
		String t2 = " where ";
		if(cdt==null){
			return sqlcmd;
		}
		Set<Map.Entry<String, String>> set = cdt.entrySet();
		for(Map.Entry<String, String> e:set){
			if(e.getValue().equals("")){
				continue;
			}
			if(i>0){
				t2 = " and ";
			}
			t1 = t2 + "`" + e.getKey() + "` like '%" + e.getValue() + "%'";
			sqlcmd += t1;
			i++;
		}
		return sqlcmd;
	}

	public static String fileoutput(Document doc){
		Format xmlDocumentFormat=Format.getPrettyFormat();
		XMLOutputter outputter=new XMLOutputter(xmlDocumentFormat);
		return outputter.outputString(doc);
	}

	public static String NSinsert(String str, String dfval){
		return zstr(str)?dfval:str;
	}

}
