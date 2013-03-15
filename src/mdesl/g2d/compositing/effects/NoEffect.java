package mdesl.g2d.compositing.effects;

import mdesl.g2d.compositing.Compositor;

public class NoEffect extends SimpleEffect {

	public static final String FRAG_SHADER =
			 "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n" + //
			"varying vec2 "+V_TEXCOORD+"0;\n" +
			"uniform sampler2D "+Compositor.U_TEXTURE+"0;\n" +	
			"void main(void) {\n" +
			"	gl_FragColor = texture2D(u_texture0, vTexCoord0);\n" + 
			"}";
	
	public NoEffect() {
		super("NoEffect", 1, FRAG_SHADER);
	}
}


