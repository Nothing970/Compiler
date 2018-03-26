package com.compile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Compiler {

	public static void main(String[] args) throws IOException {
		LexicalAnalyzer.mainContrl("src.txt");
	}
}
