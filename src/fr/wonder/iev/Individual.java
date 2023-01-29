package fr.wonder.iev;

import fr.wonder.gl.Color;

public class Individual {
	
	public final int textureIndex;
	public final Transform transform;
	public Color color;
	public int score;
	
	public Individual(int textureIndex, Transform transform) {
		this.textureIndex = textureIndex;
		this.transform = transform;
	}
	
	public int getScore() {
		return score;
	}
	
	@Override
	public String toString() {
		return String.format("[tex=%d pos=(%+.6f, %+.6f) size=%.6f rot=%+.6f color=(%.3f, %.3f, %.3f)]",
				textureIndex,
				transform.translation.x,
				transform.translation.x,
				transform.scale,
				transform.rotation,
				color.r,
				color.g,
				color.b);
	}
	
}
