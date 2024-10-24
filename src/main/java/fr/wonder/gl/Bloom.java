package fr.wonder.gl;


import fr.wonder.iev.Mathr;

public class Bloom implements Disposable {
	
	private final ShaderProgram firstPassShader = GLUtils.shaderFromResources("/shaders/blit.vs", "/shaders/bloom/first_pass.fs");
	private final ShaderProgram downscaleShader = GLUtils.shaderFromResources("/shaders/blit.vs", "/shaders/bloom/downscale.fs");
	private final ShaderProgram upscaleShader   = GLUtils.shaderFromResources("/shaders/blit.vs", "/shaders/bloom/upscale.fs");
	private final ShaderProgram finalPassShader = GLUtils.shaderFromResources("/shaders/blit.vs", "/shaders/bloom/final_pass.fs");
	private final FrameBuffer fbo;
	private final VertexArray vao;
	private final VertexBuffer vbo;
	private final IndexBuffer ibo;
	private Texture textureOrigin, textureBloomOrigin, textureBloomDestination;
	private Texture[] texturesA, texturesB;
	
	public Bloom() {
		VertexArray.unbind();
		vbo = new VertexBuffer();
		ibo = GLUtils.createQuadIndexBuffer(1);
		vao = new VertexArray().setBuffer(vbo, new VertexBufferLayout()).setIndices(ibo);
		fbo = new FrameBuffer();
		upscaleShader.bind();
		upscaleShader.setUniform1f("u_filterRadius", .005f);
		upscaleShader.setUniform1i("u_upscaled", 1);
		finalPassShader.bind();
		finalPassShader.setUniform1i("u_bloomTexture", 1);
		setBloomStrength(1);
		ShaderProgram.unbind();
	}
	
	public void setBloomStrength(float strength) {
		finalPassShader.bind();
		finalPassShader.setUniform1f("u_bloomStrength", strength);
		ShaderProgram.unbind();
	}
	
	public void regenerateTextures(int originWidth, int originHeight) {
		disposeTextures();
		
		int textureCount = Math.min(Mathr.positionOfMostSignificantBit(originWidth), Mathr.positionOfMostSignificantBit(originHeight));
		texturesA = new Texture[textureCount];
		texturesB = new Texture[textureCount-2];
		textureOrigin = Texture.standard(originWidth, originHeight);
		textureBloomOrigin = Texture.standard(originWidth, originHeight);
		textureBloomDestination = Texture.standard(originWidth, originHeight);
		texturesA[0] = Texture.standard(originWidth, originHeight);
		for(int i = 1; i < textureCount-1; i++) {
			originWidth /= 2;
			originHeight /= 2;
			texturesA[i] = Texture.standard(originWidth, originHeight);
			texturesB[i-1] = Texture.standard(originWidth, originHeight);
		}
		originWidth /= 2;
		originHeight /= 2;
		texturesA[textureCount-1] = Texture.standard(originWidth, originHeight);
	}
	
	public void applyBloom(FrameBuffer target) {
		fbo.setColorAttachment(0, textureOrigin);
		target.blitToBuffer(fbo, 0);
		fbo.setColorAttachment(0, textureBloomOrigin);
		target.blitToBuffer(fbo, 1);
		target.unbind();
		
//		ProcessUtils.silence(() -> textureOrigin.writeToFile(new File("foo-O.png")));
//		System.exit(0);
		
		vao.bind();
		fbo.bind();

		firstPassShader.bind();
		textureBloomOrigin.bind(0);
		fbo.setColorAttachment(0, texturesA[0]);
		GLUtils.dcQuads(1);
//		ProcessUtils.silence(() -> textureOrigin.writeToFile(new File("foo-O.png")));
//		ProcessUtils.silence(() -> texturesA[0].writeToFile(new File("foo-0.png")));
		
		downscaleShader.bind();
		for(int i = 1; i < texturesA.length; i++) {
			texturesA[i-1].bind(0);
			downscaleShader.setUniform2f("u_srcResolution", texturesA[i-1].getWidth(), texturesA[i-1].getHeight());
			fbo.setColorAttachment(0, texturesA[i]);
			GLUtils.dcQuads(1);
//			int ii = i;
//			ProcessUtils.silence(() -> texturesA[ii].writeToFile(new File("foo-"+ii+".png")));
		}
		
		upscaleShader.bind();
		texturesA[texturesA.length-1].bind(0);
		texturesA[texturesA.length-2].bind(1);
		fbo.setColorAttachment(0, texturesB[texturesB.length-1]);
		GLUtils.dcQuads(1);
//		ProcessUtils.silence(() -> texturesB[texturesB.length-1].writeToFile(new File("foo-b"+(texturesB.length-1)+".png")));
		for(int i = texturesB.length-2; i >= 0; i--) {
			fbo.setColorAttachment(0, texturesB[i]);
//			fbo.bind();
			texturesB[i+1].bind(0);
			texturesA[i+1].bind(1);
			GLUtils.dcQuads(1);
//			int ii = i;
//			ProcessUtils.silence(() -> texturesB[ii].writeToFile(new File("foo-b"+ii+".png")));
		}
//		System.exit(0);

		fbo.setColorAttachment(0, textureBloomDestination);
		texturesB[0].bind(0);
		texturesA[0].bind(1);
		GLUtils.dcQuads(1);
		
		fbo.unbind();
		
//		ProcessUtils.silence(() -> textureOrigin.writeToFile(new File("foo-O.png")));
//		System.exit(0);
		
		finalPassShader.bind();
		textureOrigin.bind(0);
		textureBloomDestination.bind(1);
		target.bind();
		GLUtils.dcQuads(1);
	}
	
	@Override
	public void dispose() {
		vao.dispose();
		vbo.dispose();
		ibo.dispose();
		fbo.dispose();
		disposeTextures();
		firstPassShader.dispose();
		downscaleShader.dispose();
		upscaleShader.dispose();
		finalPassShader.dispose();
	}
	
	private void disposeTextures() {
		if(textureOrigin != null) {
			for(Texture t : texturesA)
				t.dispose();
			for(Texture t : texturesB)
				t.dispose();
			textureOrigin.dispose();
			textureBloomOrigin.dispose();
			textureBloomDestination.dispose();
		}
	}
	
}
