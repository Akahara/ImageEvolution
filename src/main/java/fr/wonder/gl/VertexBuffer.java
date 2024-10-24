package fr.wonder.gl;


import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.GL_VERTEX_ARRAY_BINDING;

import java.nio.ByteBuffer;

public class VertexBuffer implements Disposable {
	
	private int bufferId;
	
	public VertexBuffer(float... data) {
		this.bufferId = glGenBuffers();
		if(glGetInteger(GL_VERTEX_ARRAY_BINDING) != 0)
			throw new IllegalStateException("VBO generated with active vao bound");
		glBindBuffer(GL_ARRAY_BUFFER, bufferId);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public VertexBuffer() {
		this.bufferId = glGenBuffers();
	}
	
	public VertexBuffer(ByteBuffer data) {
		this.bufferId = glGenBuffers();
		if(glGetInteger(GL_VERTEX_ARRAY_BINDING) != 0)
			throw new IllegalStateException("VBO generated with active vao bound");
		glBindBuffer(GL_ARRAY_BUFFER, bufferId);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	private VertexBuffer(int id) {
		this.bufferId = id;
	}
	
	public static VertexBuffer emptyBuffer() {
		return new VertexBuffer(glGenBuffers());
	}

	/** Buffer must be bound */
	public void setData(float... data) {
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
	}
	
	/** Buffer must be bound */
	public void setData(ByteBuffer data) {
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
	}

	/** Buffer must be bound */
	public void updateData(ByteBuffer data) {
		glBufferSubData(GL_ARRAY_BUFFER, 0, data);
	}
	
	/** Buffer must be bound */
	public void updateData(float[] data) {
		glBufferSubData(GL_ARRAY_BUFFER, 0, data);
	}
	
	/** Buffer must be bound */
	public void readData(ByteBuffer dataStore) {
		glGetBufferSubData(GL_ARRAY_BUFFER, 0, dataStore);
	}
	
	public void bind() {
		glBindBuffer(GL_ARRAY_BUFFER, bufferId);
	}
	
	public static void unbind() {
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void dispose() {
		glDeleteBuffers(bufferId);
		bufferId = 0;
	}
	
	public int getBufferId() {
		return bufferId;
	}
	
}
