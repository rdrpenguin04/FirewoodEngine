package com.lightning.firewood.loading;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.ParameterizedType;

public class Resource<T extends ResourceType> implements Runnable {
	public File file;
	public Thread loaderThread;
	public volatile boolean finished;
	public volatile boolean error;
	public volatile Throwable errorClass;
	public volatile T result;
	
	public Resource(File file) {
		this.file = file;
		loaderThread = new Thread(this);
		loaderThread.start();
	}
	
	@SuppressWarnings("unchecked")
	public void run() {
		try {
			// From Stack Overflow
			// Get the class name of this instance's type.
			result = (T) ((Class<? extends T>)((ParameterizedType)this.getClass().
				       getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
			result.load(file);
			finished = true;
		} catch(Exception | Error e) {
			errorClass = e;
			error = true;
			finished = true;
		}
	}
}
