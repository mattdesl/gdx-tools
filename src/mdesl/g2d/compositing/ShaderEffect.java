package mdesl.g2d.compositing;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public interface ShaderEffect {
		
	/**
	 * Gets the minimum texture (sampler2D) texture count required by this shader.
	 * A Compositor needs to be initialized with at least this many textures.
	 * 
	 * @return the minimum number of texture units necessary for this shader
	 */
	public int getMinimumTextureCount();
	
	public ShaderProgram getShader();
	
	public String getName();
	
	public void dispose();
}
