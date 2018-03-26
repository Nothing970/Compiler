package com.compile;

public class Token {
	public String wordName;
	public int wordCode;
	public int wordValue;
	
	public Token(String wordName, int wordCode, int wordValue) {
		this.wordName = wordName;
		this.wordCode = wordCode;
		this.wordValue = wordValue;
	}
}
