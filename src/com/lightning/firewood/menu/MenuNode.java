package com.lightning.firewood.menu;

import com.lightning.firewood.rendering.Texture;

public abstract class MenuNode {
	public Texture graphic;
	public String text;
	public float x;
	public float y;
	public float width;
	public float height;
	
	public MenuNode(Texture graphic, String text, float x, float y, float width, float height) {
		this.graphic = graphic;
		this.text = text;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public abstract void go();
}
