package com.cqt.thirdchinanet.entity.sms;

import lombok.Data;

@Data
public class SmsRequestHeader {
	/*
	{
	    "header": {
	        "streamNumber":"1c7799203edf9c71d1b2762dc68725b9",
	        "messageId":"35135rgsetgqw341qgsd24t"
	    },
	    "body": {
	        "aPhoneNumber": "18559106605",
	        "inPhoneNumber": "18559752475",
	        "gsmCenter": "008613010112500",
	        "inContent": "测试短信。"
	    }
	}
	*/

    //流水号
    private String streamNumber;
    //业务流水号
    private String messageId;
    //短信标识
    private String messageReference;


}
