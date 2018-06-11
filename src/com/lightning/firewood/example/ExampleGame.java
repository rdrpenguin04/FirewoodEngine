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

import com.lightning.firewood.display.Border;
import com.lightning.firewood.identification.*;
import com.lightning.firewood.util.Logger;

/**
 * @author Ray Redondo
 *
 */
@FirewoodGame(name="Example Game")
public class ExampleGame extends FirewoodParent {
	public ExampleGame() {
		Logger.subTask();
		setBorder(new Border("examplegame"));
		Logger.returned();
	}
}
