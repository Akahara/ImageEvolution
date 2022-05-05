package fr.wonder.iev;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.*;

public class FrameBuffer {
	
	private final int id;
	private final int textureId;
	private final int width, height;
	
	public FrameBuffer(int width, int height, boolean antialiasing) {
		this.id = glGenFramebuffers();
		bind();
		this.textureId = glGenTextures();
		this.width = width;
		this.height = height;
		glBindTexture(GL_TEXTURE_2D, textureId);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
		if(antialiasing) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		} else {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		}
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
		glBindTexture(GL_TEXTURE_2D, 0);
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new IllegalStateException("Incomplete frame buffer");
		unbind();
	}

	/** Binds the active frame buffer to this buffer, *do not forget to call setViewPort* as well */
	public void bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, id);
		glViewport(0, 0, width, height);
	}
	
	public static void unbind() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public void dispose() {
		glDeleteFramebuffers(id);
	}
	
	public void bindTexture(int target) {
		if(target != 0) glActiveTexture(GL_TEXTURE0 + target);
		glBindTexture(GL_TEXTURE_2D, textureId);
		if(target != 0) glActiveTexture(GL_TEXTURE0);
	}
	
	public static void unbindTexture() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
}
