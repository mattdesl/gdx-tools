package mdesl.test;

import mdesl.g2d.batch.GenericBatch;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class CustomAttrib implements ApplicationListener {

	public static void main(String[] args) {
		new LwjglApplication(new CustomAttrib(), "Test", 640, 480, true);
	}
	

	
	
	Texture tex0, tex1, mask;
	SplatBatch batch;
	OrthographicCamera cam;
	
	static enum Attrib {
		GenAttrib1;
		
		static final VertexAttribute[] list = {
			new VertexAttribute(Usage.Generic, 2, "GenAttrib1")
		};
	}
	
	static class SplatBatch extends GenericBatch {
		
		static final String VERT =  
				"attribute vec4 "+ShaderProgram.POSITION_ATTRIBUTE+";\n" +
				"attribute vec2 "+ShaderProgram.TEXCOORD_ATTRIBUTE+"0;\n" +
				"attribute vec2 "+ShaderProgram.TEXCOORD_ATTRIBUTE+"1;\n" +
				"attribute vec2 GenAttrib1;\n" +
				
				"uniform mat4 u_projTrans;\n" + 
				" \n" + 
				"varying vec2 vTexCoord0;\n" +
				"varying vec2 vTexCoord1;\n" +
				"varying vec2 vGenAttrib1;\n" +
				
				"void main() {\n" +  
				"	vTexCoord0 = "+ShaderProgram.TEXCOORD_ATTRIBUTE+"0;\n" +
				"	vTexCoord1 = "+ShaderProgram.TEXCOORD_ATTRIBUTE+"1;\n" + 
				"   vGenAttrib1 = GenAttrib1;\n" +
				"	gl_Position = u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
				"}";
		
		static final String FRAG = 
				//GL ES specific stuff
				  "#ifdef GL_ES\n" //
				+ "#define LOWP lowp\n" //
				+ "precision mediump float;\n" //
				+ "#else\n" //
				+ "#define LOWP \n" //
				+ "#endif\n" + //
				"varying vec2 vTexCoord0;\n" + 
				"varying vec2 vTexCoord1;\n" + 
				"varying vec2 vGenAttrib1;\n" +
				"uniform sampler2D u_texture0;\n" +	
				"uniform sampler2D u_texture1;\n" +
				"void main(void) {\n" + 
				"	//sample the colour from the first texture\n" + 
				"	vec4 texColor0 = texture2D(u_texture0, vTexCoord0);\n" + 
				"\n" + 
				"	//sample the colour from the second texture\n" + 
				"	vec4 texColor1 = texture2D(u_texture1, vTexCoord0);\n" + 
				"\n" + 
				"	//get the mask; we will only use the alpha channel\n" + 
				"	float mask = 1.0;\n" + 
				"\n" + 
				"	//interpolate the colours based on the mask\n" + 
				"	gl_FragColor = vec4(texColor0.rgb * vGenAttrib1.x, 1.0);\n" + 
				"}";
		
		public static ShaderProgram createDefaultShader() {
			ShaderProgram prog = new ShaderProgram(VERT, FRAG);
			if (!prog.isCompiled())
				throw new GdxRuntimeException("could not compile splat batch: "+prog.getLog());
			if (prog.getLog().length()!=0)
				Gdx.app.log("SplatBatch", prog.getLog());
			return prog;
		}
		
		public SplatBatch() {
			super(1, 1, 100, Attrib.list);
			shader = createDefaultShader();
		}	
		
		public void begin() {
			super.begin(shader);
		}
	}
	
	@Override
	public void create() {
		tex0 = new Texture(Gdx.files.internal("data/mask.png"));
		tex1 = new Texture(Gdx.files.internal("data/dirt.png"));
		mask = new Texture(Gdx.files.internal("data/mask.png"));
		
		//important since we aren't using some uniforms and attributes that SpriteBatch expects
		ShaderProgram.pedantic = false;
		
		batch = new SplatBatch();
		
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.setToOrtho(false);
	}
 
	@Override
	public void resize(int width, int height) {
		cam.setToOrtho(false, width, height);
		batch.setProjectionMatrix(cam.combined);
	}
	 
	@Override
	public void render() {
		Gdx.gl.glClearColor(1f,1f,1f,1f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		batch.begin();
				
//		batch.setTexture(0, new TextureRegion(tex1, 25, 25, 50, 50));
//		batch.setTexture(1, tex0);
		
		batch.setAttribute(0, 0.5f, 1f);
		batch.draw(tex1, 25, 25);
		
		batch.setAttribute(0, 1f, 1f);
		batch.draw(tex1, 100, 25);
		
		
		batch.end();
	}
 
	@Override
	public void pause() {
		
	}
 
	@Override
	public void resume() {
		
	}
 
	@Override
	public void dispose() {
		tex0.dispose();
	}	
	
}


