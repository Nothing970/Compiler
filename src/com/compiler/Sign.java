package com.compiler;

public class Sign {
	public String idName;
	public String type;
	public int offset;
	public SignTable tablePtr;
	public int dimens;
	public String arrayAd;
	
	public Sign() {
		this.idName = null;
		this.type = null;
		this.offset = 0;
		this.tablePtr = null;
		this.dimens = 0;
		this.arrayAd = null;
	}
}
