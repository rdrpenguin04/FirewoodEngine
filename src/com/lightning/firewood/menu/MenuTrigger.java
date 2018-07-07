package com.lightning.firewood.menu;

import com.lightning.firewood.rendering.Texture;

public class MenuTrigger extends MenuNode {
	private Runnable r;
	
	public MenuTrigger(Texture graphic, String text, float x, float y, float width, float height, Runnable r) {
		super(graphic, text, x, y, width, height);
		this.r = r;
	}
	
	public void go() {
		r.run();
	}
}
