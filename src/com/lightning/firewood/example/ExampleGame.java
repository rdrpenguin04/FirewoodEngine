/**
 * Example game main class.
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
package com.lightning.firewood.example;

import java.io.File;

import com.lightning.firewood.display.Border;
import com.lightning.firewood.identification.*;
import com.lightning.firewood.loading.Resource;
import com.lightning.firewood.main.GameState;
import com.lightning.firewood.main.GameState.GameStateEnum;
import com.lightning.firewood.main.Main;
import com.lightning.firewood.menu.Menu;
import com.lightning.firewood.menu.MenuNode;
import com.lightning.firewood.menu.MenuTrigger;
import com.lightning.firewood.rendering.Font;
import com.lightning.firewood.rendering.Texture;
import com.lightning.firewood.util.Invokable;
import com.lightning.firewood.util.Logger;

/**
 * @author Ray Redondo
 *
 */
@FirewoodGame(name="Example Game")
public class ExampleGame extends FirewoodParent {
	public ExampleGame() {
		Logger.getLogger().println("Here is where any initialization code would go. Not that you would really need any.");
	}
	
	@Override
	public void startLoading(GameStateEnum nextState) {
		if(nextState.equals(GameStateEnum.MAIN_GAME)) {
			Resource borderResource = new Resource(new File("assets/examplegame/gfx/border"), Border.class, new Invokable() {
				public void invoke(Object... params) {
					Resource resource = (Resource) params[0];
					if(resource.error) {
						Logger.getLogger().printErrln("Error?");
					}
					Main.setBorder((Border) resource.result);
				}
			});
			Main.queueLoad(borderResource, "border");
		} else if(nextState.equals(GameStateEnum.MAIN_MENU)) {
			Resource fontResource = new Resource(new File("assets/examplegame/gfx/font/font.bmp"), Font.class, new Invokable() {
				public void invoke(Object... params) {
					Resource resource = (Resource) params[0];
					if(resource.error) {
						Logger.getLogger().printErrln("Error?");
					}
					Main.setFont((Font) resource.result);
				}
			});
			Main.queueLoad(fontResource, "font");
			Main.setCurMenu(new Menu(new MenuNode[] {
				new MenuTrigger(new Texture("assets/examplegame/gfx/menu/button.png"), "Main game", -0.5f, -0.25f, 1, 0.5f, new Runnable() { public void run() { GameState.loadState(GameStateEnum.MAIN_GAME); } })
			}));
		}
	}
}
