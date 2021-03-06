/**
 * Interface for establishing conformity with games for the Firewood Engine.
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
package com.lightning.firewood.identification;

import com.lightning.firewood.display.Border;
import com.lightning.firewood.main.GameState.GameStateEnum;
import com.lightning.firewood.main.Main;

/**
 * @author Ray Redondo
 *
 */
public abstract class FirewoodParent {
	public void setBorder(Border newBorder) {
		Main.setBorder(newBorder);
	}
	
	public void startLoading(GameStateEnum toState) {} // Recommended to implement, but not strictly required.
}
