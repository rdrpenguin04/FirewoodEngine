package com.lightning.firewood.menu;

public class Menu {
	public MenuNode[] nodes;
	MenuNode[][] prevStates = new MenuNode[0][];
	
	public Menu(MenuNode[] nodes) {
		this.nodes = nodes;
	}
}
