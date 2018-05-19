package com.compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class SyntacticAnalyzer {
	private static HashSet<String> SyntacticVariables = new HashSet<String>();//语法变量集
	private static HashSet<String> Terminators = new HashSet<String>();//终结符集
	//产生式集合，主键为语法变量，所有由这个语法变量推出的产生式存放在集合里
	private static HashMap<String, HashSet<String>> Productions = new HashMap<String, HashSet<String>>();
	private static String startProduction = null;//最开始的产生式
	
	private static LinkedList<TreeSet<Item>> resultCollection;
	
	//存放状态转移的表，第一个下表为状态序号，HashMap键值为符号，整形为转移状态序号
	private static LinkedList<HashMap<String, Integer>> goNextTable = new LinkedList<HashMap<String, Integer>>();
	
	//action表    goto表
	private static LinkedList<HashMap<String, String>> actionTable = new LinkedList<HashMap<String, String>>();
	private static LinkedList<HashMap<String, Integer>> gotoTable = new LinkedList<HashMap<String, Integer>>();
	
	public static SemanticAnalyzer semAnalyzer = new SemanticAnalyzer();
	
	//主控函数
 	public static void mainControl(String productionPath) throws IOException {
        initialize(productionPath);
        generateAnalysisTable();
//        //countItem(resultCollection);
//		//judgeItemSetCollection(resultCollection);
		outputState();
//		//outputFirst();
//        //first("Expr");
        outputTables();
        ArrayList<String> tokenList = generateTokenList("TokenTable.txt");
        analysis(tokenList);
        semAnalyzer.outputIns("asm.s");
	}
	
 	//初始化函数，从文件读取产生式
 	//文件格式：第一行为所有的语法变量空格隔开，第二行为所有的终结符空格隔开，留一行空格，以下全部为产生式，右边用空格隔开
	public static void initialize(String productionPath) throws IOException {
		BufferedReader productionReader = new BufferedReader(new FileReader(productionPath));
		String production = productionReader.readLine();
		startProduction = production;
		while(production != null) {
			//拆解产生式，填入集合
			String productionLeft = production.substring(0, production.indexOf('-'));
			String productionRight = production.substring(production.indexOf('>') + 1, production.length());
			SyntacticVariables.add(productionLeft);
			//判断对应语法变量是否已经拥有产生式集合
			if(Productions.get(productionLeft) == null) {
				HashSet<String> newSet = new HashSet<String>();
				newSet.add(productionRight);
				Productions.put(productionLeft, newSet);
			}else {
				Productions.get(productionLeft).add(productionRight);
			}
			production = productionReader.readLine();
		}
		for(String synStr : SyntacticVariables) {
			for(String producStr : Productions.get(synStr)) {
				for(String tmpStr : producStr.split(" ")) {
					if(!SyntacticVariables.contains(tmpStr) && !tmpStr.equals("@")) {
						Terminators.add(tmpStr);
					}
				}
				
			}
		}
		productionReader.close();
	}

	//读取文件，生成Token序列，格式为< A , B >，每行一个
	public static ArrayList<String> generateTokenList(String tokenPath) throws IOException {
		BufferedReader tokenReader = new BufferedReader(new FileReader(tokenPath));
		ArrayList<String> tokenList = new ArrayList<String>();
		String tokenStr = tokenReader.readLine();
		String token = null;
		while(tokenStr != null) {
			token = tokenStr.substring(2, tokenStr.lastIndexOf(',') - 1);
			tokenList.add(token);
			tokenStr = tokenReader.readLine();
		}
		tokenReader.close();
		tokenList.add("#");
		return tokenList;
	}
	
	//求一个字符的first集，递归调用
	public static HashSet<String> first(String curStr) {
		HashSet<String> firstSet = new HashSet<String>();
		//终结符处理
		if(Terminators.contains(curStr)) {
			firstSet.add(curStr);
		}else{
			//遍历每一个产生式
			for(String str : Productions.get(curStr)) {
				String[] rightStrs = str.split(" ");
				int length = rightStrs.length;
				//有空产生式，first集中加入空(@)
				if(str.equals("@")) {
					firstSet.add(str);
				}else if(Terminators.contains(rightStrs[0])) {//产生式右边第一个为终结符
					firstSet.add(rightStrs[0]);
				}else {//产生式右边为语法变量
					int i = 0;
					for(i = 0; i < length; i++) {
						//如果和自己相同，则跳过，避免死递归
						if(!rightStrs[i].equals(curStr)) {
							//获得右边字符的first集，合并过来
							HashSet<String> nextSet = first(rightStrs[i]);
							//右边字符的first集是否有空，有则继续下一个字符，没有则跳出
							if(nextSet.contains("@")) {
								nextSet.remove("@");
								firstSet.addAll(nextSet);
							}else {
								firstSet.addAll(nextSet);
								break;
							}
						}else {
							break;
						}
					}
					//判断退出循环的情况，如果是遍历所有字符退出，则加入空
					if(i == length) {
						firstSet.add("@");
					}
				}
			}
//			if(firstSet.contains("@")) {
//				for(String str : Productions.get(curStr)) {
//					String[] rightStrs = str.split(" ");
//					int length = rightStrs.length;
//					//有空产生式，first集中加入空(@)
//					if(!str.equals("@") && Terminators.contains(rightStrs[0])) {
//						int i = 0;
//						for(i = 0; i < length; i++) {
//							if(rightStrs[i].equals(curStr)) {
//								break;
//							}
//						}
//						for(i = i + 1; i < length; i++) {
//							HashSet<String> nextSet = first(rightStrs[i]);
//							firstSet.addAll(nextSet);
//							//右边字符的first集是否有空，有则继续下一个字符，没有则跳出
//							if(!nextSet.contains("@")) {
//								break;
//							}
//						}
//					}
//				}
//			}
		}
		return firstSet;
	}
	
	//求一个串的first集
	public static HashSet<String> firstForStr(List<String> beta, String endStr) {
		HashSet<String> firstSet = new HashSet<String>();
		int i = 0;
		HashSet<String> nextSet = null;
		//对串里的字符进行遍历求first集，直至全部求出，或有一个字符的first集中不含空
		for(i = 0; i< beta.size(); i++) {
			nextSet = first(beta.get(i));
			if(nextSet.contains("@")) {
				nextSet.remove("@");
				firstSet.addAll(nextSet);
			}else {
				firstSet.addAll(nextSet);
				break;
			}
		}
		//判断退出循环条件，如果是遍历退出，则加入endStr，此时表明beta集可推出空
		if(i == beta.size()) {
			firstSet.add(endStr);
		}
		return firstSet;
	}
	
	//求集合闭包，使用TreeSet作为闭包避免放入重复元素
	public static TreeSet<Item> closure(LinkedList<Item> itemList) {
		TreeSet<Item> itemClosure = new TreeSet<Item>();
		int size = itemList.size();
		int i = 0;
		for(i = 0; i < size; i++) {
			Item curItem = itemList.get(i);
			itemClosure.add(curItem);
			int position = curItem.position;
			if(position + 1 == curItem.state.size()) {
				continue;
			}else if(SyntacticVariables.contains(curItem.state.get(position + 1))) {
				String nextVariable = curItem.state.get(position + 1);
				List<String> beta = curItem.state.subList(position + 2, curItem.state.size());
				String endStr  = curItem.expecSymbol;
				HashSet<String> firstSet = firstForStr(beta, endStr);
				HashSet<String> production = Productions.get(nextVariable);
				for(String curProduction : production) {
					for(String expecSysmbol : firstSet) {
						ArrayList<String> newState = new ArrayList<String>();
						newState.add(".");
						for(String rightStr : curProduction.split(" ")) {
							newState.add(rightStr);
						}
						Item newItem = new Item(nextVariable, newState, expecSysmbol);
						if(!itemList.contains(newItem)) {
							itemList.add(newItem);
							size++;
						}
					}
				}
			}
		}
		return itemClosure;
	}

	public static TreeSet<Item> go(TreeSet<Item> itemSet, String nextStr) {
		TreeSet<Item> itemClosure = new TreeSet<Item>();
		LinkedList<Item> resultList = new LinkedList<Item>();
		for(Item curItem : itemSet) {
			int position = curItem.position;
			if(position + 1 == curItem.state.size()) {
				continue;
			}else if(curItem.state.get(position + 1).equals(nextStr)) {
				ArrayList<String> newState = new ArrayList<String>();
				for(int j = 0; j < curItem.state.size(); j++) {
					if(curItem.state.get(j).equals(".")) {
						newState.add(nextStr);
						newState.add(".");
						j++;
					}else {
						newState.add(curItem.state.get(j));
					}
				}
				Item newItem = new Item(curItem.variable, newState, curItem.expecSymbol);
				resultList.add(newItem);
			}
		}
		itemClosure = closure(resultList);
		return itemClosure;
	}

	public static LinkedList<TreeSet<Item>> itemSetCollection() {
		LinkedList<TreeSet<Item>> resultList = new LinkedList<TreeSet<Item>>();
		String startLeft = startProduction.substring(0, startProduction.indexOf('-'));
		String startRight = startProduction.substring(startProduction.indexOf('>') + 1, startProduction.length());
		ArrayList<String> newState = new ArrayList<String>();
		newState.add(".");
		newState.add(startRight);
		Item newItem = new Item(startLeft, newState, "#");
		LinkedList<Item> startState = new LinkedList<Item>();
		TreeSet<Item> startClosure = new TreeSet<Item>();
		startState.add(newItem);
		startClosure = closure(startState);
		resultList.add(startClosure);
		int size = resultList.size();
		for(int i = 0; i < size; i++) {
			HashMap<String, Integer> newMap = new HashMap<String, Integer>();
			goNextTable.add(newMap);
			for(String variable : SyntacticVariables) {
				TreeSet<Item> newSet = go(resultList.get(i), variable);
				if(newSet.size() != 0 && !resultList.contains(newSet)) {
					resultList.add(newSet);
					size++;
				}
				if(resultList.contains(newSet)) {
					goNextTable.get(i).put(variable, resultList.indexOf(newSet));	
				}
			}
			for(String terminator : Terminators) {
				TreeSet<Item> newSet = go(resultList.get(i), terminator);
				if(newSet.size() != 0 && !resultList.contains(newSet)) {
					resultList.add(newSet);
					size++;
				}
				if(resultList.contains(newSet)) {
					goNextTable.get(i).put(terminator, resultList.indexOf(newSet));
				}
			}
		}
		return resultList;
	}
	
	public static void generateAnalysisTable() throws IOException {
		resultCollection = itemSetCollection();
		for(int i = 0; i < resultCollection.size(); i++) {
			HashMap<String, String> newActionMap = new HashMap<String, String>();
			actionTable.add(newActionMap);
			HashMap<String, Integer> newGotoMap = new HashMap<String, Integer>();
			gotoTable.add(newGotoMap);
		}
		int n = 0;
		for(int i = 0; i < resultCollection.size(); i++) {
			TreeSet<Item> curItemSet = resultCollection.get(i);
			for(Item curItem : curItemSet) {
				int position = curItem.position;
				if(position + 1 == curItem.state.size()) {
					String startLeft = startProduction.substring(0, startProduction.indexOf('-'));
					if(curItem.variable.equals(startLeft)) {
						actionTable.get(i).put("#", "acc");
					}else {
						String production = curItem.variable;
						production = production + " ->";
						for(int k = 0; k < position; k++) {
							production = production + " " + curItem.state.get(k);
						}
						if(actionTable.get(i).containsKey(curItem.expecSymbol) && !actionTable.get(i).get(curItem.expecSymbol).equals(production)) {
							System.out.println(i + curItem.expecSymbol);
							n++;
						}else {
							actionTable.get(i).put(curItem.expecSymbol, production);
						}
					}
				}else if(Terminators.contains(curItem.state.get(position + 1))) {
					String terminator = curItem.state.get(position + 1);
					int nextState = goNextTable.get(i).get(terminator);
					if(actionTable.get(i).containsKey(terminator) && !actionTable.get(i).get(terminator).equals(String.valueOf(nextState))) {
						System.out.println(i + terminator);
						n++;
					}else {
						actionTable.get(i).put(terminator, String.valueOf(nextState));
					}
				}else if(SyntacticVariables.contains(curItem.state.get(position + 1))) {
					String variable = curItem.state.get(position + 1);
					int nextState = goNextTable.get(i).get(variable);
					if(gotoTable.get(i).containsKey(variable) && !gotoTable.get(i).get(variable).equals(nextState)) {
						System.out.println(i + variable);
						n++;
					}else {
						gotoTable.get(i).put(variable, nextState);
					}
				}else if(curItem.state.get(position + 1).equals("@")) {
					String production = curItem.variable;
					production = production + " -> @";
					if(actionTable.get(i).containsKey(curItem.expecSymbol)&&!actionTable.get(i).get(curItem.expecSymbol).equals(production)) {
						System.out.println(i + curItem.expecSymbol);
						n++;
					}else {
						actionTable.get(i).put(curItem.expecSymbol, production);
					}
				}
			}
		}
		System.out.println("Collision:" + n);
	}

	public static void analysis(ArrayList<String> tokenList) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter("ProductOrder.txt"));
		Stack<String> strStack = new Stack<String>();
		Stack<Integer> stateStack = new Stack<Integer>();
		strStack.push("#");
		stateStack.push(0);
		int i = 0;
		int newID = -1;
		while(i < tokenList.size()) {
			String curStr = tokenList.get(i);
//			if(newID != i && (curStr.equals("ID") || curStr.indexOf("CONST") != -1)) {
//				semAnalyzer.addNewIDorConst();
//				newID = i;
//			}
			int statePop = stateStack.peek();
			String action = actionTable.get(statePop).get(curStr);
			if(action == null) {
				System.err.println("error : " + curStr);
				out.write("error : " + curStr + "\r\n");
				i++;
				continue;
			}else if(action.equals("acc")) {
				out.close();
				return;
			}else if(action.indexOf("->") != -1) {
				String[] productionRights = action.substring(action.indexOf(">") + 2, action.length()).split(" ");
				if(!productionRights[0].equals("@")) {
					for(int j = 0; j < productionRights.length; j++) {
						strStack.pop();
						stateStack.pop();
					}
				}
				System.out.println(curStr + "  :  " + action);
				statePop = stateStack.peek();
				curStr = action.substring(0, action.indexOf(' '));
				strStack.push(curStr);
				stateStack.push(gotoTable.get(statePop).get(curStr));
				semAnalyzer.semanticAnalysis(action);
				out.write(action + "\r\n");
			}else {
				out.write("读入" + curStr + "转移到" + Integer.valueOf(action) + "\r\n");
				stateStack.push(Integer.valueOf(action));
				strStack.push(curStr);
				if((curStr.equals("ID") || curStr.indexOf("CONST") != -1)) {
					semAnalyzer.addNewIDorConst();
					//newID = i;
				}
				i++;
			}
		}
		out.close();
	}
	
	public static void outputTables() {
		System.out.println("ActionTable:");
		for(int i = 0; i < actionTable.size(); i++) {
			HashMap<String, String> tmpMap = actionTable.get(i);
			for(String tmpStr : tmpMap.keySet()) {
				System.out.println("(" + i + ", " + tmpStr + ") :    " + tmpMap.get(tmpStr));
			}
		}
		System.out.println("\nGotoTable:");
		for(int i = 0; i < gotoTable.size(); i++) {
			HashMap<String, Integer> tmpMap = gotoTable.get(i);
			for(String tmpStr : tmpMap.keySet()) {
				System.out.println("(" + i + ", " + tmpStr + ") :	" + tmpMap.get(tmpStr));
			}
		}
	}

	public static void outputState() throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter("State.txt"));
		for(int j = 0; j < goNextTable.size(); j++) {
			TreeSet<Item> curSet = resultCollection.get(j);
			out.write("State" + j + ":" + curSet.size() + " \r\n");
			for(Item curItem : curSet) {
				String item = "" + curItem.variable + "->";
				for(String str : curItem.state) {
					item = item + str;
				}
				item = item + "-----" + curItem.expecSymbol;
				out.write(item + "\r\n");
			}
			HashMap<String, Integer> map = goNextTable.get(j);
			Set<String> set = map.keySet();
			for(String str : set) {
				int next = map.get(str);
				TreeSet<Item> nextSet = resultCollection.get(next);
				out.write("读取" + str + "到State" + next + ":" + nextSet.size() + "\r\n");
				for(Item curItem : nextSet) {
					String item = "" + curItem.variable + "->";
					for(String s : curItem.state) {
						item = item + s;
					}
					item = item + "-----" + curItem.expecSymbol;
					out.write(item + "\r\n");
				}
			}
		}
