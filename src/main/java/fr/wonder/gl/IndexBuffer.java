package fr.wonder.gl;


import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

public class IndexBuffer implements Disposable {

	private int bufferId;
	private final int dataSize;
	
	public IndexBuffer(int... data) {
		bufferId = glGenBuffers();
		dataSize = data.length;
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferId);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public void bind() {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferId);
	}
	
	public static void unbind() {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void dispose() {
		glDeleteBuffers(bufferId);
		bufferId = 0;
	}

	public int size() {
		return dataSize;
	}
	
}
