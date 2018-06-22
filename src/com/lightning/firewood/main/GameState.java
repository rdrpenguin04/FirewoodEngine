/**
 * Stores the current state of the game.
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

/**
 * @author Ray Redondo
 *
 */
public class GameState {
	public enum GameStateEnum {
		MAIN_MENU(false, true, false, true, false, null),
		MAIN_GAME(true, false, false, false, false, null),
		MAIN_GAME_PAUSED(true, false, true, true, false, null),
		LOAD_TO_MAIN_MENU(false, false, false, false, true, MAIN_MENU),
		LOAD_TO_MAIN_GAME(false, false, false, false, true, MAIN_GAME),
		LOAD_TO_MAIN_GAME_PAUSED(false, false, false, false, true, MAIN_GAME_PAUSED);
		
		boolean isMainGame;
		boolean isMainMenu;
		boolean isPaused;
		boolean isMenu;
		boolean isLoading;
		GameStateEnum next;
		
		GameStateEnum(boolean isMainGame, boolean isMainMenu, boolean isPaused, boolean isMenu, boolean isLoading, GameStateEnum next) {
			this.isMainGame = isMainGame;
			this.isMainMenu = isMainMenu;
			this.isPaused = isPaused;
			this.isMenu = isMenu;
			this.isLoading = isLoading;
			this.next = next;
		}
		
		public GameStateEnum pause() {
			if(ordinal() == 0) return MAIN_GAME_PAUSED;
			else return this;
		}
		
		public GameStateEnum unpause() {
			if(ordinal() == 1) return MAIN_GAME;
			else return this;
		}
		
		public GameStateEnum loadState(GameStateEnum next) {
			if(next.ordinal() >= values().length) return next;
			return values()[next.ordinal()+values().length/2];
		}
		
		public GameStateEnum finishedLoading() {
			if(ordinal() < values().length/2) return this;
			return next;
		}
	}

	private static GameStateEnum state = GameStateEnum.LOAD_TO_MAIN_MENU;
	
	public static boolean isMainGame() {
		return state.isMainGame;
	}
	
	public static boolean isMainMenu() {
		return state.isMainMenu;
	}
	
	public static boolean isPaused() {
		return state.isPaused;
	}
	
	public static boolean isMenu() {
		return state.isMenu;
	}
	
	public static boolean isLoading() {
		return state.isLoading;
	}
	
	public static void pause() {
		state = state.pause();
	}
	
	public static void unpause() {
		state = state.unpause();
	}
	
	public static void loadState(GameStateEnum next) {
		state = state.loadState(next);
	}
	
	public static void finishedLoading() {
		state = state.finishedLoading();
	}
	
	public static void replaceGameState(GameStateEnum newState) {
		state = newState;
	}
}
