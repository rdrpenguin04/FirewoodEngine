/**
 * Class for containing various types of fonts with the same printing functions.
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
package com.lightning.firewood.rendering;

import java.io.File;

import com.lightning.firewood.loading.ResourceType;
import com.lightning.firewood.util.Logger;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Ray Redondo
 *
 */
public class Font extends ResourceType {
	private FontType type;
	
	public Texture bmpTexture = null;
	
	public Font() {}
	
	public Font(String fileName) {
		Logger.getLogger().println("Loading " + fileName + " as a font...");
		load(new File(fileName));
	}
	
	public Font(File file) {
		Logger.getLogger().println("Loading " + file.getAbsolutePath() + " as a font...");
		load(file);
	}
	
	public void load(File f) {
		if(f.getName().endsWith(".bmp")) {
			// Bitmap monospace font. Assuming 7-bit (ASCII or compatible).
			type = FontType.ASCII_BMP;
			Logger.getLogger().subTask();
			bmpTexture = new Texture();
			bmpTexture.forceAlphaOnWhite = true;
			bmpTexture.load(f);
			Logger.getLogger().returned();
			
		}
	}

	public int getWidth(String text) {
		if(type == FontType.ASCII_BMP) return text.length() * bmpTexture.getWidth()/128;
		else return -1; // Hmm...
	}
	
	public int getHeight(String text) {
		if(type == FontType.ASCII_BMP) return bmpTexture.getHeight();
		else return -1; // Hmm...
	}
	
	public void render(String text, float x, float y, float z, float scaleX, float scaleY) {
		switch(type) {
		case ASCII_BMP:
			for(int i = 0; i < text.length(); i++) {
				glBegin(GL_QUADS);
				{
					glTexCoord2f(((int)text.charAt(i))/128.0f, 1);
					glVertex4f(x+i*scaleX, y, z, 1);
					glTexCoord2f(((int)text.charAt(i)+1)/128.0f, 1);
					glVertex4f(x+(i+1)*scaleX, y, z, 1);
					glTexCoord2f(((int)text.charAt(i)+1)/128.0f, 0);
					glVertex4f(x+(i+1)*scaleX, y+scaleY, z, 1);
					glTexCoord2f(((int)text.charAt(i))/128.0f, 0);
					glVertex4f(x+i*scaleX, y+scaleY, z, 1);
				}
				glEnd();
			}
		}
	}
	
	private static enum FontType {
		ASCII_BMP
	}
}
