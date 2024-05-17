package com.cqt.model.unicom.entity;

public class Hcode {
    public String telcode;
    public String areacode;
	public String getTelcode() {
		return telcode;
	}
	public void setTelcode(String telcode) {
		this.telcode = telcode;
	}
	public String getAreacode() {
		return areacode;
	}
	public void setAreacode(String areacode) {
		this.areacode = areacode;
	}
	@Override
	public String toString() {
		return "Hcode [telcode=" + telcode + ", areacode=" + areacode + "]";
	}



}
