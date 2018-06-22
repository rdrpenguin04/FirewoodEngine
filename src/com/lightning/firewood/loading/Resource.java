/**
 * Asynchronous loading object
 * 
 * Copyright (C) 2018 Lightning Creations
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lightning.firewood.loading;

import java.io.File;
import java.lang.reflect.ParameterizedType;

/**
 * @author Ray Redondo
 *
 */
public class Resource<T extends ResourceType> implements Runnable {
	public File file;
	public Thread loaderThread;
	public volatile boolean finished;
	public volatile boolean error;
	public volatile Throwable errorClass;
	public volatile T result;
	public Runnable postLoad;
	
	public Resource(File file, Runnable postLoad) {
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
		postLoad.run();
	}
}
