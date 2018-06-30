package com.lightning.firewood.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class Logger extends OutputStream {
	private Integer subTasks = 0; // class version to preserve cross-object when copied later. ;)
	private static boolean logGoing = false;
	private static boolean errGoing = false;
	private long threadID; // doesn't need to be class because it's constant cross-thread anyway
	private static Long curThread = -1L; // class to synchronize off of
	
	private static HashMap<Long, Logger> loggers = new HashMap<Long, Logger>();
	
	public Logger() {
		long id = Thread.currentThread().getId();
		if(loggers.containsKey(id)) {
			Logger curLogger = loggers.get(id);
			subTasks = curLogger.subTasks;
		}
		threadID = id;
	}
	
	public static Logger getLogger() {
		long id = Thread.currentThread().getId();
		if(loggers.containsKey(id)) {
			return loggers.get(id);
		} else {
			return new Logger();
		}
	}
	
	public void print(String text) {
		synchronized(curThread) {
			if(text.indexOf("\n") != -1) { print(text.substring(0,text.indexOf("\n"))); println(); print(text.substring(text.indexOf("\n")+1)); return; }
			if(errGoing) { System.err.println(); errGoing = false; }
			if(logGoing && curThread != threadID) { System.out.println(); logGoing = false; }
			if(!logGoing && text.length() != 0) {
				curThread = threadID;
				System.out.print("(" + Thread.currentThread().getName() + ")\tLOG:\t");
				logGoing = true;
				for(int i = 0; i < subTasks; i++)
					System.out.print('\t');
			}
			System.out.print(text);
			try {
				Thread.sleep(10); // Temporary to sync Eclipse.
			} catch(InterruptedException e) {}
		}
	}
	
	public void println() {
		System.out.println();
		logGoing = false;
	}
	
	public void println(String text) {
		print(text+'\n');
	}
	
	public void printErr(String text) {
		synchronized(curThread) {
			if(text.indexOf("\n") != -1) { printErr(text.substring(0,text.indexOf("\n"))); printErrln(); printErr(text.substring(text.indexOf("\n")+1)); return; }
			if(logGoing) { System.out.println(); logGoing = false; }
			if(errGoing && threadID != curThread) { System.err.println(); errGoing = false; }
			if(!errGoing && text.length() != 0) {
				System.err.print("(" + Thread.currentThread().getName() + ")\tERR:\t");
				errGoing = true;
				for(int i = 0; i < subTasks; i++)
					System.err.print('\t');
			}
			System.err.print(text);
			try {
				Thread.sleep(10); // Temporary to sync Eclipse.
			} catch(InterruptedException e) {}
		}
	}
	
	public void printErrln() {
		System.err.println();
		errGoing = false;
	}
	
	public void printErrln(String text) {
		printErr(text+'\n');
	}
	
	public void subTask() {
		subTasks++;
	}
	
	public void returned() {
		subTasks--;
	}
	
	public void write(int arg0) throws IOException {
		printErr(String.valueOf((char)arg0));
	}
}
