package com.compiler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class SemanticAnalyzer {
	public Stack<SignTable> signTableStack;//符号表栈
	public SignTable curTable;//当前使用的符号表
	public Stack<String> typeStack;//数据类型的栈
 	public Stack<Expr> exprStack;//规约出的表达式的栈
	public int exprNum;//表达式标号
	public Stack<Integer> IDStack;//ID和常量的栈
	public int IDorConstNum;//ID常量的标号
	public ArrayList<String> data;
	public int dataNum;
	public ArrayList<String> instructions;//存放所有指令的列表
	public ArrayList<String> curIns;//存放当前生成指令的列表
	public int nextQuad;//下一条指令的序号
	public Stack<ArrayList<Integer>> trueList;
	public Stack<ArrayList<Integer>> falseList;
	public Stack<ArrayList<Integer>> nextList;
	public Stack<String> backLabels;
	public Stack<Integer> backQuads;
	public int labelNum;
	public String curCondOp;
	public Stack<ArrayList<Integer>> breakList;
	public Stack<ArrayList<Integer>> continueList;
	public int callParas;
	public String callID;
	public SignTable callTable;
	
	//初始化上述变量
 	public SemanticAnalyzer() {
		signTableStack = new Stack<SignTable>();
		curTable = null;
		typeStack = new Stack<String>();
		exprStack = new Stack<Expr>();
		IDStack = new Stack<Integer>();
		exprNum = 1;
		IDorConstNum = -1;
		data = new ArrayList<String>();
		data.add(".section .data\r\n");
		dataNum = 0;
		instructions = new ArrayList<String>();
		instructions.add(".section .text\r\n");
		curIns = new ArrayList<String>();
		nextQuad = 0;
		trueList = new Stack<ArrayList<Integer>>();
		falseList = new Stack<ArrayList<Integer>>();
		nextList = new Stack<ArrayList<Integer>>();
		backLabels = new Stack<String>();
		backQuads = new Stack<Integer>();
		labelNum = 0;
		curCondOp = null;
		breakList = new Stack<ArrayList<Integer>>();
		continueList = new Stack<ArrayList<Integer>>();
		callParas = 0;
		callID = null;
		callTable = null;
	}
	
	//读入新的ID或者常量，将对应标号压栈，并且标号加1
	public void addNewIDorConst() {
		IDorConstNum++;
		IDStack.push(IDorConstNum);
	}
	
	//分析各规约表达式应该生成的指令
	public void semanticAnalysis(String production) {
		switch(production) {
		//程序开始，生成全局符号表，压栈
		case("M0 -> @"):
			if(signTableStack.size() == 0) {
				curTable = new SignTable();
				curTable.tableName = "global";
				curTable.offset = 0;
				signTableStack.push(curTable);
			}
			break;
		//此处为main函数，生成main的符号表，生成main函数的初始指令，生成main函数当中的指令列表
		case("M1 -> @"):
			if(true) {
				curTable = new SignTable();
				curTable.tableName = "main";
				curTable.parTable = signTableStack.peek();
				curTable.offset = 0;
				curTable.paras = 0;
				signTableStack.push(curTable);
				String tmpIns = ".globl _main";
				instructions.add(tmpIns);
				tmpIns = "_main:";
				instructions.add(tmpIns);
				tmpIns = "\tpushl %ebp";
				instructions.add(tmpIns);
				tmpIns = "\tmovl %esp, %ebp";
				instructions.add(tmpIns);
				curIns = new ArrayList<String>();
				nextQuad = 0;
				curTable.retType = "int";
			}
			break;
		//此处为其他函数，生成对应的符号表，生成函数的初始指令，生成当前函数当中的指令列表
		case("M2 -> @"):
			if(true) {
				curTable = new SignTable();
				curTable.tableName = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				curTable.parTable = signTableStack.peek();
				curTable.offset = 0;
				curTable.paras = 0;
				signTableStack.push(curTable);
				String tmpIns = ".globl _" + curTable.tableName;
				instructions.add(tmpIns);
				tmpIns = "_" + curTable.tableName + ":";
				instructions.add(tmpIns);
				tmpIns = "\tpushl %ebp";
				instructions.add(tmpIns);
				tmpIns = "\tmovl %esp, %ebp";
				instructions.add(tmpIns);
				curIns = new ArrayList<String>();
				nextQuad = 0;
				curTable.retType = typeStack.peek();
			}
			break;
		//将当前的规约出的类型入栈
		case("Type -> INT"):
			typeStack.push("int");
			break;
		case("Type -> FLOAT"):
			typeStack.push("float");
			break;
		case("Type -> CHAR"):
			typeStack.push("char");
			break;
		case("MainDef -> INT MAIN M1 ( ParaList ) { Sen M3 RETURN Expr ; }"):
			if(exprStack.peek().type.equals("INT")) {//!
				System.err.println("返回类型错误！");
			}else {
				Expr tmpExpr  = exprStack.pop();
				if(tmpExpr.type.equals(curTable.retType)) {
					SignTable tmpTable = curTable;
					curIns.add("\tmovl " + (SignTable.getOffset(tmpTable, tmpExpr.addr) + 1) + "%(esp), %eax");
					nextQuad++;
				}else {
					curIns.add("\tmovl $" + tmpExpr.addr + ", %eax");
					nextQuad++;
				}
				curIns.add("\tleave");
				nextQuad++;
				curIns.add("\tret");
				nextQuad++;
				backpatch(nextList.pop(), backLabels.pop(), backQuads.pop());
				SignTable tmpTable = signTableStack.pop();
				curTable = signTableStack.peek();
				int tmpOffset = tmpTable.offset;
				Sign tmpSign = new Sign();
				tmpSign.idName = tmpTable.tableName;
				tmpSign.type = "table";
				tmpSign.offset = curTable.offset;
				tmpSign.tablePtr = tmpTable;
				curTable.addNewSign(tmpSign);
				curTable.offset = curTable.offset + tmpOffset;
				String tmpIns = "\tsubl $" + tmpOffset + ", %esp ";
				instructions.add(tmpIns);
				instructions.addAll(curIns);
			}
			break;
		case("FuncDef -> Type ID M2 ( ParaList ) { Sen M3 RETURN Expr ; }"):
			if(exprStack.peek().type.equals(typeStack.peek())) {//!
				System.err.println("返回类型错误！");
			}else {
				Expr tmpExpr  = exprStack.pop();
				if(tmpExpr.type.equals(curTable.retType)) {
					SignTable tmpTable = curTable;
					curIns.add("\tmovl " + (SignTable.getOffset(tmpTable, tmpExpr.addr) + 1) + "%(esp), %eax");
					nextQuad++;
				}else {
					curIns.add("\tmovl $" + tmpExpr.addr + ", %eax");
					nextQuad++;
				}
				curIns.add("\tleave");
				nextQuad++;
				curIns.add("\tret");
				nextQuad++;
				backpatch(nextList.pop(), backLabels.pop(), backQuads.pop());
				typeStack.pop();
				SignTable tmpTable = signTableStack.pop();
				curTable = signTableStack.peek();
				int tmpOffset = tmpTable.offset;
				Sign tmpSign = new Sign();
				tmpSign.idName = tmpTable.tableName;
				tmpSign.type = "table";
				tmpSign.offset = curTable.offset;
				tmpSign.tablePtr = tmpTable;
				curTable.addNewSign(tmpSign);
				curTable.offset = curTable.offset + tmpOffset;
				String tmpIns = "\tsubl $" + tmpOffset + ", %esp ";
				instructions.add(tmpIns);
				instructions.addAll(curIns);
			}
			break;
		case("ParaList -> Type ID"):
		case("ParaList1 -> Type ID"):
		case("ParaList -> Type ID , ParaList1"):
		case("ParaList1 -> Type ID , ParaList1"):
			if(true) {
				String tmpType = typeStack.pop();
				String tmpID = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				SignTable tmpTable = curTable;
				int isExist = SignTable.getOffset(tmpTable, tmpID);
				if(isExist == -1) {
					Sign tmpSign = new Sign();
					tmpSign.idName = tmpID;
					tmpSign.type = tmpType;
					tmpSign.offset = curTable.offset;
					curTable.addNewSign(tmpSign);
					curTable.offset = curTable.offset + 4;
					curTable.paras++;
				}else {
					System.err.println(tmpID + "重声明!");
				}
			}
			break;
		case("Id1 -> ID"):
			if(true) {
				String tmpID = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				SignTable tmpTable = curTable;
				int isExist = SignTable.getOffset(tmpTable, tmpID);
				if(isExist == -1) {
					Sign tmpSign = new Sign();
					tmpSign.idName = tmpID;
					tmpSign.type = typeStack.peek();
					tmpSign.offset = curTable.offset;
					curTable.addNewSign(tmpSign);
					curTable.offset = curTable.offset + 4;
				}else {
					System.err.println(tmpID + "重声明!");
				}
			}
			break;
		case("Id2 -> ID [ INTCONST ]"):
			if(true) {
				int tmpDimens = Integer.valueOf(LexicalAnalyzer.mySignTable.get(IDStack.pop()));
				String tmpID = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				SignTable tmpTable = curTable;
				int isExist = SignTable.getOffset(tmpTable, tmpID);
				if(isExist == -1) {
					Sign tmpSign = new Sign();
					tmpSign.idName = tmpID;
					tmpSign.type = typeStack.peek() + "_array";
					tmpSign.dimens = tmpDimens;
					tmpSign.offset = curTable.offset;
					curTable.addNewSign(tmpSign);
					curTable.offset = curTable.offset + 4 * tmpDimens;
				}else {
					System.err.println(tmpID + "重声明!");
				}
			}
			break;
		case("DeclareS -> Type Idlist ;"):
			if(true) {
				typeStack.pop();
				ArrayList<Integer> nList = new ArrayList<Integer>();
				nextList.push(nList);
			}
			break;
		case("Expr2 -> ID"):
			if(true) {
				SignTable tmpTable = curTable;
				String tmpID = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				while(tmpTable != null) {
					if(tmpTable.indexOf(tmpID) != -1) {
						break;
					}else {
						tmpTable = tmpTable.parTable;
					}
				}
				if(tmpTable == null) {
					System.err.println("变量未声明！");
				}else {
					Expr tmpExpr = new Expr();
					tmpExpr.addr = tmpID;
					tmpExpr.type = tmpTable.getSign(tmpID).type;
					exprStack.push(tmpExpr);
				}
			}
			break;
		case("Expr2 -> INTCONST"):
			if(true) {
				String tmpConst = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				Expr tmpExpr = new Expr();
				tmpExpr.addr = tmpConst;
				tmpExpr.type = "intconst";
				exprStack.push(tmpExpr);
			}
			break;
		case("Expr2 -> STRINGCONST"):
			if(true) {
				String tmpConst = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				Expr tmpExpr = new Expr();
				tmpExpr.addr = tmpConst;
				tmpExpr.type = "stringconst";
				exprStack.push(tmpExpr);
			}
			break;
		case("Expr2 -> REALCONST"):
			if(true) {
				String tmpConst = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				Expr tmpExpr = new Expr();
				tmpExpr.addr = tmpConst;
				tmpExpr.type = "floatconst";
				exprStack.push(tmpExpr);
			}
			break;
		case("Expr2 -> CHARCONST"):
			if(true) {
				String tmpConst = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				Expr tmpExpr = new Expr();
				tmpExpr.addr = tmpConst;
				tmpExpr.type = "charconst";
				exprStack.push(tmpExpr);
			}
			break;
		case("Expr2 -> ID [ Expr ]"):
			if(true) {
				
			}
			break;
		case("Expr2 -> ID C1 ( CallParaList )"):
			if(true) {
				curIns.add("\tcall " + callID);
				nextQuad++;
				Expr tmpExpr = new Expr();
				tmpExpr.addr = "t" + String.valueOf(exprNum);
				exprNum++;
				tmpExpr.type = callTable.retType;
				exprStack.push(tmpExpr);
				Sign tmpSign = new Sign();
				tmpSign.idName = tmpExpr.addr;
				tmpSign.type = tmpExpr.type;
				tmpSign.offset = curTable.offset;
				curTable.addNewSign(tmpSign);
				curTable.offset = curTable.offset + 4;
				curIns.add("movl %eax, " + (tmpSign.offset + 1) + "%(esp)"); 
				for(int i = 0; i <= callParas - 1; i++) {
					curIns.add("\tpopl %eax");
					nextQuad++;
				}
			}
			break;
		case("Expr -> Expr + Expr1"):
			if(true) {
				Expr tmpExpr1 = exprStack.pop();
				Expr tmpExpr2 = exprStack.pop();
				Expr tmpExpr3 = new Expr();
				if(tmpExpr1.type.indexOf("int") != -1 && tmpExpr2.type.indexOf("int") != -1) {
					tmpExpr3.addr = "t" + String.valueOf(exprNum);
					exprNum++;
					tmpExpr3.type = "int";
					exprStack.push(tmpExpr3);
					Sign tmpSign = new Sign();
					tmpSign.idName = tmpExpr3.addr;
					tmpSign.type = tmpExpr3.type;
					tmpSign.offset = curTable.offset;
					curTable.addNewSign(tmpSign);
					curTable.offset = curTable.offset + 4;
//					curIns.add("\tpushl %eax");
//					nextQuad++;
//					curIns.add("\tpushl %edx");
//					nextQuad++;
					String tmpIns = null;
					if(tmpExpr1.type.equals("intconst")) {
						tmpIns= "\tmovl $" + tmpExpr1.addr + ", %eax";
					}else {
						SignTable tmpTable = curTable;
						tmpIns = "\tmovl " + (SignTable.getOffset(tmpTable, tmpExpr1.addr) + 1) + "(%esp)" + ", %eax";
					}
					curIns.add(tmpIns);
					nextQuad++;
					if(tmpExpr2.type.equals("intconst")) {
						tmpIns= "\tmovl $" + tmpExpr2.addr + ", %edx";
					}else {
						SignTable tmpTable = curTable;
						tmpIns = "\tmovl " + (SignTable.getOffset(tmpTable, tmpExpr2.addr) + 1) + "(%esp)" + ", %edx";
					}
					curIns.add(tmpIns);
					nextQuad++;
					curIns.add("\tleal (%eax,%edx), %eax");
					nextQuad++;
					SignTable tmpTable = curTable;
					curIns.add("\tmovl %eax, " + (SignTable.getOffset(tmpTable, tmpExpr3.addr) + 1) + "(%esp)");
					nextQuad++;
//					curIns.add("\tpopl %eax");
//					nextQuad++;
//					curIns.add("\tpopl %edx");
//					nextQuad++;
				}
			}
			break;
		case("Expr -> Expr - Expr1"):
			if(true) {
				Expr tmpExpr1 = exprStack.pop();
				Expr tmpExpr2 = exprStack.pop();
				Expr tmpExpr3 = new Expr();
				if(tmpExpr1.type.indexOf("int") != -1 && tmpExpr2.type.indexOf("int") != -1) {
					tmpExpr3.addr = "t" + String.valueOf(exprNum);
					exprNum++;
					tmpExpr3.type = "int";
					exprStack.push(tmpExpr3);
					Sign tmpSign = new Sign();
					tmpSign.idName = tmpExpr3.addr;
					tmpSign.type = tmpExpr3.type;
					tmpSign.offset = curTable.offset;
					curTable.addNewSign(tmpSign);
					curTable.offset = curTable.offset + 4;
					String tmpIns = null;
					if(tmpExpr1.type.equals("intconst")) {
						tmpIns= "\tmovl $" + tmpExpr1.addr + ", %eax";
					}else {
						SignTable tmpTable = curTable;
						tmpIns = "\tmovl " + (SignTable.getOffset(tmpTable, tmpExpr1.addr) + 1) + "(%esp)" + ", %eax";
					}
					curIns.add(tmpIns);
					nextQuad++;
					if(tmpExpr2.type.equals("intconst")) {
						tmpIns= "\tmovl $" + tmpExpr2.addr + ", %edx";
					}else {
						SignTable tmpTable = curTable;
						tmpIns = "\tmovl " + (SignTable.getOffset(tmpTable, tmpExpr2.addr) + 1) + "(%esp)" + ", %edx";
					}
					curIns.add(tmpIns);
					nextQuad++;
					curIns.add("\tsubl (%eax,%edx), %eax");
					nextQuad++;
					SignTable tmpTable = curTable;
					curIns.add("\tmovl %eax, " + (SignTable.getOffset(tmpTable, tmpExpr3.addr) + 1) + "(%esp)");
					nextQuad++;
				}
			}
			break;
		case("Expr1 -> Expr1 * Expr2"):
			break;
		case("Expr1 -> Expr1 / Expr2"):
			if(true) {
				Expr tmpExpr1 = exprStack.pop();
				Expr tmpExpr2 = exprStack.pop();
				Expr tmpExpr3 = new Expr();
				tmpExpr3.addr = "t" + String.valueOf(exprNum);
				exprNum++;
				tmpExpr3.type = "int";
				exprStack.push(tmpExpr3);
				Sign tmpSign = new Sign();
				tmpSign.idName = tmpExpr3.addr;
				tmpSign.type = tmpExpr3.type;
				tmpSign.offset = curTable.offset;
				curTable.addNewSign(tmpSign);
				curTable.offset = curTable.offset + 4;
				String tmpIns = "push eax";
				curIns.add(tmpIns);
				nextQuad++;
				if(tmpExpr1.type.indexOf("const") == -1) {
					SignTable tmpTable = curTable;
					int offset = SignTable.getOffset(tmpTable, tmpExpr1.addr);
					if(offset != -1) {
						if(!tmpTable.tableName.equals("global")) {
							tmpIns = "mov eax [ebp+" + offset + "]";
							curIns.add(tmpIns);
							nextQuad++;
						}else {
							
						}
					}else {
						System.err.println("变量未声明！");
					}
				}else {
					tmpIns = "mov eax " + tmpExpr1.addr;
					curIns.add(tmpIns);
					nextQuad++;
				}
				if(tmpExpr2.type.indexOf("const") == -1) {
					SignTable tmpTable = curTable;
					int offset = SignTable.getOffset(tmpTable, tmpExpr2.addr);
					if(offset != -1) {
						if(!tmpTable.tableName.equals("global")) {
							tmpIns = "div [ebp+" + offset + "]";
							curIns.add(tmpIns);
							nextQuad++;
						}else {
							
						}
					}else {
						System.err.println("变量未声明！");
					}
				}else {
					tmpIns = "div " + tmpExpr2.addr;
					curIns.add(tmpIns);
					nextQuad++;
				}
				tmpIns = "mov [ebp+" + tmpSign.offset + "] eax";
				curIns.add(tmpIns);
				nextQuad++;
				tmpIns = "pop eax";
				curIns.add(tmpIns);
				nextQuad++;
			}
			break;
		case("AssignS -> ID = Expr ;"):
			if(true) {
				ArrayList<Integer> nList = new ArrayList<Integer>();
				nextList.push(nList);
				String tmpID = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				Expr tmpExpr1 = exprStack.pop();
				SignTable tmpTable = curTable;
				if(tmpExpr1.type.indexOf("const") == -1) {
					String tmpIns = "\tmovl " + (SignTable.getOffset(tmpTable, tmpExpr1.addr) + 1) + "(%esp)";
					tmpTable = curTable;
					tmpIns = tmpIns + ", " + (SignTable.getOffset(tmpTable, tmpID) + 1) + "(%esp)";
					curIns.add(tmpIns);
					nextQuad++;
				}else {
					String tmpIns = "\tmovl $" + tmpExpr1.addr;
					tmpTable = curTable;
					tmpIns = tmpIns + ", " + (SignTable.getOffset(tmpTable, tmpID) + 1) + "(%esp)";
					curIns.add(tmpIns);
					nextQuad++;
				}
				
			}
			break;
		case("BoolE -> BoolE || M3 BoolE1"):
			if(true) {
				ArrayList<Integer> list = falseList.pop();
				backpatch(falseList.pop(), backLabels.pop(), backQuads.pop());
				falseList.add(list);
				ArrayList<Integer> list2 = trueList.pop();
				ArrayList<Integer> list1 = trueList.pop();
				merge(list1,list2);
				trueList.push(list2);
			}
			break;
		case("BoolE1 -> BoolE1 && M3 BoolE2"):
			if(true) {
				ArrayList<Integer> list = trueList.pop();
				backpatch(trueList.pop(), backLabels.pop(), backQuads.pop());
				trueList.add(list);
				ArrayList<Integer> list2 = falseList.pop();
				ArrayList<Integer> list1 = falseList.pop();
				merge(list1,list2);
				falseList.push(list2);
			}
			break;
		case("M3 -> @"):
			if(true) {
				backQuads.push(nextQuad);
				backLabels.push("L" + labelNum);
				labelNum++;
			}
			break;
		case("BoolE2 -> ! BoolE3"):
			if(true) {
				ArrayList<Integer> list1 = falseList.pop();
				ArrayList<Integer> list2 = trueList.pop();
				trueList.push(list1);
				falseList.push(list2);
			}
			break;
		case("BoolE3 -> Expr Condop Expr"):
			if(true) {
				ArrayList<Integer> tList = new ArrayList<Integer>();
				ArrayList<Integer> fList = new ArrayList<Integer>();
				tList.add(nextQuad);
				fList.add(nextQuad + 1);
				trueList.push(tList);
				falseList.push(fList);
				curIns.add("\tif true goto");
				nextQuad++;
				curIns.add("\tgoto");
				nextQuad++;
			}
			break;
		case("Condop -> >"):
			curCondOp = ">";
			break;
		case("Condop -> >="):
			curCondOp = ">=";
			break;
		case("Condop -> <"):
			curCondOp = "<";
			break;
		case("Condop -> <="):
			curCondOp = "<=";
			break;
		case("Condop -> =="):
			curCondOp = "==";
			break;
		case("Condop -> !="):
			curCondOp = "!=";
			break;
		case("IfS -> IF ( BoolE ) M3 { IfSen }"):
		case("CirIfS -> IF ( BoolE ) M3 { CirSen }"):
			if(true) {
				backpatch(trueList.pop(), backLabels.pop(), backQuads.pop());
				ArrayList<Integer> list1 = falseList.pop();
				ArrayList<Integer> list2 = nextList.pop();
				merge(list1, list2);
				nextList.push(list2);
			}
			break;
		case("IfS -> IF ( BoolE ) M3 { IfSen } N1 ELSE M3 { IfSen }"):
		case("IfS -> IF ( BoolE ) M3 { IfSen } N1 ELSE M3 IfS"):
		case("CirIfS -> IF ( BoolE ) M3 { CirSen } N1 ELSE M3 { CirSen }"):
		case("CirIfS -> IF ( BoolE ) M3 { CirSen } N1 ELSE M3 CirIfS"):
			if(true) {
				backpatch(falseList.pop(), backLabels.pop(), backQuads.pop());
				backpatch(trueList.pop(), backLabels.pop(), backQuads.pop());
				ArrayList<Integer> list2 = nextList.pop();
				ArrayList<Integer> list1 = nextList.pop();
				merge(list1, list2);
				list1 = nextList.pop();
				merge(list1, list2);
				nextList.push(list2);
			}
			break;
		case("N1 -> @"):
			if(true) {
				ArrayList<Integer> nList = new ArrayList<Integer>();
				nList.add(nextQuad);
				nextList.push(nList);
				curIns.add("\tjmp");
				nextQuad++;
			}
			break;
		case("WhileS -> WHILE M4 ( BoolE ) M3 { CirSen }"):
			if(true) {
				backpatch(trueList.pop(), backLabels.pop(), backQuads.pop());
				String tmpLabel = backLabels.pop();
				backpatch(nextList.pop(), tmpLabel, backQuads.pop());
				curIns.add("\tjmp " + tmpLabel);
				nextQuad++;
				ArrayList<Integer> bList = breakList.pop();
				merge(falseList.pop(), bList);
				nextList.push(bList);
				ArrayList<Integer> cList = continueList.pop();
				for(int i = 0; i<cList.size(); i++) {
					int tmpQuad = cList.get(i);
					String tmpIns = curIns.get(tmpQuad);
					tmpIns = tmpIns + " " + tmpLabel;
					curIns.set(tmpQuad, tmpIns);
				}
			}
			break;
		case("M4 -> @"):
			if(true) {
				backQuads.push(nextQuad);
				backLabels.push("L" + labelNum);
				labelNum++;
				ArrayList<Integer> bList = new ArrayList<Integer>();
				ArrayList<Integer> cList = new ArrayList<Integer>();
				breakList.push(bList);
				continueList.push(cList);
			}
			break;
		case("IfSen -> Sen M3 RETURN Expr ;"):
			if(true) {
				Expr tmpExpr = exprStack.pop();
				if(tmpExpr.type.indexOf(curTable.retType) != -1) {
					if(tmpExpr.type.equals(curTable.retType)) {
						SignTable tmpTable = curTable;
						curIns.add("movl " + (SignTable.getOffset(tmpTable, tmpExpr.addr) + 1) + "%(esp), %eax");
						nextQuad++;
					}else {
						curIns.add("movl $" + tmpExpr.addr + ", %eax");
						nextQuad++;
					}
					curIns.add("leave");
					nextQuad++;
					curIns.add("ret");
					nextQuad++;
					backpatch(nextList.pop(), backLabels.pop(), backQuads.pop());
				}else {
					System.err.println(curTable.tableName + "返回参数类型不匹配!");
				}
			}
			break;
		case("Sen -> DeclareS Sen"):
		case("Sen -> AssignS Sen"):
		case("Sen -> CallS Sen"):
		case("CirSen -> DeclareS CirSen"):
		case("CirSen -> AssignS CirSen"):
		case("CirSen -> CallS CirSen"):
			if(true) {
				ArrayList<Integer> nList = nextList.pop();
				nextList.pop();
				nextList.push(nList);
			}
			break;
		case("Sen -> IfS M3 Sen"):
		case("Sen -> WhileS M3 Sen"):
		case("CirSen -> CirIfS M3 CirSen"):
		case("CirSen -> WhileS M3 CirSen"):
			if(true) {
				ArrayList<Integer> nList = nextList.pop();
				backpatch(nextList.pop(), backLabels.pop(), backQuads.pop());
				nextList.push(nList);
			}
			break;
		case("CirSen -> @"):
		case("Sen -> @"):
			if(true) {
				ArrayList<Integer> nList = new ArrayList<Integer>();
				nList.add(nextQuad);
				nextList.push(nList);
				curIns.add("\tjmp");
				nextQuad++;
			}
			break;
		case("CirSen -> BREAK ;"):
			if(true) {
				breakList.peek().add(nextQuad);
				curIns.add("\tjmp");
				nextQuad++;
				ArrayList<Integer> nList = new ArrayList<Integer>();
				nextList.push(nList);
			}
			break;
		case("CirSen -> CONTINUE ;"):
			if(true) {
				continueList.peek().add(nextQuad);
				curIns.add("\tjmp");
				nextQuad++;
				ArrayList<Integer> nList = new ArrayList<Integer>();
				nextList.push(nList);
			}
			break;
		case("C1 -> @"):
			if(true) {
				callID = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				callParas = 0;
				if(!callID.equals("printf") && !callID.equals("scanf")) {
					callTable = curTable.parTable.getSign(callID).tablePtr;
				}
			}
			break;
		case("CallParaList -> Expr"):
		case("CallParaList1 -> Expr"):
		case("CallParaList1 -> Expr , CallParaList1"):
		case("CallParaList -> Expr , CallParaList1"):
			if(true) {
				if(callID.equals("printf") || callID.equals("scanf")) {
					Expr tmpExpr = exprStack.pop();
					if(tmpExpr.type.equals("stringconst")) {
						String tmpStr = tmpExpr.addr.substring(0, tmpExpr.addr.length() - 1) + "\\0\"";
						data.add("LC" + dataNum + ": .ascii " +  tmpStr );
						curIns.add("\tpushl $LC" + dataNum);
						nextQuad++;
						dataNum++;
						callParas++;
					}else if(tmpExpr.type.equals("int")) {
						SignTable tmpTable = curTable;
						int offset = SignTable.getOffset(tmpTable, tmpExpr.addr);
						if(tmpTable.tableName.equals("global")) {
							
						}else {
							curIns.add("\tmovl " + (offset  + 1) + "(%esp), %eax");
							nextQuad++;
							curIns.add("\tpushl %eax");
							nextQuad++;
							callParas++;
						}
					}
				}else {
					if(callParas >= callTable.paras) {
						System.err.println(callID + "参数数量不匹配");
					}else if(!exprStack.peek().type.equals(callTable.signList.get(callParas).type)) {
						System.err.println(callID + "参数类型不匹配");
					}else {
						Expr tmpExpr = exprStack.pop();
						SignTable tmpTable = curTable;
						String tmpIns = "\tpushl " + (SignTable.getOffset(tmpTable, tmpExpr.addr) + 1) + "(%esp)";
						curIns.add(tmpIns);
						nextQuad++;
						callParas++;
					}
				}
			}
			break;
		case("CallS -> ID C1 ( CallParaList ) ;"):
			if(true) {
				ArrayList<Integer> nList = new ArrayList<Integer>();
				nextList.push(nList);
				if(callID.equals("printf") || callID.equals("scanf")) {
					curIns.add("\tcall _" + callID);
					nextQuad++;
				}else {
					curIns.add("\tcall " + callID);
					nextQuad++;
				}
				for(int i = 0; i <= callParas - 1; i++) {
					curIns.add("\tpopl %eax");
					nextQuad++;
				}
			}
			break;
		}
	}

	public void backpatch(ArrayList<Integer> list, String label, int quad) {
		String tmpIns = curIns.get(quad);
		tmpIns = label + ": " + tmpIns;
		curIns.set(quad, tmpIns);
		for(int i = 0;i<list.size();i++) {
			int tmpQuad = list.get(i);
			tmpIns = curIns.get(tmpQuad);
			tmpIns = tmpIns + " " + label;
			curIns.set(tmpQuad, tmpIns);
		}
	}
	
	public void merge(ArrayList<Integer> list1, ArrayList<Integer> list2) {
		for(int i = 0; i < list1.size(); i++) {
			list2.add(list1.get(i));
		}
	}
	
	public void outputIns(String fileName) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
		for(String tmpIns : data ){
	         out.write(tmpIns + "\r\n");
	    }
		for(String tmpIns : instructions ){
	         out.write(tmpIns + "\r\n");
	    }
		out.close();
	}
}
