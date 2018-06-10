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

import java.util.Set;

import org.reflections.*;

import com.lightning.firewood.display.Border;
import com.lightning.firewood.identification.*;

/**
 * @author Ray Redondo
 *
 */
public class Main {
	private static Border border;
	
	/**
	 * The main function of the engine. Finds an annotated game and runs it.
	 * If no external games are found, defaults to <code>com.lightning.firewood.example.ExampleGame</code>.
	 * 
	 * @param args The command line arguments. Not used at the moment.
	 */
	public static void main(String[] args) {
		System.out.println(
				".########.####.########..########.##......##..#######...#######..########.\n" + 
				".##........##..##.....##.##.......##..##..##.##.....##.##.....##.##.....##\n" + 
				".##........##..##.....##.##.......##..##..##.##.....##.##.....##.##.....##\n" + 
				".######....##..########..######...##..##..##.##.....##.##.....##.##.....##\n" + 
				".##........##..##...##...##.......##..##..##.##.....##.##.....##.##.....##\n" + 
				".##........##..##....##..##.......##..##..##.##.....##.##.....##.##.....##\n" + 
				".##.......####.##.....##.########..###..###...#######...#######..########.");
		System.out.println("by Lightning Creations");
		System.out.println("\nStarting...");
		
		System.out.println("\nSearching for games...");
		Reflections r = new Reflections("");
		Set<Class<?>> gamesSet = r.getTypesAnnotatedWith(FirewoodGame.class);
		Class<?>[] games = new Class<?>[gamesSet.size()];
		int index = 0;
		int exGameIndex = 0;
		
		System.out.println("\nFound:");
		
		for(Class<?> game : gamesSet) {
			games[index] = game;
			System.out.println("  * "+game.getName());
			if(game.getName().equals("Example Game"))
				exGameIndex = index;
			index++;
		}
		System.out.println();
		
		if(games.length == 1) {
			System.out.println("Only detected ExampleGame... Oh well.\n");
		}
		
		Class<?> gameClass = null;
		gameClass = games[exGameIndex];
		
		for(int i = 0; i < games.length; i++) {
			if((FirewoodParent.class.isAssignableFrom(games[i]) && !games[i].getName().equals("Example Game"))) {
				gameClass = games[i];
				break;
			}
		}
		
		FirewoodParent game = null;
		
		try {
			game = (FirewoodParent) gameClass.newInstance();
		} catch(Error | Exception e) {
			System.err.print("FATAL ERROR! ");
			e.printStackTrace();
			System.exit(1);
		}
		
		FirewoodGame annotation = gameClass.getAnnotation(FirewoodGame.class);
		
		System.out.println("Starting " + annotation.name() + (annotation.description().isEmpty() ? ", which has no description provided." : (": " + annotation.description())));
		
		if(border == null) {
			System.err.println("ERROR! Game didn't finish configuration!");
			System.err.println("This isn't fatal; attempting to find correct config files...");
			
			// TODO: I'm tired, but here's psuedocode:
			// 1. Get a list of all of the directories in /assets/
			// 2. Run the list through Util.findClosestString(list, annotation.name())
			// 3. Load config for missing items through there.
		}
	}
}
