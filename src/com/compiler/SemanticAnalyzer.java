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
	public ArrayList<String> instructions;//存放所有指令的列表
	public ArrayList<String> curIns;//存放当前生成指令的列表
	public int nextQuad;//下一条指令的序号
	
	//初始化上述变量
	public SemanticAnalyzer() {
		signTableStack = new Stack<SignTable>();
		curTable = null;
		typeStack = new Stack<String>();
		exprStack = new Stack<Expr>();
		IDStack = new Stack<Integer>();
		exprNum = 1;
		IDorConstNum = -1;
		instructions = new ArrayList<String>();
		instructions.add(".section .text\r\n");
		curIns = new ArrayList<String>();
		nextQuad = 0;
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
		//
		case("M1 -> @"):
			if(true) {
				curTable = new SignTable();
				curTable.tableName = "main";
				curTable.parTable = signTableStack.peek();
				curTable.offset = 0;
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
			}
			break;
		case("M2 -> @"):
			if(true) {
				curTable = new SignTable();
				curTable.tableName = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				curTable.parTable = signTableStack.peek();
				curTable.offset = 0;
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
			}
			break;
		case("Type -> INT"):
			typeStack.push("int");
			break;
		case("Type -> FLOAT"):
			typeStack.push("float");
			break;
		case("Type -> STRING"):
			typeStack.push("string");
			break;
		case("Type -> CHAR"):
			typeStack.push("char");
			break;
		case("MainDef -> INT MAIN M1 ( ParaList ) { Sen RETURN Expr ; }"):
			if(false) {//!exprStack.peek().type.equals("INT")
				System.err.println("返回类型错误！");
			}else {
				//exprStack.pop();
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
				String tmpIns = "\tsub %esp " + tmpOffset;
				instructions.add(tmpIns);
				instructions.addAll(curIns);
				tmpIns = "\tleave";
				instructions.add(tmpIns);
				tmpIns = "\tret\r\n";
				instructions.add(tmpIns);
			}
			break;
		case("FuncDef -> Type ID M2 ( ParaList ) { Sen RETURN Expr ; }"):
			if(false) {//!exprStack.peek().type.equals(typeStack.peek())
				System.err.println("返回类型错误！");
			}else {
				//exprStack.pop();
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
				String tmpIns = "\tleave";
				instructions.add(tmpIns);
				tmpIns = "\tret\r\n";
				instructions.add(tmpIns);
			}
			break;
		case("ParaList -> Type ID"):
		case("ParaList1 -> Type ID"):
		case("ParaList -> Type ID , ParaList1"):
		case("ParaList1 -> Type ID , ParaList1"):
			if(true) {
				String tmpType = typeStack.pop();
				String tmpID = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				Sign tmpSign = new Sign();
				tmpSign.idName = tmpID;
				tmpSign.type = tmpType;
				tmpSign.offset = curTable.offset;
				curTable.addNewSign(tmpSign);
				curTable.offset = curTable.offset + 4;
			}
			break;
		case("Id1 -> ID"):
			if(true) {
				String tmpID = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				Sign tmpSign = new Sign();
				tmpSign.idName = tmpID;
				tmpSign.type = typeStack.peek();
				tmpSign.offset = curTable.offset;
				curTable.addNewSign(tmpSign);
				curTable.offset = curTable.offset + 4;
			}
			break;
		case("Id2 -> ID [ INTCONST ]"):
			if(true) {
				int tmpDimens = Integer.valueOf(LexicalAnalyzer.mySignTable.get(IDStack.pop()));
				String tmpID = LexicalAnalyzer.mySignTable.get(IDStack.pop());
				Sign tmpSign = new Sign();
				tmpSign.idName = tmpID;
				tmpSign.type = typeStack.peek() + "_array";
				tmpSign.dimens = tmpDimens;
				tmpSign.offset = curTable.offset;
				curTable.addNewSign(tmpSign);
				curTable.offset = curTable.offset + 4 * tmpDimens;
			}
			break;
		case("DeclareS -> Type Idlist ;"):
			typeStack.pop();
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
		}
	}

	public void outputIns(String fileName) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
		for(String tmpIns : instructions ){
	         out.write(tmpIns + "\r\n");
	    }
		out.close();
	}
}
