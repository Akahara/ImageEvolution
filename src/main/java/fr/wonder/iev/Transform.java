package fr.wonder.iev;

public class Transform {
	
	public float translationX, translationY;
	public float scale;
	public float rotation;
	
	public Transform(float translationX, float translationY, float scale, float rotation) {
		this.translationX = translationX;
		this.translationY = translationY;
		this.scale = scale;
		this.rotation = rotation;
	}
	
	public Transform() {
		this(0, 0, 1, 0);
	}
	
}
