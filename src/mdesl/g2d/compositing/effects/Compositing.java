package mdesl.g2d.compositing.effects;

import com.badlogic.gdx.graphics.Color;

/**
 * A set of common effects
 * @author 
 */
public class Compositing {

	
	static String vec4(Color c) {
		return "vec4("+c.r+", "+c.g+", "+c.b+", "+c.a+")";
	}
	
	public static class Effect {
		
		public final String header;
		public final String core;
		
		public Effect(String header, String core) {
			this.header = header;
			this.core = core;
		}
		
		public Effect(String core) {
			this("", core);
		}
	}
	
	
	public static Effect Invert = new Effect("{1}.rgb = 1.0 - {0}.rgb;");
	
	
	public static class ColorizeEffect extends Effect {
				
		public ColorizeEffect(Color tint, float amount) {
			super("const vec4 ColorizeEffect = " + vec4(tint) + ";",
				  "{1} = {0} * ColorizeEffect * amount;");
		}
	}
	
	public static Effect Sepia = new ColorizeEffect(new Color(1.2f, 1.0f, 0.8f, 1f), 1f); 
}