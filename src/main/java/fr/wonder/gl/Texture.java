package fr.wonder.gl;


import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.GL_DEPTH24_STENCIL8;
import static org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE;
import static org.lwjgl.opengl.GL43.glTexStorage2DMultisample;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

public class Texture {

	private final int width, height;
	private int id;
	private final boolean hasMSAA;
	
	private Texture(int id, int width, int height, boolean hasMSAA) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.hasMSAA = hasMSAA;
	}
	
	public static Texture loadTexture(String path) throws IOException {
		try (InputStream is = Texture.class.getResourceAsStream(path)) {
			if (is == null)
				throw new IOException("Resource " + path + " does not exist");
			return loadTexture(is);
		}
	}
	
	public static Texture loadTexture(File file) throws IOException {
		try (InputStream is = new FileInputStream(file)) {
			return loadTexture(is);
		}
	}
	
	public static Texture loadTexture(InputStream stream) throws IOException {
		return loadTexture(ImageIO.read(stream));
	}
		
	public static Texture loadTexture(BufferedImage image) {
		int id, width, height;

		width = image.getWidth();
		height = image.getHeight();

		int size = width * height;
		int[] data = new int[size];

		image.getRGB(0, 0, width, height, data, 0, width);

		int[] px = new int[size];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int pos = i * width + j;
				int a = (data[pos] & 0xff000000) >> 24;
				int r = (data[pos] & 0x00ff0000) >> 16;
				int g = (data[pos] & 0x0000ff00) >> 8;
				int b = (data[pos] & 0x000000ff);
				px[(height - 1 - i) * width + j] =
						a << 24 |
						b << 16 |
						g << 8 |
						r;
			}
		}

		id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, px);
		unbind();
		
		return new Texture(id, width, height, false);
	}
	
	public static Texture empty(int width, int height) {
		return fromBuffer(width, height, null);
	}
	
	public static Texture fromBuffer(int width, int height, ByteBuffer buffer) {
		int id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_FLOAT, buffer);
		glBindTexture(GL_TEXTURE_2D, 0);
		return new Texture(id, width, height, false);
	}
	
	public static Texture withMSAA(int width, int height, int msaaLevel) {
		int textureId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureId);
		glTexStorage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaLevel, GL_RGBA8, width, height, true);
		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
		return new Texture(textureId, width, height, true);
	}
	
	public static Texture standard(int width, int height) {
		int textureId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureId);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_FLOAT, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glBindTexture(GL_TEXTURE_2D, 0);
		return new Texture(textureId, width, height, false);
	}
	
	public static Texture depthMSAA(int width, int height, int msaaLevel) {
		int textureId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, textureId);
		glTexStorage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaaLevel, GL_DEPTH24_STENCIL8, width, height, true);
		glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
		return new Texture(textureId, width, height, true);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isMultisampled() {
		return hasMSAA;
	}

	public void bind(int slot) {
		glActiveTexture(GL_TEXTURE0 + slot);
		glBindTexture(GL_TEXTURE_2D, id);
	}
	
	public static void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void dispose() {
		glDeleteTextures(id);
		id = 0;
	}

	public void writeToFile(File file) throws IOException {
		int[] pixels = new int[width*height];
		int boundTexture = glGetInteger(GL_TEXTURE_BINDING_2D);
		glBindTexture(GL_TEXTURE_2D, id);
		glGetTexImage(GL_TEXTURE_2D, 0, GL_BGRA, GL_UNSIGNED_BYTE, pixels);
		glBindTexture(GL_TEXTURE_2D, boundTexture);
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		img.setRGB(0, 0, width, height, pixels, width*height-width, -width);
		ImageIO.write(img, FilesUtils.getFileExtension(file.getName()), file);
	}
}
