package com.compiler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class SignTable {
	public String tableName;
	public SignTable parTable;
	public int offset;
	public LinkedList<String> signIndex;
	public LinkedList<Sign> signList;
	public int paras;
	public String retType;
	
	public SignTable() {
		this.tableName = null;
		this.parTable = null;
		this.offset = 0;
		this.signIndex = new LinkedList<String>();
		this.signList = new LinkedList<Sign>();
		this.paras = 0;
		retType = null;
	}
	
	public int indexOf(String indentifier) {
		return this.signIndex.indexOf(indentifier);
	}
	
	public int addNewSign(Sign newSign) {
		this.signList.add(newSign);
		this.signIndex.add(newSign.idName);
		return 1;
	}

	public Sign getSign(String indentifier) {
		int i = this.indexOf(indentifier);
		if(i == -1) {
			return null;
		}else {
			return signList.get(i);
		}
	}

	public static int getOffset(SignTable tmpTable, String indentifier) {
		while(tmpTable != null) {
			if(tmpTable.indexOf(indentifier) != -1) {
				break;
			}else {
				tmpTable = tmpTable.parTable;
			}
		}
		if(tmpTable == null) {
			return -1;
		}else {
			return tmpTable.getSign(indentifier).offset;
		}
	}
	
	public void outputSign(String fileName) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
		int i = 0;
		for(Sign tmpSign : signList ){
	         out.write(i + ":	" + tmpSign.idName + "\r\n");
	         i++;
	    }
		out.close();
	}
	
}
