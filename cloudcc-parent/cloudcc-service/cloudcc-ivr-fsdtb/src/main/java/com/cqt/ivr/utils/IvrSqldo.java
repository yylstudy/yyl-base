package com.cqt.ivr.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IvrSqldo {

	private static final Logger log = LoggerFactory.getLogger(IvrSqldo.class);
	
	public String elevaluesselsql(String[] ivrids, String LOG_TAG) {
		String sqlcmd;
		sqlcmd = "select sc.id,sc.nodevalueid,sc.app,sc.paratype,sc.paraid,sc.paravalue,s.ivrid,s.nodeid "
				+ "from `tx_company_pbxivr_nodeparavalue` as sc right join `tx_company_pbxivr_nodevalue` as s on s.nodevalueid = sc.nodevalueid "
				+ "where s.ivrid";
		if(ivrids!=null){
			for(int i=0;i<ivrids.length;i++){
				if(i==0){
					sqlcmd += (" = '"+ivrids[i]+"'");
				} else {
					sqlcmd += (" or s.ivrid = '"+ivrids[i]+"'");
				}
			}
		}else {
			sqlcmd += (" in (select ivrid from `tx_company_pbxivr`)");
		}
		sqlcmd += " order by s.nodeid";
		log.info(LOG_TAG + "执行sql：" + sqlcmd);
		return sqlcmd;
	}

	public String flowdataselsql(String[] ivrids, String LOG_TAG){
		String sqlcmd;
		sqlcmd = "select * from `tx_company_pbxivr`";
		if(ivrids!=null){
			for(int i=0;i<ivrids.length;i++){
				if(i==0){
					sqlcmd +=(" where `ivrid`='"+ivrids[i]+"'");
				} else {
					sqlcmd +=(" or `ivrid`='"+ivrids[i]+"'");
				}
			}
		}
		log.info(LOG_TAG + "执行sql：" + sqlcmd);
		return sqlcmd;
	}
		
}
