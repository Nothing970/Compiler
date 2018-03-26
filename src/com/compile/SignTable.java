package com.compile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class SignTable {
	public LinkedList<String> signIndex = new LinkedList<String>();
	public LinkedList<Sign> signList = new LinkedList<Sign>();
	
	public int indexOf(String indentifier) {
		return this.signIndex.indexOf(indentifier);
	}
	
	public int addNewSign(Sign newSign) {
		this.signList.add(newSign);
		this.signIndex.add(newSign.idName);
		return 1;
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
