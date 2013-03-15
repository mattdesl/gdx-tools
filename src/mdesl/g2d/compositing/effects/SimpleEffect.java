package mdesl.g2d.compositing.effects;

import mdesl.g2d.compositing.Compositor;
import mdesl.g2d.compositing.ShaderEffect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * A basic implementation of a shader effect that writes N 
 * texture coordinates to the fragment shader, and also passes
 * color and position along.
 * @author mattdesl
 */
public class SimpleEffect implements ShaderEffect {
	
	public static final String V_TEXCOORD = "vTexCoord";
	public static final String V_COLOR = "vColor";
	
	protected ShaderProgram shader;
	private int minTextures;
	protected String name;
	
	/**
	 * Sets up the effect with the given number of 
	 * @param numTexCoords
	 * @param frag
	 */
	public SimpleEffect(String name, int numTexCoords, String frag) {
		this.minTextures = numTexCoords;
		this.name = name;
		String vert = buildVertexShaderSource(numTexCoords);
		shader = new ShaderProgram(vert, frag);
		if (!shader.isCompiled())
			throw new GdxRuntimeException("could not compile "+name+"\n"+shader.getLog());
		if (shader.getLog().length()!=0)
			Gdx.app.log(name, "Shader Log for "+name+": "+shader.getLog());
		shader.begin();
		updateUniforms();
		shader.end();
	}

	@Override
	public void dispose() {
		shader.dispose();
	}
	
	public String getName() {
		return name;
	}
	
	protected void updateUniforms() {
		//to be implemented by subclasses...
	}
	
	public int getMinimumTextureCount() {
		return minTextures;
	}
	
	public ShaderProgram getShader() {
		return shader;
	}
	
	protected String buildVertexShaderSource(int numTexCoords) {
		String str = "attribute vec4 "+ShaderProgram.POSITION_ATTRIBUTE+";\n" +
				 "attribute vec4 "+ShaderProgram.COLOR_ATTRIBUTE+";\n";
		for (int i=0; i<numTexCoords; i++) 
			str += "attribute vec2 "+ShaderProgram.TEXCOORD_ATTRIBUTE+i+";\n";
		str += "\n";
		str += "uniform mat4 "+Compositor.U_PROJECTION_MATRIX+";\n";
		str += "varying vec4 "+V_COLOR+";\n";
		for (int i=0; i<numTexCoords; i++) 
			str += "varying vec2 "+V_TEXCOORD+i+";\n";
		str +=  "\nvoid main() {\n";
		str += "  "+V_COLOR+" = "+ShaderProgram.COLOR_ATTRIBUTE+";\n";
		for (int i=0; i<numTexCoords; i++)
			str += "  "+V_TEXCOORD+i+" = "+ShaderProgram.TEXCOORD_ATTRIBUTE+i+";\n"; 
		str += "  gl_Position = "+Compositor.U_PROJECTION_MATRIX+" * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n";
		str += "}";
		return str;
	}
}
