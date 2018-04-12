package com.compiler;

import java.io.IOException;

public class Compiler {

	public static void main(String[] args) throws IOException {
		LexicalAnalyzer.mainContrl("src.c");
		SyntacticAnalyzer.mainControl("production.txt");
	}
}
