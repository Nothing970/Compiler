package com.compiler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class TokenTable {
	public LinkedList<Token> tokenList = new LinkedList<Token>();
	
	public int addNewToken(Token newToken) {
		this.tokenList.add(newToken);
		return 1;
	}
	
	public void outputToken(String fileName) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
		for(Token tmpToken : tokenList ){
	         out.write("< " + tmpToken.wordName.toUpperCase() + " , " + tmpToken.wordValue + " >\r\n");
	    }
		out.close();
	}
	
}
