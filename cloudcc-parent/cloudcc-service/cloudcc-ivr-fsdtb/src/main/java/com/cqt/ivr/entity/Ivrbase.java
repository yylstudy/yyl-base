package com.cqt.ivr.entity;

import lombok.Data;

@Data
public class Ivrbase {
	private String id;
	private String ivrid;
	private String ivrname;
	private String context;
	private String creator;
	private String lastmodifyer;
	private String create_time;
	private String update_time;
	private String iflock;
	private String owner;
	private String flowdata;
	private String tenant_id;
	private String chanid;
}
