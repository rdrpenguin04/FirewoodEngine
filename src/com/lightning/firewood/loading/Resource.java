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

import com.lightning.firewood.util.Invokable;

/**
 * @author Ray Redondo
 *
 */
public class Resource implements Runnable {
	//private Thread loaderThread;
	private Class<? extends ResourceType> type;

	public File file;
	public boolean finished;
	public boolean error;
	public Throwable errorClass;
	public ResourceType result;
	public Invokable postLoad;
	
	public Resource(File file, Class<? extends ResourceType> type, Invokable postLoad) {
		this.file = file;
		this.type = type;
		this.postLoad = postLoad;
//		loaderThread = new Thread(this);
//		loaderThread.start();
		run(); // Because OpenGL hates multithreading
	}
	
	public void run() {
		try {
			result = type.newInstance();
			result.load(file);
			finished = true;
		} catch(Exception | Error e) {
			errorClass = e;
			error = true;
			finished = true;
		}
		postLoad.invoke(this);
	}
}
