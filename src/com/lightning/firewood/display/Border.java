/**
 * Defines the playfield border, including all animations and widgets.
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
package com.lightning.firewood.display;

import java.io.File;

import com.lightning.firewood.loading.ResourceType;
import com.lightning.firewood.rendering.*;

/**
 * @author Ray Redondo
 *
 */
public class Border extends ResourceType {
	private Texture[] borderTextures;
	
	public Border() {}
	
	public Border(String gameDir) {
		this(new File("assets/" + gameDir + "/gfx/border/"));
	}
	
	public Border(File gameDir) {
		load(gameDir);
	}
	
	public void bindTexture(int texID) {
		borderTextures[texID].bind();
	}
	
	public int getTextureWidth(int texID) {
		return borderTextures[texID].getWidth();
	}
	
	public int getTextureHeight(int texID) {
		return borderTextures[texID].getHeight();
	}

	public void load(File f) {
		borderTextures = new Texture[12];
		borderTextures[0] = new Texture(f.getAbsolutePath()+"/upFixed.png");
		borderTextures[1] = new Texture(f.getAbsolutePath()+"/downFixed.png");
		borderTextures[2] = new Texture(f.getAbsolutePath()+"/leftFixed.png");
		borderTextures[3] = new Texture(f.getAbsolutePath()+"/rightFixed.png");
		borderTextures[4] = new Texture(f.getAbsolutePath()+"/ulFixed.png");
		borderTextures[5] = new Texture(f.getAbsolutePath()+"/urFixed.png");
		borderTextures[6] = new Texture(f.getAbsolutePath()+"/dlFixed.png");
		borderTextures[7] = new Texture(f.getAbsolutePath()+"/drFixed.png");
		borderTextures[8] = new Texture(f.getAbsolutePath()+"/upExpand.png");
		borderTextures[9] = new Texture(f.getAbsolutePath()+"/downExpand.png");
		borderTextures[10] = new Texture(f.getAbsolutePath()+"/leftExpand.png");
		borderTextures[11] = new Texture(f.getAbsolutePath()+"/rightExpand.png");
	}
}
