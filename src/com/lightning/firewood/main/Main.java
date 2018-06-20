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
import java.nio.IntBuffer;
import java.util.Set;

import org.lwjgl.BufferUtils;
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
		
		window = glfwCreateWindow(720, 480, annotation.name(), NULL, NULL);
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
		
		glFrontFace(GL_CW);
		glCullFace(GL_BACK);
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);

		glEnable(GL_TEXTURE_2D);
		
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
			if(GameState.isPaused()) {
				// TODO: Apply post-shader for half-bright
			}
			
			if(GameState.isMainGame()) {
				glDisable(GL_CULL_FACE); // Disabling culling suggested by Kristof09
				// Check dimensions of window
				IntBuffer w = BufferUtils.createIntBuffer(1);
				IntBuffer h = BufferUtils.createIntBuffer(1);
				glfwGetWindowSize(window, w, h);
				int width = w.get(0);
				int height = h.get(0);
				double mainAspect = ((double)border.getTextureWidth(0)+border.getTextureWidth(2)+border.getTextureWidth(3))/(border.getTextureHeight(2)+border.getTextureHeight(0)+border.getTextureHeight(1));
				glViewport(0,0,width,height);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				if((float)width/height >= mainAspect) {
					// Side expansions
					int expandWidth = (int)Math.ceil((width-mainAspect*height)/2);
					glViewport(0, 0, expandWidth, height);
					border.bindTexture(10);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(-height*(float)border.getTextureWidth(10)/border.getTextureHeight(10),0);
						glVertex4f(-1,-1,0,1);
						glTexCoord2f(0,0);
						glVertex4f( 1,-1,0,1);
						glTexCoord2f(0,1);
						glVertex4f( 1, 1,0,1);
						glTexCoord2f(-height*(float)border.getTextureWidth(10)/border.getTextureHeight(10),1);
						glVertex4f(-1, 1,0,1);
					}
					glEnd();
					glViewport(width-expandWidth, 0, expandWidth, height);
					border.bindTexture(11);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-1,-1,0,1);
						glTexCoord2f(height*(float)border.getTextureWidth(11)/border.getTextureHeight(11),0);
						glVertex4f( 1,-1,0,1);
						glTexCoord2f(height*(float)border.getTextureWidth(11)/border.getTextureHeight(11),1);
						glVertex4f( 1, 1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-1, 1,0,1);
					}
					glEnd();
					
					float sideWidth = ((float)border.getTextureWidth(2)/((float)border.getTextureWidth(0)+border.getTextureWidth(2)+border.getTextureWidth(3)));
					float tbHeight = ((float)border.getTextureHeight(0)/((float)border.getTextureHeight(0)+border.getTextureHeight(1)+border.getTextureHeight(2)));
					glViewport(expandWidth, 0, width - expandWidth*2, height); // Single viewport suggested by Snek on Discord.
					border.bindTexture(2);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-1,-tbHeight*2+1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(sideWidth*2-1,-tbHeight*2+1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(sideWidth*2-1,tbHeight*2-1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-1,tbHeight*2-1,0,1);
					}
					glEnd();
					border.bindTexture(3);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-sideWidth*2+1,-tbHeight*2+1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(1,-tbHeight*2+1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(1,tbHeight*2-1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-sideWidth*2+1,tbHeight*2-1,0,1);
					}
					glEnd();
					border.bindTexture(0);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(sideWidth*2-1,1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(-sideWidth*2+1,1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(-sideWidth*2+1,-tbHeight*2+1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(sideWidth*2-1,-tbHeight*2+1,0,1);
					}
					glEnd();
					border.bindTexture(1);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(sideWidth*2-1,tbHeight*2-1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(-sideWidth*2+1,tbHeight*2-1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(-sideWidth*2+1,-1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(sideWidth*2-1,-1,0,1);
					}
					glEnd();
					border.bindTexture(4);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-1,1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(sideWidth*2-1,1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(sideWidth*2-1,-tbHeight*2+1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-1,-tbHeight*2+1,0,1);
					}
					glEnd();
					border.bindTexture(5);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-sideWidth*2+1,1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(1,1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(1,-tbHeight*2+1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-sideWidth*2+1,-tbHeight*2+1,0,1);
					}
					glEnd();
					border.bindTexture(6);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-1,tbHeight*2-1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(sideWidth*2-1,tbHeight*2-1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(sideWidth*2-1,-1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-1,-1,0,1);
					}
					glEnd();
					border.bindTexture(7);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-sideWidth*2+1,tbHeight*2-1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(1,tbHeight*2-1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(1,-1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-sideWidth*2+1,-1,0,1);
					}
					glEnd();
					glViewport((int)Math.floor(expandWidth+sideWidth), (int)Math.floor(tbHeight), (int)Math.ceil(width-2*(expandWidth+sideWidth)), (int)Math.ceil(height-2*tbHeight));
				} else {
					// Top/bottom expansions
					int expandHeight = (int)Math.ceil((height-width/mainAspect)/2);
					glViewport(0, 0, width, expandHeight);
					border.bindTexture(8);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,-width/(float)border.getTextureWidth(10)*border.getTextureHeight(10));
						glVertex4f(-1,-1,0,1);
						glTexCoord2f(1,-width/(float)border.getTextureWidth(10)*border.getTextureHeight(10));
						glVertex4f( 1,-1,0,1);
						glTexCoord2f(1,1);
						glVertex4f( 1, 1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-1, 1,0,1);
					}
					glEnd();
					glViewport(0, height-expandHeight, width, expandHeight);
					border.bindTexture(9);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-1,-1,0,1);
						glTexCoord2f(1,0);
						glVertex4f( 1,-1,0,1);
						glTexCoord2f(1,width/(float)border.getTextureWidth(11)*border.getTextureHeight(11));
						glVertex4f( 1, 1,0,1);
						glTexCoord2f(0,width/(float)border.getTextureWidth(11)*border.getTextureHeight(11));
						glVertex4f(-1, 1,0,1);
					}
					glEnd();
					
					float sideWidth = ((float)border.getTextureWidth(2)/((float)border.getTextureWidth(0)+border.getTextureWidth(2)+border.getTextureWidth(3)));
					float tbHeight = ((float)border.getTextureHeight(0)/((float)border.getTextureHeight(0)+border.getTextureHeight(1)+border.getTextureHeight(2)));
					glViewport(0, expandHeight, width, height - expandHeight*2); // Single viewport suggested by Snek on Discord.
					border.bindTexture(2);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-1,-tbHeight*2+1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(sideWidth*2-1,-tbHeight*2+1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(sideWidth*2-1,tbHeight*2-1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-1,tbHeight*2-1,0,1);
					}
					glEnd();
					border.bindTexture(3);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-sideWidth*2+1,-tbHeight*2+1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(1,-tbHeight*2+1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(1,tbHeight*2-1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-sideWidth*2+1,tbHeight*2-1,0,1);
					}
					glEnd();
					border.bindTexture(0);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(sideWidth*2-1,1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(-sideWidth*2+1,1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(-sideWidth*2+1,-tbHeight*2+1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(sideWidth*2-1,-tbHeight*2+1,0,1);
					}
					glEnd();
					border.bindTexture(1);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(sideWidth*2-1,tbHeight*2-1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(-sideWidth*2+1,tbHeight*2-1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(-sideWidth*2+1,-1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(sideWidth*2-1,-1,0,1);
					}
					glEnd();
					border.bindTexture(4);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-1,1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(sideWidth*2-1,1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(sideWidth*2-1,-tbHeight*2+1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-1,-tbHeight*2+1,0,1);
					}
					glEnd();
					border.bindTexture(5);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-sideWidth*2+1,1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(1,1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(1,-tbHeight*2+1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-sideWidth*2+1,-tbHeight*2+1,0,1);
					}
					glEnd();
					border.bindTexture(6);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-1,tbHeight*2-1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(sideWidth*2-1,tbHeight*2-1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(sideWidth*2-1,-1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-1,-1,0,1);
					}
					glEnd();
					border.bindTexture(7);
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(-sideWidth*2+1,tbHeight*2-1,0,1);
						glTexCoord2f(1,0);
						glVertex4f(1,tbHeight*2-1,0,1);
						glTexCoord2f(1,1);
						glVertex4f(1,-1,0,1);
						glTexCoord2f(0,1);
						glVertex4f(-sideWidth*2+1,-1,0,1);
					}
					glEnd();
					glViewport((int)Math.floor(sideWidth), (int)Math.floor(expandHeight+tbHeight), (int)Math.ceil(width-2*sideWidth), (int)Math.ceil(height-2*(expandHeight+tbHeight)));
				}
				glEnable(GL_CULL_FACE);
			}
			
			if(GameState.isPaused()) {
				// TODO: Remove half-bright post-shader
			}
			
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
