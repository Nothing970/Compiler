package com.compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class SyntacticAnalyzer {
	private static HashSet<String> SyntacticVariables = new HashSet<String>();
	private static HashSet<String> Terminators = new HashSet<String>();
	private static HashMap<String, HashSet<String>> Productions = new HashMap<String, HashSet<String>>();
	private static String startProduction = null;
	
	private static LinkedList<HashMap<String, Integer>> goNextTable = new LinkedList<HashMap<String, Integer>>();
	
	private static LinkedList<HashMap<String, String>> actionTable = new LinkedList<HashMap<String, String>>();
	private static LinkedList<HashMap<String, Integer>> gotoTable = new LinkedList<HashMap<String, Integer>>();
	
 	public static void mainControl(String productionPath) throws IOException {
        initialize(productionPath);
        generateAnalysisTable();
        //outputTables();
        ArrayList<String> tokenList = generateTokenList("TokenTable.txt");
//        for(String str : tokenList) {
//        	System.out.println(str);
//        }
        //analysis(tokenList);
	}
	
	public static void initialize(String productionPath) throws IOException {
		BufferedReader productionReader = new BufferedReader(new FileReader(productionPath));
		String syntacticVariableStr = productionReader.readLine();
		for(String str : syntacticVariableStr.split(" ")) {
			SyntacticVariables.add(str);
		}
		String terminatorStr = productionReader.readLine();
		for(String str : terminatorStr.split(" ")) {
			Terminators.add(str);
		}
		String production = productionReader.readLine();
		production = productionReader.readLine();
		startProduction = production;
		while(production != null) {
			String productionLeft = production.substring(0, production.indexOf('-'));
			String productionRight = production.substring(production.indexOf('>') + 1, production.length());
			if(Productions.get(productionLeft) == null) {
				HashSet<String> newSet = new HashSet<String>();
				newSet.add(productionRight);
				Productions.put(productionLeft, newSet);
			}else {
				Productions.get(productionLeft).add(productionRight);
			}
			production = productionReader.readLine();
		}
	}

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
	
	public static HashSet<String> first(String curStr) {
//		try {
			HashSet<String> firstSet = new HashSet<String>();
			if(Terminators.contains(curStr)) {
				firstSet.add(curStr);
			}else{
				for(String str : Productions.get(curStr)) {
					String[] rightStrs = str.split(" ");
					int length = rightStrs.length;
					if(str.equals("@")) {
						firstSet.add(str);
					}else if(Terminators.contains(rightStrs[0])) {
						firstSet.add(rightStrs[0]);
					}else {
						int i = 0;
						for(i = 0; i < length; i++) {
							if(!rightStrs[i].equals(curStr)) {
								HashSet<String> nextSet = first(rightStrs[i]);
								if(nextSet.contains("@")) {
									nextSet.remove("@");
									firstSet.addAll(nextSet);
								}else {
									firstSet.addAll(nextSet);
									break;
								}
							}
						}
						if(i == length) {
							firstSet.add("@");
						}
					}
				}
			}
			return firstSet;
//		}catch(NullPointerException e) {
//			System.out.println("异常：" + curStr);
//		}
//		return SyntacticVariables;
		
	}
	
	public static HashSet<String> firstForStr(List<String> beta, String endStr) {
		HashSet<String> firstSet = new HashSet<String>();
		int i = 0;
		HashSet<String> nextSet = null;
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
		if(i == beta.size()) {
			firstSet.add(endStr);
		}
		return firstSet;
	}
	
	public static void closure(LinkedList<Item> itemList) {
		int size = itemList.size();
		int i = 0;
		for(i = 0; i < size; i++) {
			Item curItem = itemList.get(i);
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
	}

	public static LinkedList<Item> go(LinkedList<Item> itemList, String nextStr) {
		LinkedList<Item> resultList = new LinkedList<Item>();
		int i = 0;
		int size = itemList.size();
		for(i = 0; i < size; i++) {
			Item curItem = itemList.get(i);
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
		closure(resultList);
		return resultList;
	}

	public static LinkedList<LinkedList<Item>> itemSetCollection() {
		LinkedList<LinkedList<Item>> resultList = new LinkedList<LinkedList<Item>>();
		String startLeft = startProduction.substring(0, startProduction.indexOf('-'));
		String startRight = startProduction.substring(startProduction.indexOf('>') + 1, startProduction.length());
		ArrayList<String> newState = new ArrayList<String>();
		newState.add(".");
		newState.add(startRight);
		Item newItem = new Item(startLeft, newState, "#");
		LinkedList<Item> startState = new LinkedList<Item>();
		startState.add(newItem);
		closure(startState);
		resultList.add(startState);
		int size = resultList.size();
		for(int i = 0; i < size; i++) {
			HashMap<String, Integer> newMap = new HashMap<String, Integer>();
			goNextTable.add(newMap);
			for(String variable : SyntacticVariables) {
				LinkedList<Item> newList = go(resultList.get(i), variable);
				if(newList.size() != 0 && !resultList.contains(newList)) {
					resultList.add(newList);
					size++;
				}
				goNextTable.get(i).put(variable, resultList.indexOf(newList));
			}
			for(String terminator : Terminators) {
				LinkedList<Item> newList = go(resultList.get(i), terminator);
				if(newList.size() != 0 && !resultList.contains(newList)) {
					resultList.add(newList);
					size++;
				}
				goNextTable.get(i).put(terminator, resultList.indexOf(newList));
			}
		}
		return resultList;
	}

	public static void generateAnalysisTable() {
		LinkedList<LinkedList<Item>> resultCollection = itemSetCollection();
		judgeItemSetCollection(resultCollection);
		for(int i = 0; i < resultCollection.size(); i++) {
			HashMap<String, String> newActionMap = new HashMap<String, String>();
			actionTable.add(newActionMap);
			HashMap<String, Integer> newGotoMap = new HashMap<String, Integer>();
			gotoTable.add(newGotoMap);
		}
		for(int i = 0; i < resultCollection.size(); i++) {
			LinkedList<Item> curItemList = resultCollection.get(i);
			for(int j =0 ; j < curItemList.size(); j++) {
				Item curItem = curItemList.get(j);
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
						actionTable.get(i).put(curItem.expecSymbol, production);
					}
				}else if(Terminators.contains(curItem.state.get(position + 1))) {
					String terminator = curItem.state.get(position + 1);
					int nextState = goNextTable.get(i).get(terminator);
					actionTable.get(i).put(terminator, String.valueOf(nextState));
				}else if(SyntacticVariables.contains(curItem.state.get(position + 1))) {
					String variable = curItem.state.get(position + 1);
					int nextState = goNextTable.get(i).get(variable);
					gotoTable.get(i).put(variable, nextState);
				}else if(curItem.state.get(position + 1).equals("@")) {
					String production = curItem.variable;
					production = production + " -> @";
					actionTable.get(i).put(curItem.expecSymbol, production);
				}
			}
		}
	}

	public static void analysis(ArrayList<String> tokenList) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter("ProductOrder.txt"));
		Stack<String> strStack = new Stack<String>();
		Stack<Integer> stateStack = new Stack<Integer>();
		strStack.push("#");
		stateStack.push(0);
		int i = 0;
		while(i < tokenList.size()) {
			String curStr = tokenList.get(i);
			int statePop = stateStack.peek();
			String action = actionTable.get(statePop).get(curStr);
			if(action == null) {
				System.out.println("error : " + curStr);
				return;
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
				out.write(action + "\r\n");
			}else {
				stateStack.push(Integer.valueOf(action));
				strStack.push(curStr);
				i++;
			}
		}
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

	public static void judgeItemSetCollection(LinkedList<LinkedList<Item>> itemSetCollection) {
		int flag = 0;
		for(int i = 0; i < itemSetCollection.size(); i++) {
			LinkedList<Item> aList = itemSetCollection.get(i);
			for(int j = i + 1; j < itemSetCollection.size(); j++) {
				LinkedList<Item> bList = itemSetCollection.get(j);
				if(aList.size() == bList.size()) {
					for(Item tmp : aList) {
						if(!bList.contains(tmp)) {
							flag = 1;
						}
					}
					if(flag == 0) {
						System.out.println(itemSetCollection.indexOf(aList) + "  :  " + itemSetCollection.indexOf(bList));
					}
					flag = 0;
				}
			}
		}
	}
	
}
