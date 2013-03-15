package mdesl.g2d.compositing;

import mdesl.g2d.batch.GenericBatch;

public class Compositor extends GenericBatch {
	
	public static final int DEFAULT_MAX_TEXTURES = 2;
	
	public Compositor(int textures, int size, boolean multiTexCoord) {
		super(textures, size, multiTexCoord);
	}
	
	public Compositor(int textures) {
		this(textures, 500, true);
	}
	
	public Compositor() {
		this(DEFAULT_MAX_TEXTURES);
	}
	
	public void begin(ShaderEffect effect) {
		if (getTextureCount() < effect.getMinimumTextureCount())
			throw new IllegalArgumentException("the Compositor doesn't have enough texture units for this effect");
		begin(effect.getShader());
	}
}



//TODO: generic vertex attributes for each sprite?
	/*
	GenAttrib genAttrib = new GenAttrib(); 
  batch = new Compositor(blah, genAttrib ...);
	 
	 
	for each sprite..
	    genAttrib.x = sprite.genX;
	    genAttrib.y = sprite.genY;
	    batch.setTexture(sprite.getTextureRegion());
	    batch.draw();
	*/