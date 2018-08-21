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
import java.util.ArrayList;
import java.util.Set;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.reflections.*;

import com.lightning.firewood.display.Border;
import com.lightning.firewood.identification.*;
import com.lightning.firewood.loading.Resource;
import com.lightning.firewood.menu.Menu;
import com.lightning.firewood.menu.MenuNode;
import com.lightning.firewood.rendering.Font;
import com.lightning.firewood.rendering.Std3DShader;
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
	private static Font font;
	private static long window;
	public static ArrayList<Resource> resources = new ArrayList<>();
	public static ArrayList<String> resNames = new ArrayList<>();
	private static ArrayList<Resource> toLoad = new ArrayList<>();
	private static ArrayList<String> toLoadNames = new ArrayList<>();
	private static Menu curMenu;
	
	/**
	 * The main function of the engine. Finds an annotated game and runs it.
	 * If no external games are found, defaults to <code>com.lightning.firewood.example.ExampleGame</code>.
	 * 
	 * @param args The command line arguments. Not used at the moment.
	 */
	public static void main(String[] args) {
		Logger logger = Logger.getLogger();
		logger.println(".########.####.########..########.##......##..#######...#######..########."); 
		logger.println(".##........##..##.....##.##.......##..##..##.##.....##.##.....##.##.....##"); 
		logger.println(".##........##..##.....##.##.......##..##..##.##.....##.##.....##.##.....##"); 
		logger.println(".######....##..########..######...##..##..##.##.....##.##.....##.##.....##"); 
		logger.println(".##........##..##...##...##.......##..##..##.##.....##.##.....##.##.....##"); 
		logger.println(".##........##..##....##..##.......##..##..##.##.....##.##.....##.##.....##"); 
		logger.println(".##.......####.##.....##.########..###..###...#######...#######..########.");
		logger.println("by Lightning Creations");
		logger.println();
		logger.println("Starting...");
		logger.println();
		logger.println("Searching for games...");
		Reflections r = new Reflections("");
		Set<Class<?>> gamesSet = r.getTypesAnnotatedWith(FirewoodGame.class);
		Class<?>[] games = new Class<?>[gamesSet.size()];
		int index = 0;
		int exGameIndex = 0;
		
		logger.println();
		logger.println("Found:");
		
		for(Class<?> game : gamesSet) {
			games[index] = game;
			logger.println("\t* "+game.getName() + ": " + game.getName());
			if(game.getName().equals("Example Game"))
				exGameIndex = index;
			index++;
		}
		logger.println();
		
		if(games.length == 1) {
			logger.println("Only detected ExampleGame... Oh well.\n");
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
		
		logger.println("Starting " + annotation.name() + (annotation.description().isEmpty() ? ", which has no description provided." : (": " + annotation.description())));
		
		GLFWErrorCallback.createPrint(new PrintStream(new Logger())).set();
		
		if(!glfwInit()) {
			logger.printErr("FATAL ERROR! Could not initialize GLFW!");
			logger.printErr("I mean, it is kind of hard to play a game without having a window...");
			System.exit(1);
		}
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		
		window = glfwCreateWindow(720, 480, annotation.name(), NULL, NULL);
		if(window == NULL) {
			logger.printErr("FATAL ERROR! Could not initialize GLFW!");
			logger.printErr("I mean, it is kind of hard to play a game without having a window...");
			System.exit(1);
		}
		
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwSetInputMode(window, GLFW_STICKY_MOUSE_BUTTONS, GLFW_TRUE);
		glfwSetInputMode(window, GLFW_STICKY_KEYS, GLFW_TRUE);
		glfwShowWindow(window);
		
		GL.createCapabilities();
		glClearColor(0,0,0,1);
		
		glFrontFace(GL_CW);
		glCullFace(GL_BACK);
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);

		glEnable(GL_TEXTURE_2D);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		FirewoodParent game = null;
		
		try {
			Logger.getLogger().subTask();
			game = (FirewoodParent) gameClass.newInstance();
			Logger.getLogger().returned();
		} catch(Error | Exception e) {
			logger.printErr("FATAL ERROR! ");
			e.printStackTrace();
			System.exit(1);
		}
		
		boolean wasLoading = false;
		boolean mouseDown = false;
		
		while(!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			if(GameState.isLoading()) {
				// TODO: Put loading texture on-screen
				if(!wasLoading) {
					wasLoading = true;
					logger.println("Loading...");
					game.startLoading(GameState.nextState());
				}
				for(int i = 0; i < toLoad.size(); i++) {
					Resource res = toLoad.get(i);
					if(res.finished) {
						if(res.error) {
							logger.printErrln("ERROR: Could not load " + res.file.getAbsolutePath() + "!");
							logger.printErrln("The game will decide whether this is fatal or not.");
						}
						resources.add(res);
						resNames.add(toLoadNames.get(i));
						toLoad.remove(i);
						toLoadNames.remove(i);
						i--; // We removed this object, so everything is now back one.
					}
				}
				if(toLoad.size() == 0) {
					GameState.finishedLoading();
					wasLoading = false;
				}
			}
			
			if(GameState.isPaused()) {
				// TODO: Apply post-shader for half-bright
			}
			
			if(GameState.isMainGame()) {
				if(border == null) {
					logger.printErrln("ERROR! Game didn't configure border!");
					logger.printErrln("This isn't fatal; attempting to find border graphics...");
					
					File root = new File("assets");
					
					String[] directories = root.list(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return new File(dir, name).isDirectory();
						}
					});
					
					for(int i = 0; i < directories.length; i++) {
						logger.println("Found directory: " + directories[i]);
					}
					
					String bestDirectory = directories[Util.findClosestString(directories, gameClass.getTypeName())];
					
					logger.println("Trying to find border in /assets/" + bestDirectory + "...");
					
					if(border == null) { // Need border
						logger.println("Creating border...");
						logger.subTask();
						border = new Border(bestDirectory);
						logger.returned();
						logger.println("Found border, lucky... do it yourself next time, developer.");
					}
				}
				glDisable(GL_CULL_FACE); // Disabling culling suggested by Kristof09
				// Check dimensions of window
				int[] w = new int[1];
				int[] h = new int[1];
				glfwGetWindowSize(window, w, h);
				int width = w[0];
				int height = h[0];
				double mainAspect = ((double)border.getTextureWidth(0)+border.getTextureWidth(2)+border.getTextureWidth(3))/(border.getTextureHeight(2)+border.getTextureHeight(0)+border.getTextureHeight(1));
				glViewport(0,0,width,height);
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
				Std3DShader.enable();
				
				Std3DShader.disable();
			}
			
			if(GameState.isPaused()) {
				// TODO: Remove half-bright post-shader
			}
			
			if(GameState.isMenu()) {
				if(font == null) {
					logger.printErrln("ERROR! Game didn't configure font!");
					logger.printErrln("This isn't fatal; attempting to find font graphics...");
					
					File root = new File("assets");
					
					String[] directories = root.list(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return new File(dir, name).isDirectory();
						}
					});
					
					for(int i = 0; i < directories.length; i++) {
						logger.println("Found directory: " + directories[i]);
					}
					
					String bestDirectory = directories[Util.findClosestString(directories, gameClass.getTypeName())];
					
					logger.println("Trying to find font in /assets/" + bestDirectory + "...");
					
					logger.println("Creating font...");
					logger.subTask();
					if(new File("assets/" + bestDirectory + "/gfx/font/font.bmp").exists())
						font = new Font("assets/" + bestDirectory + "/gfx/font/font.bmp");
					else {
						logger.printErrln("FATAL ERROR! Font is not there!");
						System.exit(1);
					}
					logger.returned();
					logger.println("Found font, lucky... do it yourself next time, developer.");
				}
				
				if(curMenu == null) {
					logger.printErrln("FATAL ERROR! There is no active menu!");
					System.exit(1);
				}

				IntBuffer w = BufferUtils.createIntBuffer(1);
				IntBuffer h = BufferUtils.createIntBuffer(1);
				glfwGetWindowSize(window, w, h);
				int width = w.get(0);
				int height = h.get(0);
				glViewport(0,0,width,height);
				glDisable(GL_CULL_FACE);
				float xMult, yMult;
				if(width > height) {
					xMult = (float)height/width;
					yMult = 1;
				} else {
					xMult = 1;
					yMult = (float)width/height;
				}
				for(int i = 0; i < curMenu.nodes.length; i++) {
					MenuNode curNode = curMenu.nodes[i];
					curNode.graphic.bind();
					glBegin(GL_QUADS);
					{
						glTexCoord2f(0,0);
						glVertex4f(xMult*curNode.x,yMult*curNode.y,0.5f,1);
						glTexCoord2f(1,0);
						glVertex4f(xMult*(curNode.x+curNode.width),yMult*curNode.y,0.5f,1);
						glTexCoord2f(1,1);
						glVertex4f(xMult*(curNode.x+curNode.width),yMult*(curNode.y+curNode.height),0.5f,1);
						glTexCoord2f(0,1);
						glVertex4f(xMult*(curNode.x),yMult*(curNode.y+curNode.height),0.5f,1);
					}
					glEnd();
					float textWidth = font.getWidth(curNode.text);
					float textHeight = font.getHeight(curNode.text);
					float textAspect = textWidth/textHeight;
					float buttonAspect = curNode.width/curNode.height;
					if(textAspect == buttonAspect)
						font.render(curNode.text, xMult*curNode.x, yMult*curNode.y, 0.4f, xMult*curNode.height, yMult*curNode.height);
					/*else if(textAspect > buttonAspect)
						font.render(curNode.text, curNode.x, curNode.y-(curNode.height-buttonAspect/textAspect*textHeight/font.bmpTexture.getHeight())/2, 0.4f, buttonAspect/textAspect*curNode.height);
					else
						font.render(curNode.text, curNode.x+(curNode.width-textAspect/buttonAspect*textWidth/font.bmpTexture.getWidth()*128)/2, curNode.y, 0.4f, textAspect/buttonAspect*curNode.height);
					Need to correct this code, commented until fixed.*/
				}
				if(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
					mouseDown = true;
				} else if(mouseDown == true) {
					System.out.println("PRESS");
					mouseDown = false;
					double[] xpos = new double[1];
					double[] ypos = new double[1];
					glfwGetCursorPos(window, xpos, ypos);
					double x = xpos[0]/width*2-1;
					double y = ypos[0]/height*-2+1;
					for(int i = 0; i < curMenu.nodes.length; i++) {
						MenuNode curNode = curMenu.nodes[i];
						if(x >= curNode.x && x < curNode.x + curNode.width && y >= curNode.y && y < curNode.y + curNode.height) {
							curNode.go();
							break;
						}
					}
				}
				glEnable(GL_CULL_FACE);
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
	
	public static void setFont(Font newFont) {
		font = newFont;
	}
	
	public static void setCurMenu(Menu newMenu) {
		curMenu = newMenu;
	}
	
	public static void queueLoad(Resource resource, String name) {
		if(resNames.contains(name)) {
			resource.postLoad.invoke(resource);
		} else {
			toLoad.add(resource);
			toLoadNames.add(name);
		}
	}
}
