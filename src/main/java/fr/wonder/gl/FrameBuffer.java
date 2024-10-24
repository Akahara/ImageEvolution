package fr.wonder.gl;


import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.IntStream;

public class FrameBuffer implements Disposable {
	
	private static final Deque<FrameBuffer> fboStack = new LinkedList<>();
	
	private int id;
	private Texture[] colorAttachments = new Texture[2];
	private Texture depthAttachment;
	
	public FrameBuffer() {
		this.id = glGenFramebuffers();
	}
	
	public void bind() {
		if(colorAttachments[0] == null)
			throw new IllegalStateException("No color attachment");
		if(fboStack.size() > 10)
			throw new IllegalStateException("FBOs were not unbound " + fboStack);
		fboStack.push(this);
		rebind();
	}
	
	private static void rebind() {
		FrameBuffer current = fboStack.peek();
		if(current == null) {
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			glViewport(0, 0, GLWindow.getWinWidth(), GLWindow.getWinHeight());
//			glReadBuffer(GL_COLOR_ATTACHMENT0);
		} else {
			glBindFramebuffer(GL_FRAMEBUFFER, current.id);
			glViewport(0, 0, current.colorAttachments[0].getWidth(), current.colorAttachments[0].getHeight());
			glReadBuffer(GL_COLOR_ATTACHMENT0);
			glDrawBuffers(IntStream.range(0, 10)
					.filter(i -> i<current.colorAttachments.length && current.colorAttachments[i]!=null)
					.map(i -> GL_COLOR_ATTACHMENT0+i).toArray());
		}
	}
	
	public void unbind() {
		if(fboStack.pop() != this)
			throw new IllegalStateException("This fbo is not the currently active fbo");
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(0, 0, GLWindow.getWinWidth(), GLWindow.getWinHeight());
	}
	
	@Override
	public void dispose() {
		glDeleteFramebuffers(id);
		id = 0;
	}
	
	public Texture getColorAttachment(int slot) {
		return colorAttachments[slot];
	}
	
	public void setColorAttachment(int slot, Texture colorAttachment) {
		if(colorAttachments[0] == null && slot != 0)
			throw new IllegalArgumentException("The first color attachment must be bound before all others");
		this.colorAttachments[slot] = colorAttachment;
		glBindFramebuffer(GL_FRAMEBUFFER, id);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + slot,
				colorAttachment.isMultisampled() ? GL_TEXTURE_2D_MULTISAMPLE : GL_TEXTURE_2D, colorAttachment.getId(), 0);
		checkCompleteness(GL_FRAMEBUFFER);
		rebind();
	}
	
	public Texture getDepthAttachment() {
		return depthAttachment;
	}
	
	public void setDepthAttachment(Texture depthTexture) {
		this.depthAttachment = depthTexture;
		glBindFramebuffer(GL_FRAMEBUFFER, id);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT,
				depthTexture.isMultisampled() ? GL_TEXTURE_2D_MULTISAMPLE : GL_TEXTURE_2D, depthTexture.getId(), 0);
		checkCompleteness(GL_FRAMEBUFFER);
		rebind();
	}
	
	private static void checkCompleteness(int boundingPoint) {
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new IllegalStateException("Incomplete frame buffer " + glCheckFramebufferStatus(GL_FRAMEBUFFER));
	}
	
	public int[] readColorAttachment(int attachmentId, int[] outBuffer) {
		int w = colorAttachments[attachmentId].getWidth();
		int h = colorAttachments[attachmentId].getHeight();
		if(outBuffer.length != w*h)
			throw new IllegalArgumentException("Invalid buffer size, got " + outBuffer.length + " expected " + w*h);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, id);
		glReadPixels(0, 0, w, h, GL_BGRA, GL_UNSIGNED_BYTE, outBuffer);
		return outBuffer;
	}
	
	public void blitMSAAToMainBuffer() {
		glBindFramebuffer(GL_READ_FRAMEBUFFER, id);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
		glBlitFramebuffer(
				0, 0, colorAttachments[0].getWidth(), colorAttachments[0].getHeight(),
				0, 0, GLWindow.getWinWidth(), GLWindow.getWinHeight(),
				GL_COLOR_BUFFER_BIT, GL_NEAREST);
		rebind();
	}

	public void blitToBuffer(FrameBuffer dst, int srcAttachment) {
		glBindFramebuffer(GL_READ_FRAMEBUFFER, id);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, dst.id);
		glReadBuffer(GL_COLOR_ATTACHMENT0 + srcAttachment);
		glBlitFramebuffer(
				0, 0, colorAttachments[srcAttachment].getWidth(), colorAttachments[srcAttachment].getHeight(),
				0, 0, dst.colorAttachments[0].getWidth(), dst.colorAttachments[0].getHeight(),
				GL_COLOR_BUFFER_BIT, GL_NEAREST);
		checkCompleteness(GL_DRAW_FRAMEBUFFER);
		checkCompleteness(GL_READ_FRAMEBUFFER);
		rebind();
	}
	
	public void blitToBuffer(FrameBuffer dst) {
		blitToBuffer(dst, 0);
	}
	
}
