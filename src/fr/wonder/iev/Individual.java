package fr.wonder.iev;

import fr.wonder.gl.Color;

public class Individual {
	
	public final int textureIndex;
	public final Transform transform;
	
	private int score;
	private Color color;
	
	public Individual(int textureIndex, Transform transform) {
		this.textureIndex = textureIndex;
		this.transform = transform;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public String toString() {
		return String.format("new Individual(%d, new Transform(new Vec2(%f, %f), %f, %f))  new Color(%f, %f, %f, %f)",
				textureIndex,
				transform.translation.x,
				transform.translation.x,
				transform.scale,
				transform.rotation,
				color.r,
				color.g,
				color.b,
				color.a);
	}
	
}
