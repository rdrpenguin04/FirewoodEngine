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

import com.lightning.firewood.rendering.*;

/**
 * @author Ray Redondo
 *
 */
public class Border {
	private Texture[] borderTextures;
	
	public Border(String gameDir) {
		String borderResourceDir = "assets/" + gameDir + "/gfx/border/";
		borderTextures = new Texture[24];
		borderTextures[0] = new Texture(borderResourceDir+"upFixed.png");
		borderTextures[1] = new Texture(borderResourceDir+"downFixed.png");
		borderTextures[2] = new Texture(borderResourceDir+"leftFixed.png");
		borderTextures[3] = new Texture(borderResourceDir+"rightFixed.png");
		borderTextures[0] = new Texture(borderResourceDir+"ulFixed.png");
		borderTextures[1] = new Texture(borderResourceDir+"urFixed.png");
		borderTextures[2] = new Texture(borderResourceDir+"dlFixed.png");
		borderTextures[3] = new Texture(borderResourceDir+"drFixed.png");
	}
}
