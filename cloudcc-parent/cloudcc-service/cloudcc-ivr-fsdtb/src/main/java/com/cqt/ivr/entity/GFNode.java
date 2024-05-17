package com.cqt.ivr.entity;

import lombok.Data;

import java.util.HashMap;

@Data
public class GFNode {
	String nodeid;
	String type;
	String app;
	HashMap<String,String> paras;
}
