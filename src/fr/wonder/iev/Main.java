package fr.wonder.iev;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import fr.wonder.commons.files.FilesUtils;
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

	private static final String imagePath = "image3.jpg";
	private static final String spritemapPath = "sprites.png";
	private static final String spritemapInfo = "sprites.txt";
	private static final int imgWidth = 250, imgHeight = 250;
	
	private static Texture targetTexture, spriteMapTexture;
	private static FrameBuffer imgFrameBuffer;

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
			{ -1, -1 },
			{ +1, -1 },
			{ +1, +1 },
			{ -1, +1 },
	};
	
	private static float[][] SPRITES_VERTICES;
	
	private static final int BINDING_TARGET_TEXTURE = 0, BINDING_CURRENT_TEXTURE = 1, BINDING_SPRITEMAP_TEXTURE = 2;
	private static final int BINDING_MEAN_OUT = 0, BINDING_EVAL_OUT = 1;
	
	private static void loadGL() throws IOException {
		System.out.println("Loading GL");
		
		System.out.println("Loading textures");
		
		targetTexture    = Texture.loadTexture(new File(imagePath));
		spriteMapTexture = Texture.loadTexture(new File(spritemapPath));
		
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
		
		imgFrameBuffer = new FrameBuffer(imgWidth, imgHeight, false);

		System.out.println("Loading shaders");
		
		meanShader = new ShaderProgram(true,
				new Shader("/mean.vs", Shader.ShaderType.VERTEX),
				new Shader("/mean.fs", Shader.ShaderType.FRAGMENT));
		meanShader.bind();
		meanShader.setUniform1i("u_targetTexture", BINDING_TARGET_TEXTURE);
		meanShader.setUniform1i("u_spriteMap", BINDING_SPRITEMAP_TEXTURE);
		meanShader.setUniform2f("u_resolution", imgWidth, imgHeight);
		
		shapeShader = new ShaderProgram(true,
				new Shader("/shape.vs", Shader.ShaderType.VERTEX),
				new Shader("/shape.fs", Shader.ShaderType.FRAGMENT));
		shapeShader.bind();
		shapeShader.setUniform1i("u_spriteMap", BINDING_SPRITEMAP_TEXTURE);
		
		evalShader = new ShaderProgram(true,
				new Shader("/eval.vs", Shader.ShaderType.VERTEX),
				new Shader("/eval.fs", Shader.ShaderType.FRAGMENT));
		evalShader.bind();
		evalShader.setUniform1i("u_targetTexture", BINDING_TARGET_TEXTURE);
		evalShader.setUniform1i("u_currentTexture", BINDING_CURRENT_TEXTURE);
		evalShader.setUniform1i("u_spriteMap", BINDING_SPRITEMAP_TEXTURE);
		evalShader.setUniform2f("u_resolution", imgWidth, imgHeight);
		
		System.out.println("Generating VAOs");
		
		VertexBufferLayout meanBufferLayout = new VertexBufferLayout()
				.addFloats(2) // i_vertex
				.addFloats(2) // i_position
				.addFloats(4) // i_textureCoords
				.addFloats(1) // i_scale
				.addFloats(1) // i_rotation
				;
		meanVertexBuffer = VertexBuffer.emptyBuffer();
		meanVertexBuffer.bind();
		meanVertexBuffer.setData(GLUtils.createBuffer((2+2+4+1+1)*4*4*EvolutionGeneration.BATCH_SIZE));
		meanVAO = new VertexArray();
		meanVAO.setIndices(batchIndexBuffer);
		meanVAO.setBuffer(meanVertexBuffer, meanBufferLayout);
		VertexArray.unbind();
		
		VertexBufferLayout shapeBufferLayout = new VertexBufferLayout()
				.addFloats(2) // i_vertex
				.addFloats(2) // i_position
				.addFloats(4) // i_textureCoords
				.addFloats(1) // i_scale
				.addFloats(1) // i_rotation
				.addFloats(3) // i_color
				;
		shapeVertexBuffer = VertexBuffer.emptyBuffer();
		shapeVertexBuffer.bind();
		shapeVertexBuffer.setData(GLUtils.createBuffer((2+2+4+1+1+(3+1))*4*4));
		shapeVAO = new VertexArray();
		shapeVAO.setIndices(singleQuadIndexBuffer);
		shapeVAO.setBuffer(shapeVertexBuffer, shapeBufferLayout);
		VertexArray.unbind();
		
		VertexBufferLayout evalBufferLayout = new VertexBufferLayout()
				.addFloats(2) // i_vertex
				.addFloats(2) // i_position
				.addFloats(4) // i_textureCoords
				.addFloats(1) // i_scale
				.addFloats(1) // i_rotation
				.addFloats(3) // i_color
				;
		evalVertexBuffer = VertexBuffer.emptyBuffer();
		evalVertexBuffer.bind();
		evalVertexBuffer.setData(GLUtils.createBuffer((2+2+4+1+1+(3+1))*4*4*EvolutionGeneration.BATCH_SIZE));
		evalVAO = new VertexArray();
		evalVAO.setIndices(batchIndexBuffer);
		evalVAO.setBuffer(evalVertexBuffer, evalBufferLayout);
		VertexArray.unbind();
		
		System.out.println("Binding buffers");

		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, BINDING_MEAN_OUT, meanOutBuffer);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, BINDING_EVAL_OUT, evalOutBuffer);
		imgFrameBuffer.bindTexture(BINDING_CURRENT_TEXTURE);
		targetTexture.bind(BINDING_TARGET_TEXTURE);
		spriteMapTexture.bind(BINDING_SPRITEMAP_TEXTURE);
	}
	
	private static void loadSpritemapVertices() throws IOException {
		List<float[]> vertices = new ArrayList<>();
		
		String info = FilesUtils.read(new File(spritemapInfo));
		
		try {
			for(String imageVertices : info.split(";")) {
				String[] parts = imageVertices.split(":");
				vertices.add(new float[] {
						Float.parseFloat(parts[0]),
						Float.parseFloat(parts[1]),
						Float.parseFloat(parts[2]),
						Float.parseFloat(parts[3]),
				});
			}
		} catch (NumberFormatException e) {
			throw new IOException("Invalid sprite info format");
		}
		
		SPRITES_VERTICES = vertices.toArray(float[][]::new);
	}
	
	private static ByteBuffer createIndividualsIndexBuffer(List<Individual> individuals, boolean includeColor) {
		Assertions.assertTrue(individuals.size() == EvolutionGeneration.BATCH_SIZE);
		
		int vertexSize = 2+2+4+1+1;
		if(includeColor) vertexSize += 3;
		ByteBuffer vertexData = GLUtils.createBuffer(vertexSize * 4 * 4 * EvolutionGeneration.BATCH_SIZE);
		
		for(int i = 0; i < EvolutionGeneration.BATCH_SIZE; i++) {
			Individual individual = individuals.get(i);
			Color color = individual.getColor();
			float[] textureVertices = SPRITES_VERTICES[individual.textureIndex];
			for(float[] vertex : QUAD_VERTICES) {
				// i_vertex
				vertexData.putFloat(vertex[0]);
				vertexData.putFloat(vertex[1]);
				// i_position
				vertexData.putFloat(individual.transform.translation.x);
				vertexData.putFloat(individual.transform.translation.y);
				// i_textureCoords
				vertexData.putFloat(textureVertices[0]); // x
				vertexData.putFloat(textureVertices[1]); // y
				vertexData.putFloat(textureVertices[2]); // width
				vertexData.putFloat(textureVertices[3]); // height
				// i_scale
				vertexData.putFloat(individual.transform.scale);
				// i_rotation
				vertexData.putFloat(individual.transform.rotation);
				if(!includeColor)
					continue;
				// i_color
				vertexData.putFloat(color.r);
				vertexData.putFloat(color.g);
				vertexData.putFloat(color.b);
			}
		}
		vertexData.position(0);
		
		return vertexData;
	}
	
	private static Color[] dispatchMeanCompute(List<Individual> individuals) {
		ByteBuffer meanInData = createIndividualsIndexBuffer(individuals, false);
		Color[] colors = new Color[EvolutionGeneration.BATCH_SIZE];
		
		glBindBuffer   (GL_SHADER_STORAGE_BUFFER, meanOutBuffer);
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, meanOutEmptyData);
		glBindBuffer   (GL_SHADER_STORAGE_BUFFER, meanVertexBuffer.getBufferId());
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, meanInData);
		imgFrameBuffer.bind();
		meanVAO.bind();
		meanShader.bind();
		
		glDrawElements(GL_TRIANGLES, 6*EvolutionGeneration.BATCH_SIZE, GL_UNSIGNED_INT, NULL);
		meanOutData.clear();
		glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
		
		glBindBuffer      (GL_SHADER_STORAGE_BUFFER, meanOutBuffer);
		glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, meanOutData);
		
		for(int i = 0; i < EvolutionGeneration.BATCH_SIZE; i++) {
			int pixels = meanOutData.getInt();
			int red = meanOutData.getInt();
			int green = meanOutData.getInt();
			int blue = meanOutData.getInt();
			float px = pixels;
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
		
		ByteBuffer evalInData = createIndividualsIndexBuffer(individuals, true);
		int[] scores = new int[EvolutionGeneration.BATCH_SIZE];
		
		glBindBuffer   (GL_SHADER_STORAGE_BUFFER, evalOutBuffer);
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, evalOutEmptyData);
		glBindBuffer   (GL_SHADER_STORAGE_BUFFER, evalVertexBuffer.getBufferId());
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, evalInData);
		imgFrameBuffer.bind();
		evalVAO.bind();
		evalShader.bind();
		
		glDrawElements(GL_TRIANGLES, 6*EvolutionGeneration.BATCH_SIZE, GL_UNSIGNED_INT, NULL);
		evalOutData.clear();
		glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
		
		glBindBuffer      (GL_SHADER_STORAGE_BUFFER, evalOutBuffer);
		glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, evalOutData);
		
		for(int i = 0; i < EvolutionGeneration.BATCH_SIZE; i++) {
			int score = evalOutData.getInt();
			scores[i] = score;
//			System.out.println(
//					"Shape " + i +
//					"\n  score     =" + score
//			);
		}
		
		return scores;
	}
	
	private static void drawShape(Individual individual) {
		drawShape(individual.textureIndex, individual.transform, individual.getColor());
	}
	
	private static void drawShape(int textureIndex, Transform transform, Color color) {
		int shapeInSize = (2+2+4+1+1+(3+1))*4*4;
		ByteBuffer shapeInData = ByteBuffer.allocateDirect(shapeInSize);
		shapeInData.order(ByteOrder.LITTLE_ENDIAN);
		for(float[] vertex : QUAD_VERTICES) {
			// i_vertex
			shapeInData.putFloat(vertex[0]);
			shapeInData.putFloat(vertex[1]);
			// i_position
			shapeInData.putFloat(transform.translation.x);
			shapeInData.putFloat(transform.translation.y);
			// i_textureCoords
			shapeInData.putFloat(SPRITES_VERTICES[textureIndex][0]); // x
			shapeInData.putFloat(SPRITES_VERTICES[textureIndex][1]); // y
			shapeInData.putFloat(SPRITES_VERTICES[textureIndex][2]); // width
			shapeInData.putFloat(SPRITES_VERTICES[textureIndex][3]); // height
			// i_scale
			shapeInData.putFloat(transform.scale);
			// i_rotation
			shapeInData.putFloat(transform.rotation);
			// i_color
			shapeInData.putFloat(color.r);
			shapeInData.putFloat(color.g);
			shapeInData.putFloat(color.b);
//			shapeInData.position(shapeInData.position()+4); // padding
		}
		shapeInData.position(0);
		
		glBindBuffer   (GL_SHADER_STORAGE_BUFFER, shapeVertexBuffer.getBufferId());
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, shapeInData);
		imgFrameBuffer.bind();
		shapeVAO.bind();
		shapeShader.bind();
		
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, NULL);
	}
	
	public static void main(String[] args) throws IOException {
		GLWindow.createWindow(700, 700);
		GLWindow.sendFrame();
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		loadGL();
		loadSpritemapVertices();
		
		try {
			for(int gen = 0; ; gen++) {
				System.out.println("Running generation " + gen);
				EvolutionGeneration generation = new EvolutionGeneration();
				generation.generateInitialPopulation(SPRITES_VERTICES.length);
				for(int step = 0; step < EvolutionGeneration.STEPS; step++) {
//					System.out.println("Running step " + step);
					List<Individual> individuals = generation.getIndividuals();
					Color[] colors = dispatchMeanCompute(individuals);
					for(int i = 0; i < individuals.size(); i++)
						individuals.get(i).setColor(colors[i]);
					int[] scores = dispatchEvalCompute(individuals);
					for(int i = 0; i < individuals.size(); i++)
						individuals.get(i).setScore(scores[i]);
					generation.rankIndividuals();
					generation.keepBestIndividuals();
					generation.reproduceIndividuals();
				}
				System.out.println(generation.getFirstIndividual());
				drawShape(generation.getFirstIndividual());
				
				FrameBuffer.blitMSAAToMainBuffer(imgFrameBuffer, GLWindow.getWinWidth(), GLWindow.getWinHeight());
				GLWindow.sendFrame();
				
				if(GLWindow.shouldDispose() || System.in.available() > 0)
					break;
			}
		} finally {
			GLWindow.dispose();
		}
	}
	
}
