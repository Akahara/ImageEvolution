package fr.wonder.iev;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

import java.io.IOException;

public class Shader {
	
	final int id;
	
	public Shader(String fileName, int type) throws IOException {
		int shader = 0;
		
		String source = new String(Shader.class.getResourceAsStream(fileName).readAllBytes());
		shader = glCreateShader(type);
		
		glShaderSource(shader, source);
		glCompileShader(shader);
		
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
			throw new IOException("Unable to parse the " + fileName + " shader ! " + glGetShaderInfoLog(shader));
		}
			
		this.id = shader;
	}
}
