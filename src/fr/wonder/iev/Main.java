package fr.wonder.iev;

import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import fr.wonder.commons.math.Mathf;
import fr.wonder.commons.math.Mathr;
import fr.wonder.commons.math.vectors.Vec4;

public class Main {
	
	public static void main(String[] args) throws IOException {
		GLWindow.createWindow(200, 200);
		
		int imgWidth = 60, imgHeight = 60;
		ByteBuffer targetTextureData = ByteBuffer.allocateDirect(16+4*4*imgWidth*imgHeight);
		ByteBuffer currentTextureData = ByteBuffer.allocateDirect(4*4*imgWidth*imgHeight);
		targetTextureData.order(ByteOrder.LITTLE_ENDIAN);
		currentTextureData.order(ByteOrder.LITTLE_ENDIAN);
		targetTextureData.putInt(imgWidth);
		targetTextureData.position(16);
		for(int x = 0; x < imgWidth; x++) {
			for(int y = 0; y < imgHeight; y++) {
				targetTextureData.putFloat(.5f);
				targetTextureData.putFloat(1f);
				targetTextureData.putFloat(.5f);
				targetTextureData.putFloat(1f);
				currentTextureData.putFloat(0f);
				currentTextureData.putFloat(0f);
				currentTextureData.putFloat(0f);
				currentTextureData.putFloat(1f);
			}
		}
		targetTextureData.position(0);
		currentTextureData.position(0);
		
		float[] triangleVertices = {
				-.5f, -.5f,
				.5f, .5f,
				.5f, -.5f
		};
		int[] triangleIndices = {
				0, 1, 2
		};
		
		float[] blitVertices = {
				-1, -1,
				+1, -1,
				+1, +1,
				-1, +1,
		};
		int[] blitIndices = {
				0, 1, 2, 2, 3, 0
		};
		
		final int BINDING_TARGET = 0, BINDING_CURRENT = 1, BINDING_DIFF = 2;

		int targetTextureBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, targetTextureBuffer);
		glBufferData(GL_UNIFORM_BUFFER, targetTextureData, GL_DYNAMIC_DRAW);
		glBindBufferBase(GL_UNIFORM_BUFFER, BINDING_TARGET, targetTextureBuffer); 
		
		int currentTextureBuffer = glGenBuffers();
		glBindBuffer(GL_UNIFORM_BUFFER, currentTextureBuffer);
		glBufferData(GL_UNIFORM_BUFFER, currentTextureData, GL_DYNAMIC_DRAW);
		glBindBufferBase(GL_UNIFORM_BUFFER, BINDING_CURRENT, currentTextureBuffer); 
		
		int diffBuffer = glGenBuffers();
		ByteBuffer diffBufferBuffer = ByteBuffer.allocateDirect(16);
		diffBufferBuffer.order(ByteOrder.LITTLE_ENDIAN);
		diffBufferBuffer.putInt(0);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, diffBuffer);
		glBufferData(GL_SHADER_STORAGE_BUFFER, diffBufferBuffer, GL_DYNAMIC_DRAW);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, BINDING_DIFF, diffBuffer);
		
		VertexArray triangleVAO = new VertexArray();
		triangleVAO.setBuffer(new VertexBuffer(triangleVertices), new VertexBufferLayout().addFloats(2));
		triangleVAO.setIndices(new IndexBuffer(triangleIndices));
		VertexArray.unbind();
		VertexArray blitVAO = new VertexArray();
		blitVAO.setBuffer(new VertexBuffer(blitVertices), new VertexBufferLayout().addFloats(2));
		blitVAO.setIndices(new IndexBuffer(blitIndices));
		
		ShaderProgram triangleShader = new ShaderProgram(
				new Shader("/test.vs", GL_VERTEX_SHADER),
				new Shader("/test.fs", GL_FRAGMENT_SHADER));
		ShaderProgram blitShader = new ShaderProgram(
				new Shader("/blit.vs", GL_VERTEX_SHADER),
				new Shader("/blit.fs", GL_FRAGMENT_SHADER));
		
		FrameBuffer imgFrameBuffer = new FrameBuffer(imgWidth, imgHeight, false);
		
		glClearColor(.2f, .2f, .5f, 1f);
		
		try {
			Vec4 best = Mathf.random4();
			int score = 0;
			int tries = 0;
			long startMillis = System.currentTimeMillis();
			while(true) {
				// reset the difference buffer
				glBindBuffer(GL_SHADER_STORAGE_BUFFER, diffBuffer);
				glBufferData(GL_SHADER_STORAGE_BUFFER, diffBufferBuffer, GL_DYNAMIC_DRAW);
				
				// draw a shape
				imgFrameBuffer.bind();
				triangleVAO.bind();
				triangleShader.bind();
				Vec4 c = new Vec4(best);
				c.x += Mathr.randSigned()*.2f;
				c.y += Mathr.randSigned()*.2f;
				c.z += Mathr.randSigned()*.2f;
				c.w = 1;
				triangleShader.setUniform4f("u_color", c.x, c.y, c.z, c.w);
				glClear(GL_COLOR_BUFFER_BIT);
				glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_INT, NULL);
				
				int[] difference = new int[1];
//				glBindBuffer(GL_SHADER_STORAGE_BUFFER, diffBuffer);
				glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, difference);
				System.out.println(c + " " + difference[0]);
				if(difference[0] > score) {
					best = c;
					score = difference[0];
				}

				// blit to screen
				FrameBuffer.unbind();
				glViewport(0, 0, GLWindow.winWidth, GLWindow.winHeight);
				imgFrameBuffer.bindTexture(0);
				blitVAO.bind();
				blitShader.bind();
				glClear(GL_COLOR_BUFFER_BIT);
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, NULL);
				
				GLWindow.sendFrame();
				
				if(GLWindow.shouldDispose() || System.in.available() > 0)
					break;
				tries++;
			}
			System.out.println(
					"Best: " + best +
					" - " + score +
					" (" + tries + " tries, " +
					(System.currentTimeMillis()-startMillis)/1000f + "s)");
		} finally {
			GLWindow.dispose();
		}
	}
	
}
