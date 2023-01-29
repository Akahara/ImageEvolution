package fr.wonder.iev;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGetBufferSubData;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import fr.wonder.commons.math.Mathf;
import fr.wonder.commons.systems.process.argparser.ArgParser;
import fr.wonder.commons.systems.process.argparser.EntryPoint;
import fr.wonder.commons.systems.process.argparser.Option;
import fr.wonder.commons.utils.Assertions;
import fr.wonder.gl.Color;
import fr.wonder.gl.FrameBuffer;
import fr.wonder.gl.GLUtils;
import fr.wonder.gl.GLWindow;
import fr.wonder.gl.IndexBuffer;
import fr.wonder.gl.Shader;
import fr.wonder.gl.ShaderProgram;
import fr.wonder.gl.Texture;
import fr.wonder.gl.VertexArray;
import fr.wonder.gl.VertexBuffer;
import fr.wonder.gl.VertexBufferLayout;

public class Main {

	private static Texture targetTexture;
	private static Texture[] spriteTextures;

	private static Texture generatedImage;
	private static FrameBuffer imgFrameBuffer;
	private static ByteBuffer individualsInBuffer;
	private static int individualSize;
	
	private static VertexArray meanVAO;
	private static VertexBuffer meanVertexBuffer;
	private static ShaderProgram meanShader;
	private static ByteBuffer meanOutEmptyData, meanOutData;
	private static int meanOutBuffer;
	
	private static VertexArray evalVAO;
	private static VertexBuffer evalVertexBuffer;
	private static ShaderProgram evalShader;
	private static ByteBuffer evalOutEmptyData, evalOutData;
	private static int evalOutBuffer;
	
	private static VertexArray shapeVAO;
	private static VertexBuffer shapeVertexBuffer;
	private static ShaderProgram shapeShader;
	
	private static final float[][] QUAD_VERTICES = {
			{ -1,-1 },
			{ +1,-1 },
			{ +1,+1 },
			{ -1,+1 },
	};
	
	private static final int BINDING_TARGET_TEXTURE = 0, BINDING_CURRENT_TEXTURE = 1, BINDING_SPRITEMAP_TEXTURE = 2;
	private static final int BINDING_MEAN_OUT = 0, BINDING_EVAL_OUT = 1;
	
	private static void loadGL(int imgWidth, int imgHeight) throws IOException {
		System.out.println("Loading GL");
		
		System.out.println("Generating buffers");
		
		meanOutBuffer = glGenBuffers();
		int meanOutSize = EvolutionGeneration.BATCH_SIZE*4*4; // total pixel count // total red // total green // total blue
		meanOutEmptyData = GLUtils.createBuffer(meanOutSize);
		meanOutData      = GLUtils.createBuffer(meanOutSize);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, meanOutBuffer);
		glBufferData(GL_SHADER_STORAGE_BUFFER, meanOutEmptyData, GL_DYNAMIC_DRAW);
		
