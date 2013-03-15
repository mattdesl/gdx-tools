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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GenAttribTest implements ApplicationListener {

	public static void main(String[] args) {
		new LwjglApplication(new GenAttribTest(), "Test", 640, 480, true);
	}
	
	/** The source for the vertex shader. */
	public static final String VERT_SRC = 
			  //vertex attributes
			  "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" 
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" 
			+ "attribute float Brightness;\n" //CUSTOM ATTRIBUTE "IN" DECLARATION
			  //projection matrix
			+ "uniform mat4 " + GenericBatch.U_PROJECTION_MATRIX +";\n\n"
			  //attributes sent to frag shader
			+ "varying vec2 vTexCoord0;\n" 
			+ "varying vec2 vTexCoord1;\n"
			+ "varying vec4 vColor;\n"
			+ "varying float vBrightness;\n" //CUSTOM ATTRIBUTE "OUT" TO FRAG SHADER
			+ "\n"
			+ "void main() {\n" 
			+ "  vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
			+ "  vTexCoord0 = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
			+ "  vBrightness = Brightness;\n" 
			+ "  gl_Position = u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" 
			+ "}";

	/** The source for the fragment shader. */
	public static final String FRAG_SRC =
			  "#ifdef GL_ES\n"
			+ "#define LOWP lowp\n" 
			+ "precision mediump float;\n" 
			+ "#else\n" 
			+ "#define LOWP \n" 
			+ "#endif\n\n"
			  //attributes from vertex shader
			+ "varying vec2 vTexCoord0;\n"
			+ "varying LOWP vec4 vColor;\n" //colors use LOWP precision
			+ "varying float vBrightness;\n\n" //CUSTOM ATTRIBUTE "IN" FROM VERT SHADER
			  //texture units
			+ "uniform sampler2D "+GenericBatch.U_TEXTURE+"0;\n"
			+ "\n"			
			+ "void main(void) {\n"
			+ "  vec4 texColor0 = texture2D("+GenericBatch.U_TEXTURE+"0, vTexCoord0);\n"
			+ "  texColor0.rgb += vBrightness;\n"
			+ "  gl_FragColor = texColor0 * vColor;\n"
			+ "}";
	
	/** 
	 * Compiles a new instance of the default shader for this batch and returns it. If compilation
	 * was unsuccessful, GdxRuntimeException will be thrown.
	 * @return the default shader
	 */
	public static ShaderProgram createDefaultShader() {
		ShaderProgram prog = new ShaderProgram(VERT_SRC, FRAG_SRC);
		if (!prog.isCompiled())
			throw new GdxRuntimeException("could not compile splat batch: " + prog.getLog());
		if (prog.getLog().length() != 0)
			Gdx.app.log("PuzzleBatch", prog.getLog());
		return prog;
	}
	
	Texture photo;
	TextureRegion photoReg; 
	
	OrthographicCamera cam;
	BrightnessBatch batch;
	ShaderProgram shader;
	
	class BrightnessBatch extends GenericBatch {
		
		private float brightness;
		
		public BrightnessBatch(int size) {
			super(1, 1, size, true, 
					new VertexAttribute(Usage.Generic, 1, "Brightness"));
		}
		
		public void setBrightness(float brightness) {
			this.brightness = brightness;
		}
		
		public float getBrightness() {
			return brightness;
		}
		
		public void vertex(float x, float y, int cornerIndex) {
			super.vertex(x, y, cornerIndex);
			vertices[idx++] = brightness;
		}
	}
				
	@Override
	public void create() {
		//important since we aren't using some uniforms and attributes that SpriteBatch expects
		ShaderProgram.pedantic = false;
		shader = createDefaultShader();
		
		//load our photo
		photo = new Texture("data/photo.png");
		photoReg = new TextureRegion(photo, 0, photo.getHeight()/2, photo.getWidth()/2, photo.getHeight()/2);
		
		//create the batch with 2 texture units
		batch = new BrightnessBatch(500);
		
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
		
		batch.begin(shader);
		
		batch.setBrightness(0.25f);
		batch.draw(photoReg, 0, 0);
		
		batch.setBrightness(0f);
		batch.draw(photoReg, photoReg.getRegionWidth()+5, 0);
				
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
		shader.dispose();
		photo.dispose();
	}	
	
}