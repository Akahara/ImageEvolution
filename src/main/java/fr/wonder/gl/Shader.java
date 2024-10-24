package fr.wonder.gl;


import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

import java.io.IOException;
import java.io.InputStream;

public class Shader implements Disposable {
	
	public enum ShaderType {
		
		VERTEX  (GL_VERTEX_SHADER  , "vs"),
		FRAGMENT(GL_FRAGMENT_SHADER, "fs"),
		COMPUTE (GL_COMPUTE_SHADER , "cs"),
		GEOMETRY(GL_GEOMETRY_SHADER, "gs")
		;
		
		private final int glType;
		public final String extension;
		
		private ShaderType(int glType, String extension) {
			this.glType = glType;
			this.extension = extension;
		}
		
		public static ShaderType fromExtension(String ext) {
			for(ShaderType type : values())
				if(type.extension.equals(ext))
					return type;
			return null;
		}
		
	}
	
	private String dbgName;
	private int id;
	
	public Shader(String source, ShaderType type) {
		this.id = glCreateShader(type.glType);
		
		glShaderSource(id, source);
		glCompileShader(id);
		
		if(glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
			String log = glGetShaderInfoLog(id);
			glDeleteShader(id);
			throw new RuntimeException("Unable to parse shader ! " + log);
		}
	}
	
	public static Shader fromResources(String path) throws IOException {
		ShaderType type = ShaderType.fromExtension(fr.wonder.gl.FilesUtils.getFileExtension(path));
		if(type == null)
			throw new IOException("Unknown shader file extension '" + FilesUtils.getFileExtension(path) + "'");
		try (InputStream is = Shader.class.getResourceAsStream(path)) {
			if (is == null)
				throw new RuntimeException("Could not load shader resource " + path);
			Shader shader = new Shader(new String(is.readAllBytes()), type);
			shader.dbgName = path.substring(path.indexOf('/')+1);
			return shader;
		}
	}
	
	@Override
	public void dispose() {
		glDeleteShader(id);
		id = 0;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return dbgName == null ? super.toString() : id + "|" + dbgName;
	}
	
}
