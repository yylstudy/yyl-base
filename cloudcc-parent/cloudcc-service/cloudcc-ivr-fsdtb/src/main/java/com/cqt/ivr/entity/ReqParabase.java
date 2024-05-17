package com.cqt.ivr.entity;

import lombok.Data;

@Data
public class ReqParabase {
	private String action;
	private String company_code;
	private String type;
	private String route_ids;
	private String ivr_ids;
	private String trunk_ids;
	private String cor_route_ids;
	private String blacklist_ids;
	private String sys_blacklist_ids;
	private String times_ids;
	private String timecdt_ids;
	private String ivrchans;
	private String phonenum_resource_ids;
	private String agents_ids;
	private String queue_ids;
	private String company_ids;
	private String profileIds;
	private String gatewayIds;
	private String dialplanIds;
	private String gatewayType;
	private String pollgroupNames;

}
