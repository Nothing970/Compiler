package com.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class LexicalAnalyzer {
	//关键字集合
	private static HashMap<String, Integer> keywordMap = new HashMap<String, Integer>(){{  
	      put("int" , 1);
	      put("float" , 2);
	      put("char" , 3);
	      put("string" , 4);
	      put("if" , 5);
	      put("else" , 6);
	      put("while" , 7);
	      put("break" , 8);
	      put("continue" , 9);
	      put("return" , 10);
	      put("+" , 11);
	      put("-" , 12);
	      put("*" , 13);
	      put("/" , 14);
	      put("<" , 15);
	      put(">" , 16);
	      put("<=" , 17);
	      put(">=" , 18);
	      put("==" , 19);
	      put("!=" , 20);
	      put("=" , 21);
	      put("&&" , 22);
	      put("||" , 23);
	      put("!" , 24);
	      put("," , 25);
	      put(";" , 26);
	      put("[" , 27);
	      put("]" , 28);
	      put("(" , 29);
	      put(")" , 30);
	      put("{" , 31);
	      put("}" , 32);
	      put("\'" , 33);
	      put("\"" , 34);
	      put("ID" , 35);
	      put("INTCONST" , 36);
	      put("REALCONST" , 37);
	      put("CHARCONST" , 38);
	      put("STRINGCONST" , 39);
	}};
	
	
	//操作符集合
	private static HashSet<Integer> operatorSet = new HashSet<Integer>() {{
		add((int)'+');
		add((int)'-');
		add((int)'*');
		add((int)'/');
		add((int)'<');
		add((int)'>');
		add((int)'=');
		add((int)'!');
		add((int)'&');
		add((int)'|');
	}};
	
	
	//分隔符集合
	private static HashSet<Integer> delimiterSet = new HashSet<Integer>() {{
		add((int)',');
		add((int)';');
		add((int)'(');
		add((int)')');
		add((int)'[');
		add((int)']');
		add((int)'{');
		add((int)'}');
		add((int)'\'');
		add((int)'\"');
	}};
	
	private static File srcFile = null;//读取源文件
	private static BufferedReader srcReader = null;//读取文件
	private static int curChar = 0;//主控程序当前在处理的字符
	private static int charNum = 0;//当前读入的字符数
	
	private static SignTable  mySignTable = new SignTable();//符号表
	private static TokenTable myTokenTable = new TokenTable();//Token序列
	
	//主控程序
	public static void mainContrl(String srcPath) throws IOException {
		srcFile = new File(srcPath);
	    srcReader = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile)));
	    srcReader .mark((int)srcFile.length() + 1);
	    curChar = srcReader.read();
	    charNum++;
	    //根据读入字符的种类进入不同的函数，空白字符略过，非法字符输出提示信息并跳过
	    while(curChar != -1) {
	    	if(curChar == ' ' || curChar == '\r' || curChar == '\n' || curChar == '\t'){
	    		curChar = srcReader.read();
	    	    charNum++;
	    	}else if((curChar >= 'A' && curChar <= 'Z') || (curChar >= 'a' && curChar <= 'z') || curChar == '_') {
	    		recogKeywordOrId(curChar);
	    	}else if((curChar >= '0' && curChar <= '9')) {
	    		recogNumber(curChar);
	    	}else if(operatorSet.contains(curChar)) {
	    		recogOperatorOrComment(curChar);
	    	}else if(delimiterSet.contains(curChar)) {
	    		recogDelimiter(curChar);
	    	}
	    	else {
	    		System.out.print((char)curChar + " is not a valid character!");
	    		curChar = srcReader.read();
	    		charNum++;
	    	}
	    }
	    //输出符号表以及Token序列文件
	    outputTokenAndSign();
	    srcReader.close();
	}
	
	//识别关键字和标识符
	public static void recogKeywordOrId(int firChar) throws IOException {
		String recogResult = "";
		int nextChar = srcReader.read();
	    charNum++;
		recogResult = recogResult + (char)firChar;
		//循环读取下一个字符，全部放入字符串中，直到出现非字符，非数字，非下划线的字符
		while((nextChar >= 'A' && nextChar <= 'Z') || (nextChar >= 'a' && nextChar <= 'z') || (nextChar >= '0' && nextChar <= '9') || nextChar == '_') {
			recogResult = recogResult + (char)nextChar;
			nextChar = srcReader.read();
			charNum++;
		}
		//判断是关键字，直接加入Token序列中
		if(keywordMap.get(recogResult) != null) {
			Token newToken = new Token(recogResult, keywordMap.get(recogResult), 0);
			myTokenTable.addNewToken(newToken);
		}else if(recogResult.length() > 63) {//如果不是关键字但长度超出63,输出提示信息，不放入符号表中
			System.out.println("The length of " + recogResult + " is more than 63");
		}else {//是合法的标识符，判断符号表中是否有相同，没有则加入符号表，最后增加Token序列
			if(mySignTable.indexOf(recogResult) == -1) {
				Sign newSign = new Sign(recogResult);
				mySignTable.addNewSign(newSign);
			}
			Token newToken = new Token("ID", keywordMap.get("ID"), mySignTable.indexOf(recogResult));
			myTokenTable.addNewToken(newToken);
		}
		//更新主控程序的当前字符，nextChar没有使用，则直接赋值
		curChar = nextChar;
	}
	
	public static void recogNumber(int firChar) throws IOException {
		String recogResult = "";
		int point = 0;//小数点出现次数
		int zeroHeadNum = 0;
		int nextChar = srcReader.read();
	    charNum++;
		recogResult = recogResult + (char)firChar;
		//判断数字开头有几个连续的0，方便后面去掉
		if(firChar == '0') {
			zeroHeadNum++;
			while(nextChar == '0') {
				zeroHeadNum++;
				recogResult = recogResult + (char)nextChar;
				nextChar = srcReader.read();
			    charNum++;
			}
		}
		//数字和第一次出现的小数点放入识别字符串中
		while((nextChar >= '0' && nextChar <= '9') || (nextChar == '.' && point == 0)){
			recogResult = recogResult + (char)nextChar;
			if(nextChar == '.') {
				point++;
			}
			nextChar = srcReader.read();
		    charNum++;
		}
		//判断小数点后是否有数字，如没有则回退到小数点前，重新读入小数点，方便后面赋值给curChar
		if(recogResult.length() == recogResult.indexOf('.') + 1) {
			recogResult = recogResult.substring(0, recogResult.length() - 1);
			charNum = charNum - 2;
			srcReader.reset();
			srcReader.skip(charNum);			
			nextChar = srcReader.read();
			charNum++;
			point--;
		}
		//判断字符串中是否只有0
		if(zeroHeadNum == recogResult.length()) {
			recogResult = "0";
		}else if(recogResult.charAt(zeroHeadNum) == '.') {//连续0后面是小数点，则只保留一个0
			recogResult = recogResult.substring(zeroHeadNum-1, recogResult.length());
		}else {//连续0后面是数字，则去掉前面所有的0
			recogResult = recogResult.substring(zeroHeadNum, recogResult.length());
		}
		//加入符号表和Token序列
		if(mySignTable.indexOf(recogResult) == -1) {
			Sign newSign = new Sign(recogResult);
			mySignTable.addNewSign(newSign);
		}
		Token newToken = null;
		if(point == 1) {
			newToken = new Token("REALCONST", keywordMap.get("REALCONST"), mySignTable.indexOf(recogResult));
		}else {
			newToken = new Token("INTCONST", keywordMap.get("INTCONST"), mySignTable.indexOf(recogResult));
		}
		myTokenTable.addNewToken(newToken);
		//更新主控程序的当前字符，nextChar没有使用，则直接赋值
		curChar = nextChar;
	}
	
	public static void recogOperatorOrComment(int firChar) throws IOException {
		int nextChar = srcReader.read();
    	charNum++;
    	//匹配开始的字符进入不同case处理
	    switch(firChar) {
	    case '/' :
	    	//下一个字符为*，则后面的全部为注释
	    	if(nextChar == '*') {
	    		nextChar = srcReader.read();
	        	charNum++;
	        	//后面的所有字符跳过，直到出现*/表示注释结束
	    		while(nextChar != -1) {
	    			if(nextChar == '*') {
	    				nextChar = srcReader.read();
	    				charNum++;
	    				if(nextChar == '/') {
	    					curChar = srcReader.read();
	    					charNum++;
	    					break;
	    				}
	    			}else {
	    				nextChar = srcReader.read();
	    	        	charNum++;
	    			}
	    		}
	    		//注释不封闭
	    		if(nextChar == -1) {
	    			System.out.println("/* is not close!");
	    			curChar = -1;
	    		}
	    	}else {//当做/处理
	    		Token newToken = new Token("/", keywordMap.get("/"), 0);
	    		myTokenTable.addNewToken(newToken);
	    		curChar = nextChar;
	    	}
	    	break;
	    case '>' :
	    	if(nextChar == '=') {
	    		Token newToken = new Token(">=", keywordMap.get(">="), 0);
	    		myTokenTable.addNewToken(newToken);
	    		curChar = srcReader.read();
	        	charNum++;
	    	}else {
	    		Token newToken = new Token(">", keywordMap.get(">"), 0);
	    		myTokenTable.addNewToken(newToken);
	    		curChar = nextChar;
	    	}
	    	break;
	    case '<' :
	    	if(nextChar == '=') {
	    		Token newToken = new Token("<=", keywordMap.get("<="), 0);
	    		myTokenTable.addNewToken(newToken);
	    		curChar = srcReader.read();
	        	charNum++;
	    	}else {
	    		Token newToken = new Token("<", keywordMap.get("<"), 0);
	    		myTokenTable.addNewToken(newToken);
	    		curChar = nextChar;
	    	}
	    	break;
	    case '=' :
	    	if(nextChar == '=') {
	    		Token newToken = new Token("==", keywordMap.get("=="), 0);
	    		myTokenTable.addNewToken(newToken);
	    		curChar = srcReader.read();
	        	charNum++;
	    	}else {
	    		Token newToken = new Token("=", keywordMap.get("="), 0);
	    		myTokenTable.addNewToken(newToken);
	    		curChar = nextChar;
	    	}
	    	break;
	    case '!' :
	    	if(nextChar == '=') {
	    		Token newToken = new Token("!=", keywordMap.get("!="), 0);
	    		myTokenTable.addNewToken(newToken);
	    		curChar = srcReader.read();
	        	charNum++;
	    	}else {
	    		Token newToken = new Token("!", keywordMap.get("!"), 0);
	    		myTokenTable.addNewToken(newToken);
	    		curChar = nextChar;
	    	}
	    	break;
	    case '&' :
	    	if(nextChar == '&') {
	    		Token newToken = new Token("&&", keywordMap.get("&&"), 0);
	    		myTokenTable.addNewToken(newToken);
	    		curChar = srcReader.read();
	        	charNum++;
	    	}else {
	    		System.out.println("& is not a valid char!");
	    		curChar = nextChar;
	    	}
	    	break;
	    case '|' :
	    	if(nextChar == '|') {
	    		Token newToken = new Token("||", keywordMap.get("||"), 0);
	    		myTokenTable.addNewToken(newToken);
	    		curChar = srcReader.read();
	        	charNum++;
	    	}else {
	    		System.out.println("| is not a valid char!");
	    		curChar = nextChar;
	    	}
	    	break;
	    case '*' :
	    case '+' :
	    case '-' :
	    	String operator = "" + (char)firChar;
	    	Token newToken = new Token(operator, keywordMap.get(operator), 0);
    		myTokenTable.addNewToken(newToken);
    		curChar = nextChar;
    		break;
	    default:
	    	break;
	    }
	}

	public static void recogDelimiter(int firChar) throws IOException {
	    switch(firChar) {
	    case ',' :
	    case ';' :
	    case '(' :
	    case ')' :
	    case '[' :
	    case ']' :
	    case '{' :
	    case '}' :
	    	String delimiter = "" + (char)firChar;
	    	Token delimiterToken = new Token(delimiter, keywordMap.get(delimiter), 0);
    		myTokenTable.addNewToken(delimiterToken);
    		curChar = srcReader.read();
    		charNum++;
    		break;
	    case '\'' :
	    	String charResult = "\'";
	    	int nextChar = srcReader.read();
	    	charNum++;
	    	//下一个字符是\，则可能为转义符
	    	if(nextChar == '\\') {
	    		charResult = charResult + "\\";
	    		nextChar = srcReader.read();
		    	charNum++;
		    	//合法的转义符
		    	if(nextChar == 't' || nextChar == 'b' || nextChar == 'n' ||nextChar == 'r' || nextChar == 'f' || nextChar == '\'' || nextChar == '\"' || nextChar == '\\' || nextChar == '0') {
		    		charResult = charResult + (char)nextChar;
		    		nextChar = srcReader.read();
		    		charNum++;
		    		//判断是否封闭
		    		if(nextChar != '\'') {
		    			System.out.println("\'\' is not close!");
		    			curChar = nextChar;
		    		}else {
		    			charResult = charResult + (char)nextChar;
		    			if(mySignTable.indexOf(charResult) == -1) {
							Sign newSign = new Sign(charResult);
							mySignTable.addNewSign(newSign);
						}
						Token newToken = new Token("CHARCONST", keywordMap.get("CHARCONST"), mySignTable.indexOf(charResult));
						myTokenTable.addNewToken(newToken);
						curChar = srcReader.read();
						charNum++;
		    		}
		    	}else if(nextChar == -1){//\后面没有字符，不封闭
		    		System.out.println("\'\' is not close!");
	    			curChar = nextChar;
		    	}else {//非法转义符，判断是否封闭，根据结果输出提示信息
		    		String error = charResult + (char)nextChar + '\'' + " is not exist";
		    		nextChar = srcReader.read();
		    		charNum++;
		    		if(nextChar != '\'') {
		    			error = "\'\' is not close!";
		    			//nextChar未被使用，直接赋值给curChar
		    			curChar = nextChar;
		    		}else {
		    			//nextChar为'\'',被使用，读取下一个字符赋值给curChar
			    		curChar = srcReader.read();
			    		charNum++;
		    		}
		    		System.out.println(error);
		    	}
	    	}else if(nextChar == -1) {//'\''后面没有字符，不封闭
	    		System.out.println("\'\' is not close!");
    			curChar = nextChar;
	    	}else if(nextChar == '\''){//空字符情况
	    		charResult = charResult + (char)nextChar;
	    		if(mySignTable.indexOf(charResult) == -1) {
					Sign newSign = new Sign(charResult);
					mySignTable.addNewSign(newSign);
				}
				Token newToken = new Token("CHARCONST", keywordMap.get("CHARCONST"), mySignTable.indexOf(charResult));
				myTokenTable.addNewToken(newToken);
				curChar = srcReader.read();
				charNum++;
	    	}else {//'\''后面有字符
	    		charResult = charResult + (char)nextChar;
	    		nextChar = srcReader.read();
	    		charNum++;
	    		//判断是否封闭
	    		if(nextChar != '\'') {
	    			System.out.println("\'\' is not close!");
	    			curChar = nextChar;
	    		}else {
	    			charResult = charResult + (char)nextChar;
	    			if(mySignTable.indexOf(charResult) == -1) {
						Sign newSign = new Sign(charResult);
						mySignTable.addNewSign(newSign);
					}
					Token newToken = new Token("CHARCONST", keywordMap.get("CHARCONST"), mySignTable.indexOf(charResult));
					myTokenTable.addNewToken(newToken);
					curChar = srcReader.read();
					charNum++;
	    		}
	    	}
	    	break;
	    case '\"' :
	    	String strResult = "\"";
	    	int strLength = 0;
	    	int escaChar = 0;//判断是否有转义符标志
	    	int lateChar = srcReader.read();
	    	int wrongEscape = 0;//判断是否出错
	    	charNum++;
	    	//不是结束符，以及长度不大于256，则一直读入
	    	while(lateChar != -1 && strLength <256) {
	    		//字符串中出现\r、\n则会断行，不封闭
	    		if(lateChar == '\r' || lateChar == '\n') {
	    			System.out.println("\"\" is not close!");
	    			curChar = lateChar;
	    			break;
	    		}else {
	    			if(escaChar == 0) {//没有转移符读入字符直接放入字符串
	    				strResult = strResult + (char)lateChar;
	    				strLength++;
	    				if(lateChar == '\\') {//读入字符为转移字符，则标志置位
	    					escaChar = 1;
	    				}else if(lateChar == '\"' && wrongEscape == 0) {//未出错时，出现引号，字符串正常结束，加入符号表和Token序列，跳出循环
	    					if(mySignTable.indexOf(strResult) == -1) {
	    						Sign newSign = new Sign(strResult);
	    						mySignTable.addNewSign(newSign);
	    					}
	    					Token newToken = new Token("STRINGCONST", keywordMap.get("STRINGCONST"), mySignTable.indexOf(strResult));
	    					myTokenTable.addNewToken(newToken);
	    					break;
	    				}else if(lateChar == '\"' && wrongEscape == 1) {//出错时，出现引号，直接跳出循环
	    					break;
	    				}
	    				lateChar = srcReader.read();
	    				charNum++;
	    			}else {//有转义标志，判断转义符是否合法，读入字符，转义标志复位
	    				if(lateChar == 't' || lateChar == 'b' || lateChar == 'n' ||lateChar == 'r' || lateChar == 'f' || lateChar == '\'' || lateChar == '\"' || lateChar == '\\' || lateChar == '0') {
	    					strResult = strResult + (char)lateChar;
	    					lateChar = srcReader.read();
		    				charNum++;
	    					escaChar = 0;
	    				}else {//转义符不合法，错误标志置位
	    					strResult = strResult + (char)lateChar;
	    					lateChar = srcReader.read();
		    				charNum++;
	    					escaChar = 0;
	    					wrongEscape = 1;
	    				}
	    			}
	    		}
	    	}
	    	//判断循环退出条件，输出对应提示信息，并更新curChar
	    	if(lateChar == -1) {
	    		System.out.println("\"\" is not close!");
	    		curChar = -1;
	    	}else if(strLength > 256){
	    		System.out.println("\"\" is not close!");
	    		curChar = lateChar;
	    	}else if(wrongEscape == 1) {//有不合法的转义符，但字符串封闭，通过break退出
	    		System.out.println(strResult + " has valid escape character!");
	    		curChar = srcReader.read();
				charNum++;
	    	}else {//合法字符串，通过break退出
	    		curChar = srcReader.read();
				charNum++;
	    	}
	    	break;
	    default:
	    	break;
	    }
	}
	
	public static void outputTokenAndSign() throws IOException {
		mySignTable.outputSign("SignTable.txt");
		myTokenTable.outputToken("TokenTable.txt");
	}
	
}