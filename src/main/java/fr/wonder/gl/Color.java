package fr.wonder.gl;


public class Color {
	
	public static final Color WHITE = new Color(1);
	public static final Color BLACK = new Color(0);
	public static final Color GRAY = new Color(.7f);
	public static final Color RED = new Color(1, 0, 0);
	public static final Color YELLOW = new Color(1, 1, 0);
	public static final Color PINK = new Color(.8f, .2f, .8f);
	public static final Color PURPLE = new Color(.8f, .35f, 1f);
	public static final Color DARK_GREEN = new Color(0, .8f, 0);

	public final float r, g, b, a;

	public Color(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public Color(float r, float g, float b) {
		this(r, g, b, 1);
	}
	
	public Color(float white) {
		this(white, white, white);
	}
	
	public Color copy() {
		return new Color(r, g, b, a);
	}
	
	public Color copy(float a) {
		return new Color(r, g, b, a);
	}
	
	public static Color fromHSV(float hue, float saturation, float value) {
		int h = (int)(hue * 6);
		float f = hue * 6 - h;
		float p = value * (1 - saturation);
		float q = value * (1 - f * saturation);
		float t = value * (1 - (1 - f) * saturation);
		
		switch (h) {
		case 0: return new Color(value, t, p);
		case 1: return new Color(q, value, p);
		case 2: return new Color(p, value, t);
		case 3: return new Color(p, q, value);
		case 4: return new Color(t, p, value);
		case 5: return new Color(value, p, q);
		default: return BLACK;
		}
	}
	
	@Override
	public String toString() {
		return String.format("(%.2f, %.2f, %.2f, %.2f)", r, g, b, a);
	}
	
}
