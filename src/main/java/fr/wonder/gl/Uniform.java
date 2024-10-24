package fr.wonder.gl;


public class Uniform {

	public final Type type;
	public final String name;
	public final int size; // length of the array, 1 if the uniform is not an array
	public final int stride; // byte offset between consecutive elements in an array, -1 if the uniform is not an array
	
	public final int blockOffset; // the offset in bytes of this uniform in its interface block, -1 if it is not in an interface block
	
	public Uniform(Type type, String name, int size, int stride, int blockOffset) {
		this.type = type;
		this.name = name;
		this.size = size;
		this.stride = stride;
		this.blockOffset = blockOffset;
	}
	
	@Override
	public String toString() {
		return type.toString().toLowerCase() + " " + name;
	}
	
	public static interface UniformFunction {
		
		public void setUniform(ShaderProgram shader, String uniformName, float[] value) throws IllegalArgumentException;
		
	}
	
	public static enum Type {
		
			FLOAT(1,
					(shader, name, value) -> { assertSize(value, 1 ); shader.setUniform1f(name, value[0]); },
					(shader, name, value) -> { assertSizeMultiple(value, 1 ); shader.setUniform1fv(name, value); }),
			VEC2 (2,
					(shader, name, value) -> { assertSize(value, 2 ); shader.setUniform2f(name, value[0], value[1]); },
					(shader, name, value) -> { assertSizeMultiple(value, 2 ); shader.setUniform2fv(name, value); }),
			VEC3 (3, 
					(shader, name, value) -> { assertSize(value, 3 ); shader.setUniform3f(name, value[0], value[1], value[2]); },
					(shader, name, value) -> { assertSizeMultiple(value, 3 ); shader.setUniform3fv(name, value); }),
			VEC4 (4,
					(shader, name, value) -> { assertSize(value, 4 ); shader.setUniform4f(name, value[0], value[1], value[2], value[3]); },
					(shader, name, value) -> { assertSize(value, 4 ); shader.setUniform4fv(name, value); }),
			MAT2 (4,
					(shader, name, value) -> { assertSize(value, 4 ); shader.setUniformMat2f(name, value); },
					(shader, name, value) -> { throw new RuntimeException(); }),
			MAT3 (9,
					(shader, name, value) -> { assertSize(value, 9 ); shader.setUniformMat3f(name, value); },
					(shader, name, value) -> { throw new RuntimeException(); }),
			MAT4 (16,
					(shader, name, value) -> { assertSize(value, 16); shader.setUniformMat4f(name, value); },
					(shader, name, value) -> { throw new RuntimeException(); }),
			SAMPLER2D(-1,
					(shader, name, value) -> { assertSize(value, 1); shader.setUniform1i(name, asInt(value[0])); },
					(shader, name, value) -> { throw new RuntimeException(); });
		
		private final int byteCount;
		private final UniformFunction uniformFunction;
		private final UniformFunction arrayFunction;
		
		Type(int byteCount, UniformFunction uniformFunction, UniformFunction arrayFunction) {
			this.byteCount = byteCount;
			this.uniformFunction = uniformFunction;
			this.arrayFunction = arrayFunction;
		}
		
		public void setUniform(ShaderProgram shader, String uniformName, float[] value) throws IllegalArgumentException {
			uniformFunction.setUniform(shader, uniformName, value);
		}
		
		public void setUniformArray(ShaderProgram shader, String uniformName, float[] value) throws IllegalArgumentException {
			arrayFunction.setUniform(shader, uniformName, value);
		}
		
		public int getSize() {
			return byteCount;
		}
		
	}
	
	private static final void assertSize(float[] array, int expectedLength) throws IllegalArgumentException {
		if(array.length != expectedLength)
			throw new IllegalArgumentException("Expected " + expectedLength + " values, got " + array.length);
	}
	
	private static final void assertSizeMultiple(float[] array, int expectedLength) throws IllegalArgumentException {
		if(array.length % expectedLength != 0)
			throw new IllegalArgumentException("Expected a multiple of " + expectedLength + " values, got " + array.length);
	}
	
	private static final int asInt(float f) {
		int x = (int) f;
		if(x != f)
			throw new IllegalArgumentException("Invalid int value");
		return x;
	}

}
