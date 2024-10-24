package fr.wonder.gl;


import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import fr.wonder.gl.Uniform.Type;

public class ShaderProgram implements Disposable {

	private String dbgName;
	private int id;
	
	private Map<String, Integer> uniformCache = new HashMap<>();
	private List<Uniform> scanedUniforms;
	private List<UniformBlock> scanedBlocks;
	
	public ShaderProgram(Shader... shaders) throws IllegalStateException {
		this.id = glCreateProgram();
		this.dbgName = Arrays.toString(shaders);
		
		for(Shader s : shaders)
			glAttachShader(id, s.getId());
		
		glLinkProgram(id);
		glValidateProgram(id);
		
		if(glGetProgrami(id, GL_VALIDATE_STATUS) == GL_FALSE) {
			String log = glGetProgramInfoLog(id);
			glDeleteProgram(id);
			throw new IllegalStateException("Unable to validate a shader program ! " + log);
		}
	}
	
	public List<Uniform> getUniforms() {
		if(scanedUniforms == null)
			scanUniforms();
		return scanedUniforms;
	}
	
	public List<UniformBlock> getUniformBlocks() {
		if(scanedUniforms == null)
			scanUniforms();
		return scanedBlocks;
	}
	
	private void scanUniforms() {
		this.scanedUniforms = new ArrayList<>();
		this.scanedBlocks = new ArrayList<>();
		
		int uniformCount = glGetProgrami(id, GL_ACTIVE_UNIFORMS);
		int blockCount = glGetProgrami(id, GL_ACTIVE_UNIFORM_BLOCKS);
		
		List<List<Uniform>> blockUniforms = new ArrayList<>(blockCount);
		for(int i = 0; i < blockCount; i++)
			blockUniforms.add(new ArrayList<>());
		
		int[] uniformIds = IntStream.range(0, uniformCount).toArray();
		int[] types = new int[uniformCount];
		int[] sizes = new int[uniformCount];
		int[] strides = new int[uniformCount];
		int[] blockIndices = new int[uniformCount];
		int[] blockOffsets = new int[uniformCount];
		glGetActiveUniformsiv(id, uniformIds, GL_UNIFORM_TYPE, types);
		glGetActiveUniformsiv(id, uniformIds, GL_UNIFORM_SIZE, sizes);
		glGetActiveUniformsiv(id, uniformIds, GL_UNIFORM_BLOCK_INDEX, blockIndices);
		glGetActiveUniformsiv(id, uniformIds, GL_UNIFORM_OFFSET, blockOffsets);
		glGetActiveUniformsiv(id, uniformIds, GL_UNIFORM_ARRAY_STRIDE, strides);
		for(int i = 0; i < uniformCount; i++) {
			Type utype = getUniformType(types[i]);
			String uname = glGetActiveUniformName(id, i);
			int usize = sizes[i];
			int ustride = strides[i];
			int ublockOffset = blockOffsets[i];
			if(usize > 1) uname = uname.substring(0, uname.length()-3); // remove "[0]" of array uniforms
			Uniform uniform = new Uniform(utype, uname, usize, ustride, ublockOffset);
			
			if(blockIndices[i] == -1) {
				scanedUniforms.add(uniform);
			} else {
				blockUniforms.get(i).add(uniform);
			}
		}
		
		for(int i = 0; i < blockCount; i++) {
			int bsize = glGetActiveUniformBlocki(id, i, GL_UNIFORM_BLOCK_DATA_SIZE);
			String bname = glGetActiveUniformBlockName(id, i);
			scanedBlocks.add(new UniformBlock(bname, bsize, blockUniforms.get(i)));
		}
	}
	
	private static Uniform.Type getUniformType(int glType) {
		switch(glType) {
		case GL_FLOAT:      return Type.FLOAT;
		case GL_FLOAT_VEC2: return Type.VEC2;
		case GL_FLOAT_VEC3: return Type.VEC3;
		case GL_FLOAT_VEC4: return Type.VEC4;
		case GL_FLOAT_MAT2: return Type.MAT2;
		case GL_FLOAT_MAT3: return Type.MAT3;
		case GL_FLOAT_MAT4: return Type.MAT4;
//		case GL_BOOL:       return Type.BOOL;  // not yet implemented
//		case GL_INT:        return Type.INT;
		case GL_SAMPLER_2D: return Type.SAMPLER2D;
		default: throw new RuntimeException("Unhandled uniform type " + glType);
		}
	}
	
	public void bind() {
		glUseProgram(id);
	}
	
	public static void unbind() {
		glUseProgram(0);
	}

	public void setUniform1i(String name, int i) { glUniform1i(getUniformLoc(name), i); }
	public void setUniform1f(String name, float f) { glUniform1f(getUniformLoc(name), f); }
	public void setUniform1fv(String name, float[] f) { glUniform1fv(getUniformLoc(name), f); }
	public void setUniform2f(String name, float f1, float f2) { glUniform2f(getUniformLoc(name), f1, f2); }
	public void setUniform2fv(String name, float[] f) { glUniform2fv(getUniformLoc(name), f); }
	public void setUniform3f(String name, float f1, float f2, float f3) { glUniform3f(getUniformLoc(name), f1, f2, f3); }
	public void setUniform3fv(String name, float[] f) { glUniform3fv(getUniformLoc(name), f); }
	public void setUniform3f(String name, Color c) { setUniform3f(name, c.r, c.g, c.b); }
	public void setUniform4f(String name, float f1, float f2, float f3, float f4) { glUniform4f(getUniformLoc(name), f1, f2, f3, f4); }
	public void setUniform4fv(String name, float[] f) { glUniform4fv(getUniformLoc(name), f); }
	public void setUniform4f(String name, Color c) { setUniform4f(name, c.r, c.g, c.b, c.a); }
	public void setUniformMat2f(String name, float[] m) { glUniformMatrix2fv(getUniformLoc(name), true, m); }
	public void setUniformMat3f(String name, float[] m) { glUniformMatrix3fv(getUniformLoc(name), true, m); }
	public void setUniformMat4f(String name, float[] m) { glUniformMatrix4fv(getUniformLoc(name), true, m); }
	
	private int getUniformLoc(String name) {
		int loc;
		
		if(uniformCache.containsKey(name))
			loc = uniformCache.get(name);
		else {
			loc = glGetUniformLocation(id, name);
			uniformCache.put(name, loc);
			
			if(loc == -1)
				System.err.println("A uniform does not exist in a shader ! " + name);
		}
		
		return loc;
	}
	
	@Override
	public void dispose() {
		glDeleteProgram(id);
		id = 0;
		uniformCache.clear();
	}
	
	@Override
	public String toString() {
		return dbgName == null ? super.toString() : id + "." + dbgName;
	}
	
}
