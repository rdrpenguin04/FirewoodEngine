package com.lightning.firewood.rendering;

import static org.lwjgl.opengl.GL20.*;

import com.lightning.firewood.util.Logger;

public class Std3DShader {
	private static final String VERT_SHADER = "" + 
			"#version 120\n" + 
			"#extension GL_ARB_explicit_attrib_location : require\n" + 
			"\n" + 
			"layout (location = 0) in vec3 pos;\n" + 
			"layout (location = 1) in vec2 texCoord;\n" + 
			"\n" + 
			"uniform mat4 Model;\n" + 
			"uniform mat4 View;\n" + 
			"uniform mat4 Projection;" + 
			"\n" + 
			"varying vec2 texCoord0;\n" + 
			"\n" + 
			"void main() {\n" + 
			"    gl_Position = Projection * View * Model * vec4(pos, 1.0);\n" + 
			"    texCoord0 = texCoord;" + 
			"}";
	private static final String FRAG_SHADER = "" + 
			"#version 120\n" + 
			"\n" + 
			"varying vec2 texCoord0;\n" + 
			"\n" + 
			"uniform sampler2D texture;\n" + 
			"\n" + 
			"void main() {\n" + 
			"    gl_FragColor = texture2D(texture, texCoord0);" + 
			"}";
	private static final int PROGRAM;
	
	static {
		Logger.getLogger().println("Compiling shader...");
		final int vShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vShader, VERT_SHADER);
		glCompileShader(vShader);
		int compStatus = glGetShaderi(vShader, GL_COMPILE_STATUS);
		if(compStatus == GL_FALSE) {
			Logger.getLogger().printErrln("Error compiling vertex shader!");
			Logger.getLogger().printErrln(glGetShaderInfoLog(vShader));
			System.exit(1);
		}
		final int fShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fShader, FRAG_SHADER);
		glCompileShader(fShader);
		compStatus = glGetShaderi(fShader, GL_COMPILE_STATUS);
		if(compStatus == GL_FALSE) {
			Logger.getLogger().printErrln("Error compiling fragment shader!");
			Logger.getLogger().printErrln(glGetShaderInfoLog(fShader));
			System.exit(1);
		}
		PROGRAM = glCreateProgram();
		glAttachShader(PROGRAM, vShader);
		glAttachShader(PROGRAM, fShader);
		glLinkProgram(PROGRAM);
		int linkStatus = glGetProgrami(PROGRAM, GL_LINK_STATUS);
		if(linkStatus == GL_FALSE) {
			Logger.getLogger().printErrln("Error linking shader!");
			Logger.getLogger().printErrln(glGetProgramInfoLog(PROGRAM));
			System.exit(1);
		}
		glDeleteShader(vShader);
		glDeleteShader(fShader);
		Logger.getLogger().println("Done!");
	}
	
	public static void enable() {
		
	}
	
	public static void disable() {
		glUseProgram(0);
	}
}
