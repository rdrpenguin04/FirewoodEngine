package com.lightning.firewood.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Logger extends OutputStream {
	private static int subTasks = 0;
	private static boolean logGoing = false;
	private static boolean errGoing = false;
	
	public static void print(String text) {
		if(text.indexOf("\n") != -1) { print(text.substring(0,text.indexOf("\n"))); println(); print(text.substring(text.indexOf("\n")+1)); return; }
		if(errGoing) { System.err.println(); errGoing = false; }
		if(!logGoing && text.length() != 0) {
			System.out.print("LOG:\t");
			logGoing = true;
			for(int i = 0; i < subTasks; i++)
				System.out.print('\t');
		}
		System.out.print(text);
	}
	
	public static void println() {
		System.out.println();
		logGoing = false;
	}
	
	public static void println(String text) {
		print(text+'\n');
	}
	
	public static void printErr(String text) {
		if(text.indexOf("\n") != -1) { printErr(text.substring(0,text.indexOf("\n"))); printErrln(); printErr(text.substring(text.indexOf("\n")+1)); return; }
		if(logGoing) { System.out.println(); logGoing = false; }
		if(!errGoing && text.length() != 0) {
			System.err.print("ERR:\t");
			errGoing = true;
			for(int i = 0; i < subTasks; i++)
				System.err.print('\t');
		}
		System.err.print(text);
	}
	
	public static void printErrln() {
		System.err.println();
		errGoing = false;
	}
	
	public static void printErrln(String text) {
		printErr(text+'\n');
	}
	
	public static void subTask() {
		subTasks++;
	}
	
	public static void returned() {
		subTasks--;
	}
	
	public void write(int arg0) throws IOException {
		printErr(String.valueOf((char)arg0));
	}
}