		evalOutBuffer = glGenBuffers();
		int evalOutSize = EvolutionGeneration.BATCH_SIZE*4; // total improvement
		evalOutEmptyData = GLUtils.createBuffer(evalOutSize);
		evalOutData      = GLUtils.createBuffer(evalOutSize);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, evalOutBuffer);
		glBufferData(GL_SHADER_STORAGE_BUFFER, evalOutEmptyData, GL_DYNAMIC_DRAW);
		
		IndexBuffer batchIndexBuffer      = GLUtils.createQuadIndexBuffer(EvolutionGeneration.BATCH_SIZE);
		IndexBuffer singleQuadIndexBuffer = GLUtils.createQuadIndexBuffer(1);
		
		imgFrameBuffer = new FrameBuffer();
		imgFrameBuffer.setColorAttachment(0, generatedImage);
		EvolutionGeneration.setTargetAspectRatio(imgWidth, imgHeight);

		System.out.println("Loading shaders");
		
		meanShader = new ShaderProgram(
				Shader.fromResources("/shape.vs"),
				Shader.fromResources("/mean.fs"));
		meanShader.bind();
		meanShader.setUniform1i("u_targetTexture", BINDING_TARGET_TEXTURE);
		meanShader.setUniform1i("u_spriteMap", BINDING_SPRITEMAP_TEXTURE);
		meanShader.setUniform2f("u_resolution", imgWidth, imgHeight);
		
		shapeShader = new ShaderProgram(
				Shader.fromResources("/shape.vs"),
				Shader.fromResources("/shape.fs"));
		shapeShader.bind();
		shapeShader.setUniform1i("u_spriteMap", BINDING_SPRITEMAP_TEXTURE);
		shapeShader.setUniform1i("u_currentTexture", BINDING_CURRENT_TEXTURE);
		shapeShader.setUniform2f("u_resolution", imgWidth, imgHeight);
		
		evalShader = new ShaderProgram(
				Shader.fromResources("/shape.vs"),
				Shader.fromResources("/eval.fs"));
		evalShader.bind();
		evalShader.setUniform1i("u_targetTexture", BINDING_TARGET_TEXTURE);
		evalShader.setUniform1i("u_currentTexture", BINDING_CURRENT_TEXTURE);
		evalShader.setUniform1i("u_spriteMap", BINDING_SPRITEMAP_TEXTURE);
		evalShader.setUniform2f("u_resolution", imgWidth, imgHeight);
		
		System.out.println("Generating VAOs");
		
		VertexBufferLayout layout = new VertexBufferLayout()
				.addFloats(2) // i_position
				.addFloats(3) // i_color
				;
		individualSize = (2+3)*4*4;
		
		individualsInBuffer = GLUtils.createBuffer(individualSize*EvolutionGeneration.BATCH_SIZE);
		
		meanVertexBuffer = VertexBuffer.emptyBuffer();
		meanVertexBuffer.bind();
		meanVertexBuffer.setData(individualsInBuffer);
		meanVAO = new VertexArray();
		meanVAO.setIndices(batchIndexBuffer);
		meanVAO.setBuffer(meanVertexBuffer, layout);
		VertexArray.unbind();
		
		shapeVertexBuffer = VertexBuffer.emptyBuffer();
		shapeVertexBuffer.bind();
		shapeVertexBuffer.setData(GLUtils.createBuffer(individualSize));
		shapeVAO = new VertexArray();
		shapeVAO.setIndices(singleQuadIndexBuffer);
		shapeVAO.setBuffer(shapeVertexBuffer, layout);
		VertexArray.unbind();
		
		evalVertexBuffer = VertexBuffer.emptyBuffer();
		evalVertexBuffer.bind();
		evalVertexBuffer.setData(individualsInBuffer);
		evalVAO = new VertexArray();
		evalVAO.setIndices(batchIndexBuffer);
		evalVAO.setBuffer(evalVertexBuffer, layout);
		VertexArray.unbind();
		
		System.out.println("Binding buffers");

		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, BINDING_MEAN_OUT, meanOutBuffer);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, BINDING_EVAL_OUT, evalOutBuffer);
		generatedImage.bind(BINDING_CURRENT_TEXTURE);
		targetTexture.bind(BINDING_TARGET_TEXTURE);
		for(int i = 0; i < spriteTextures.length; i++) {
			spriteTextures[i].bind(BINDING_SPRITEMAP_TEXTURE + i);
		}
		
		imgFrameBuffer.bind();
	}
	
	private static ByteBuffer createIndividualsVBO(List<Individual> individuals) {
		Assertions.assertTrue(individuals.size() == EvolutionGeneration.BATCH_SIZE);
		
		individualsInBuffer.position(0);
		for(int i = 0; i < EvolutionGeneration.BATCH_SIZE; i++) {
			Individual individual = individuals.get(i);
			putIndividual(individual, individualsInBuffer);
		}
		individualsInBuffer.position(0);
		
		return individualsInBuffer;
	}
	
	private static Color[] dispatchMeanCompute(List<Individual> individuals) {
		ByteBuffer meanInData = createIndividualsVBO(individuals);
		Color[] colors = new Color[EvolutionGeneration.BATCH_SIZE];
		
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, meanOutBuffer);
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, meanOutEmptyData);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, meanVertexBuffer.getBufferId());
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, meanInData);
		meanVAO.bind();
		meanShader.bind();
		
		glDrawElements(GL_TRIANGLES, 6*EvolutionGeneration.BATCH_SIZE, GL_UNSIGNED_INT, NULL);
		meanOutData.clear();
		glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
		
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, meanOutBuffer);
		glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, meanOutData);
		
		for(int i = 0; i < EvolutionGeneration.BATCH_SIZE; i++) {
			int pixels = meanOutData.getInt();
			int red = meanOutData.getInt();
			int green = meanOutData.getInt();
			int blue = meanOutData.getInt();
			float px = pixels;
			if(px == 0) px = 1;
			colors[i] = new Color(red/px, green/px, blue/px);
//			System.out.println(
//					"Shape " + i +
//					"\n  pixels    =" + pixels +
//					"\n  totalRed  =" + red +
//					"\n  totalGreen=" + green +
//					"\n  totalBlue =" + blue +
//					"\n  mean      =" + new Color(red/px, green/px, blue/px)
//			);
		}
		
		return colors;
	}
	
	private static int[] dispatchEvalCompute(List<Individual> individuals) {
		Assertions.assertTrue(individuals.size() == EvolutionGeneration.BATCH_SIZE);
		
		ByteBuffer evalInData = createIndividualsVBO(individuals);
		int[] scores = new int[EvolutionGeneration.BATCH_SIZE];
		
		glBindBuffer   (GL_SHADER_STORAGE_BUFFER, evalOutBuffer);
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, evalOutEmptyData);
		glBindBuffer   (GL_SHADER_STORAGE_BUFFER, evalVertexBuffer.getBufferId());
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, evalInData);
		evalVAO.bind();
		evalShader.bind();

