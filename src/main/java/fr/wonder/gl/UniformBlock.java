package fr.wonder.gl;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UniformBlock {
	
	public final String name;
	public final int size;
	public final Map<String, Uniform> uniforms = new HashMap<>();
	
	public UniformBlock(String name, int size, Collection<Uniform> uniforms) {
		this.name = name;
		this.size = size;
		for(Uniform u : uniforms)
			if(this.uniforms.put(u.name, u) != null)
				throw new IllegalArgumentException("Uniform " + u.name + " found twice");
	}
	
	@Override
	public String toString() {
		return "block " + name + "{" + size + "}" + uniforms;
	}

	public Uniform getUniform(String name) {
		return uniforms.get(name);
	}

	public Collection<Uniform> getUniforms() {
		return uniforms.values();
	}
	
}
