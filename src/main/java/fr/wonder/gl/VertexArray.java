package fr.wonder.gl;


import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL40.*;

import fr.wonder.gl.VertexBufferLayout.VertexBufferLayoutElement;

public class VertexArray implements Disposable {
	
	private int arrayId;
	
	public VertexArray() {
		arrayId = glGenVertexArrays();
	}
	
	public void bind() {
		glBindVertexArray(arrayId);
	}
	
	/**
	 * Bind a buffer to this vertex array object and bind it a layout,
	 * therefore when the layout will be called, all of the added vertex
	 * buffers will be used with their layouts.<br>
	 * May be called once only !
	 */
	public VertexArray setBuffer(VertexBuffer buffer, VertexBufferLayout layout) {
		bind();
		buffer.bind();
		int offset = 0;
		for(int i = 0; i < layout.elements.size(); i++) {
			VertexBufferLayoutElement elem = layout.elements.get(i);
			glEnableVertexAttribArray(i);
			glVertexAttribPointer(i, elem.count, elem.type, false, layout.stride, offset);
			offset += elem.getSize();
		}
		unbind();
		VertexBuffer.unbind();
		return this;
	}
	
	public VertexArray setIndices(IndexBuffer indices) {
		bind();
		indices.bind();
		unbind();
		return this;
	}
	
	public static void unbind() {
		glBindVertexArray(0);
	}
	
	@Override
	public void dispose() {
		glDeleteVertexArrays(arrayId);
		arrayId = 0;
	}
	
}