//		spriteTextures[0].bind(BINDING_SPRITEMAP_TEXTURE);
		glDrawElements(GL_TRIANGLES, 6*EvolutionGeneration.BATCH_SIZE, GL_UNSIGNED_INT, NULL);
		evalOutData.clear();
		glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
		
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, evalOutBuffer);
		glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, evalOutData);
		
		for(int i = 0; i < EvolutionGeneration.BATCH_SIZE; i++) {
			int score = evalOutData.getInt();
			scores[i] = score;
		}
		
		return scores;
	}
	
	private static void drawShape(Individual individual) {
		individualsInBuffer.position(individualsInBuffer.capacity() - individualSize); // use only the last <individualSize> bytes
		putIndividual(individual, individualsInBuffer);
		individualsInBuffer.position(individualsInBuffer.capacity() - individualSize);
		
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, shapeVertexBuffer.getBufferId());
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, individualsInBuffer);
		shapeVAO.bind();
		shapeShader.bind();
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, NULL);
	}
	
	private static void putIndividual(Individual individual, ByteBuffer buffer) {
		float c = Mathf.cos(individual.transform.rotation);
		float s = Mathf.sin(individual.transform.rotation);
		
		for(float[] vertex : QUAD_VERTICES) {
			float vx = (c*vertex[0]-s*vertex[1])*individual.transform.scale + individual.transform.translation.x;
			float vy = (s*vertex[0]+c*vertex[1])*individual.transform.scale + individual.transform.translation.y;
			// i_position
			buffer.putFloat(vx*generatedImage.getHeight()/generatedImage.getWidth());
			buffer.putFloat(vy);
			// i_color
			if(individual.color == null) {
				buffer.putFloat(0.f);
				buffer.putFloat(0.f);
				buffer.putFloat(0.f);
			} else {
				buffer.putFloat(individual.color.r);
				buffer.putFloat(individual.color.g);
				buffer.putFloat(individual.color.b);
			}
		}
	}
	
	private static Texture whiteTexture() {
		return Texture.fromBuffer(1, 1,
				GLUtils.createBuffer(4*4)
				.putFloat(1)
				.putFloat(1)
				.putFloat(1)
				.putFloat(1)
				.position(0));
	}
	
	private static Texture[] loadSprites(File spriteFile) throws IOException {
		if(!spriteFile.exists())
			throw new IOException("No sprite file '" + spriteFile + "'");
		if(spriteFile.isFile())
			return new Texture[] { Texture.loadTexture(spriteFile) };
		List<Texture> sprites = new ArrayList<>();
		for(File f : spriteFile.listFiles()) {
			if(!f.getName().startsWith(".") && !f.getName().startsWith("_"))
				sprites.add(Texture.loadTexture(f));
		}
		return sprites.toArray(Texture[]::new);
	}
	
	private static Individual runGeneration(int steps) {
		EvolutionGeneration generation = new EvolutionGeneration(spriteTextures.length);
		generation.generateInitialPopulation();
		for(int step = 0; step < steps; step++) {
			List<Individual> individuals = generation.getIndividuals();
			Color[] colors = dispatchMeanCompute(individuals);
			for(int i = 0; i < individuals.size(); i++)
				individuals.get(i).color = colors[i];
			int[] scores = dispatchEvalCompute(individuals);
			for(int i = 0; i < individuals.size(); i++)
				individuals.get(i).score = scores[i];
			generation.rankIndividuals();
			generation.keepBestIndividuals();
			generation.reproduceIndividuals();
		}
		return generation.getFirstIndividual();
	}
	
	private static void exportFrame(File outputFile) {
		System.out.println("Exporting frame " + outputFile);
		try {
			generatedImage.writeToFile(outputFile);
		} catch (IOException e) {
			System.err.println("Could not write frame: " + e);
		}
	}
	
	public static class Options {
		
		@Option(name = "--resume", shortand = "-r")
		public File resumeFile = null;
		@Option(name = "--no-display", shortand = "-n")
		public boolean withDisplay = true;
		@Option(name = "--export-every", shortand = "-d")
		public int exportEveryNObjects = 500;
		@Option(name = "--export-format", shortand = "-e")
		public String exportFormat = "out_{}.png";
		@Option(name = "--width", shortand = "-w")
		public int width = -1;
		@Option(name = "--height", shortand = "-h")
		public int height = -1;
		@Option(name = "--sprite", shortand = "-s")
		public File spriteFile = null;
		@Option(name = "--verbose", shortand = "-v")
		public boolean verbose;
		@Option(name = "--generations", shortand = "-g")
		public int generationCount = Integer.MAX_VALUE;
		@Option(name = "--output", shortand = "-o")
		public File outputFile = new File("out.png");
	}
	
	private static void checkOptions(Options options, File target) throws IOException {
		BufferedImage img = ImageIO.read(target);
		if(options.width == -1 && options.height == -1) {
			options.width = img.getWidth();
			options.height = img.getHeight();
		} else if(options.width == -1) {
			options.width = img.getWidth() * options.height / img.getHeight();
		} else if(options.height == -1) {
			options.height = img.getHeight() * options.width / img.getWidth();
		}
		
		if(!options.exportFormat.contains("{}"))
			throw new IOException("Invalid export format, include '{}' in the file name");
		
		if(options.width <= 0 || options.height <= 0)
			throw new IOException("Invalid image size: " + options.width + "x" + options.height);
		
		System.out.println(String.format("In %dx%d  Out %dx%d", img.getWidth(), img.getHeight(), options.width, options.height));
	}
	
	@EntryPoint(path = ":root")
	public static void run(Options options, File target) throws IOException {
		checkOptions(options, target);
		
		GLWindow.createWindow("ImageEvolution", options.width, options.height);
		
		System.out.println("Loading textures");

		generatedImage = options.resumeFile == null ? Texture.standard(options.width, options.height) : Texture.loadTexture(options.resumeFile);
		targetTexture = Texture.loadTexture(target);
		spriteTextures = options.spriteFile == null ? new Texture[] { whiteTexture() } : loadSprites(options.spriteFile);
		
		if(options.withDisplay) {
			GLWindow.show();
			GLWindow.sendFrame();
		}

		loadGL(options.width, options.height);
		
		try {
			for(int gen = 0; gen < options.generationCount; gen++) {
				Individual best = runGeneration((gen>25 || options.resumeFile!=null) ? EvolutionGeneration.STEPS : 2);
				drawShape(best);
				
				if(options.verbose) {
					System.out.println("Ran generation " + gen);
					System.out.println(best);
				} else if(gen % 100 == 0) {
					System.out.println("Ran generation " + gen);
				}
				
				if(options.withDisplay) {
					imgFrameBuffer.blitMSAAToMainBuffer();
					GLWindow.sendFrame();
				}
				
				if(options.exportEveryNObjects > 0 && gen > 10 && gen % options.exportEveryNObjects == 0)
					exportFrame(new File(options.exportFormat.replaceFirst("\\{\\}", ""+gen)));
				
				if(GLWindow.shouldDispose() || System.in.available() > 0)
					break;
			}
			
		} finally {
			exportFrame(options.outputFile);
			GLWindow.dispose();
		}
	}
	
	public static void main(String[] args) throws IOException {
//		args = new String[] { "escher.png", "-w", "1000", "-n" };
		ArgParser.runHere(args);
	}
	
}
