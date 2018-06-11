/**
 * The main class.
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
package com.lightning.firewood.main;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.Set;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.reflections.*;

import com.lightning.firewood.display.Border;
import com.lightning.firewood.identification.*;
import com.lightning.firewood.util.Logger;
import com.lightning.firewood.util.Util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * @author Ray Redondo
 *
 */
public class Main {
	private static Border border;
	private static long window;
	
	/**
	 * The main function of the engine. Finds an annotated game and runs it.
	 * If no external games are found, defaults to <code>com.lightning.firewood.example.ExampleGame</code>.
	 * 
	 * @param args The command line arguments. Not used at the moment.
	 */
	public static void main(String[] args) {
		Logger.println(".########.####.########..########.##......##..#######...#######..########."); 
		Logger.println(".##........##..##.....##.##.......##..##..##.##.....##.##.....##.##.....##"); 
		Logger.println(".##........##..##.....##.##.......##..##..##.##.....##.##.....##.##.....##"); 
		Logger.println(".######....##..########..######...##..##..##.##.....##.##.....##.##.....##"); 
		Logger.println(".##........##..##...##...##.......##..##..##.##.....##.##.....##.##.....##"); 
		Logger.println(".##........##..##....##..##.......##..##..##.##.....##.##.....##.##.....##"); 
		Logger.println(".##.......####.##.....##.########..###..###...#######...#######..########.");
		Logger.println("by Lightning Creations");
		Logger.println();
		Logger.println("Starting...");
		Logger.println();
		Logger.println("Searching for games...");
		Reflections r = new Reflections("");
		Set<Class<?>> gamesSet = r.getTypesAnnotatedWith(FirewoodGame.class);
		Class<?>[] games = new Class<?>[gamesSet.size()];
		int index = 0;
		int exGameIndex = 0;
		
		Logger.println();
		Logger.println("Found:");
		
		for(Class<?> game : gamesSet) {
			games[index] = game;
			Logger.println("\t* "+game.getName() + ": " + game.getName());
			if(game.getName().equals("Example Game"))
				exGameIndex = index;
			index++;
		}
		Logger.println();
		
		if(games.length == 1) {
			Logger.println("Only detected ExampleGame... Oh well.\n");
		}
		
		Class<?> gameClass = null;
		gameClass = games[exGameIndex];
		
		for(int i = 0; i < games.length; i++) {
			if((FirewoodParent.class.isAssignableFrom(games[i]) && !games[i].getName().equals("Example Game"))) {
				gameClass = games[i];
				break;
			}
		}
		
		FirewoodGame annotation = gameClass.getAnnotation(FirewoodGame.class);
		
		Logger.println("Starting " + annotation.name() + (annotation.description().isEmpty() ? ", which has no description provided." : (": " + annotation.description())));
		
		GLFWErrorCallback.createPrint(new PrintStream(new Logger())).set();
		
		if(!glfwInit()) {
			Logger.printErr("FATAL ERROR! Could not initialize GLFW!");
			Logger.printErr("I mean, it is kind of hard to play a game without having a window...");
			System.exit(1);
		}
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		
		window = glfwCreateWindow(640, 480, annotation.name(), NULL, NULL);
		if(window == NULL) {
			Logger.printErr("FATAL ERROR! Could not initialize GLFW!");
			Logger.printErr("I mean, it is kind of hard to play a game without having a window...");
			System.exit(1);
		}
		
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
		
		GL.createCapabilities();
		glClearColor(0,0,0,1);
		
		FirewoodParent game = null;
		
		try {
			game = (FirewoodParent) gameClass.newInstance();
		} catch(Error | Exception e) {
			Logger.printErr("FATAL ERROR! ");
			e.printStackTrace();
			System.exit(1);
		}
		
		if(border == null) {
			Logger.printErrln("ERROR! Game didn't finish configuration!");
			Logger.printErrln("This isn't fatal; attempting to find correct config files...");
			
			File root = new File("assets");
			
			String[] directories = root.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return new File(dir, name).isDirectory();
				}
			});
			
			for(int i = 0; i < directories.length; i++) {
				Logger.println("Found directory: " + directories[i]);
			}
			
			String bestDirectory = directories[Util.findClosestString(directories, gameClass.getTypeName())];
			
			Logger.println("Trying to find assets in /assets/" + bestDirectory + "...");
			
			if(border == null) { // Need border
				Logger.println("Creating border...");
				Logger.subTask();
				border = new Border(bestDirectory);
				Logger.returned();
				Logger.println("Found border, lucky... do it yourself next time, developer.");
			}
		}
		
		while(!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			// Game code here
			glfwSwapBuffers(window);
			glfwPollEvents();
		}
		
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	public static void setBorder(Border newBorder) {
		border = newBorder;
	}
}
