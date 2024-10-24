package fr.wonder.gl;


import static org.lwjgl.opengl.GL46.*;

public class UniformBuffer implements Disposable {
	
	private int id;
	private UniformBlock block;
	
	public UniformBuffer(UniformBlock block) {
		this.block = block;
		this.id = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, id);
		glBufferData(GL_UNIFORM_BUFFER, block.size, GL_DYNAMIC_DRAW);
	}
	
	public void bind(int index) {
		glBindBufferBase(GL_UNIFORM_BUFFER, index, id);
	}
	
	public void setUniform(String name, float[] value) throws IllegalArgumentException {
		Uniform u = block.getUniform(name);
		if(u == null)
			throw new IllegalArgumentException("Unknown uniform '" + name + "' in block");
		if(u.type.getSize()*u.size != value.length)
			throw new IllegalArgumentException(String.format("Invalid value for uniform %s, expected %d floats, got %d", u.name, u.type.getSize()*u.size, value.length));
		glBindBuffer(GL_UNIFORM_BUFFER, id);
		glBufferSubData(GL_UNIFORM_BUFFER, u.blockOffset, value);
	}
	
	public void setUniform(String baseName, int arrayIndex, float[] value) throws IllegalArgumentException {
		Uniform u = block.getUniform(baseName);
		if(u == null)
			throw new IllegalArgumentException("Unknown uniform '" + baseName + "' in block");
		if(u.type.getSize() != value.length)
			throw new IllegalArgumentException(String.format("Invalid value for uniform %s[%d], expected %d floats, got %d", u.name, arrayIndex, u.type.getSize(), value.length));
		if(arrayIndex < 0 || arrayIndex >= u.size)
			throw new IllegalArgumentException(String.format("Invalid index %d for uniform %s[%d]", arrayIndex, u.name, u.size));
		glBindBuffer(GL_UNIFORM_BUFFER, id);
		glBufferSubData(GL_UNIFORM_BUFFER, u.blockOffset + arrayIndex * u.stride, value);
	}
	
	public static void unbind(int index) {
		glBindBufferBase(GL_UNIFORM_BUFFER, index, 0);
	}
	
	@Override
	public void dispose() {
		glDeleteBuffers(id);
		id = 0;
	}

}
