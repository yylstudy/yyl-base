package com.cqt.ivr.entity;

import lombok.Data;

import java.util.HashMap;

@Data
public class GFLine {
	String lineid;
	String from;
	String to;
	String app;
	HashMap<String,String> paras;

}
