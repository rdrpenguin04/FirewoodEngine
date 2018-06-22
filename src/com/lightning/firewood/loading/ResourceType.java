package com.lightning.firewood.loading;

import java.io.File;

public abstract class ResourceType {
	public ResourceType() {}
	
	public abstract void load(File f);
}