//		for(int i = 0; i < resultCollection.size(); i++) {
//			TreeSet<Item> curSet = resultCollection.get(i);
//			out.write("State" + i + ":" + curSet.size() + " \r\n");
//			for(Item curItem : curSet) {
//				String item = "" + curItem.variable + "->";
//				for(String str : curItem.state) {
//					item = item + str;
//				}
//				item = item + "-----" + curItem.expecSymbol;
//				out.write(item + "\r\n");
//			}
//		}
		out.close();
	}
	
	public static void outputFirst() throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter("First.txt"));
	    for(String str : SyntacticVariables) {
	    	HashSet<String> a = first(str);
	    	out.write(str + ": {");
	    	for(String s : a) {
	    		out.write(s + "    ");
	    	}
	    	out.write(" }\r\n");
	    }
	    out.close();
	}
	
	public static void judgeItemSetCollection(LinkedList<TreeSet<Item>> itemSetCollection) {
		int repeat = 0;
		int flag = 0;
		for(int i = 0; i < itemSetCollection.size(); i++) {
			TreeSet<Item> aList = itemSetCollection.get(i);
			for(int j = 0; j < itemSetCollection.size(); j++) {
				if(i == j) {
					continue;
				}
				TreeSet<Item> bList = itemSetCollection.get(j);
				if(aList.size() <= bList.size()) {
					for(Item tmp : aList) {
						if(!bList.contains(tmp)) {
							flag = 1;
						}
					}
					if(flag == 0) {
						repeat++;
						System.out.println(itemSetCollection.indexOf(aList) + "  :  " + itemSetCollection.indexOf(bList));
					}
					flag = 0;
				}
			}
		}
		System.out.println(repeat);
	}
	
	public static void countItem(LinkedList<TreeSet<Item>> itemSetCollection) {
		int count = 0;
		for(TreeSet<Item> itemSet : itemSetCollection) {
			count = count + itemSet.size();
		}
		System.out.println(count);
	}
}
