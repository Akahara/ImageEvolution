package fr.wonder.iev;

import fr.wonder.commons.math.vectors.Vec2;

public class Transform {
	
	public Vec2 translation;
	public float scale;
	public float rotation;
	
	public Transform(Vec2 translation, float scale, float rotation) {
		this.translation = translation;
		this.scale = scale;
		this.rotation = rotation;
	}
	
	public Transform() {
		this(new Vec2(0), 1, 0);
	}
	
}
