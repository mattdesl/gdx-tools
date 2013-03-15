package mdesl.test;

import mdesl.g2d.batch.GenericBatch;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class PuzzleBatchTest implements ApplicationListener {

	public static void main(String[] args) {
		new LwjglApplication(new PuzzleBatchTest(), "Test", 640, 480, true);
	}
	
	/** The source for the vertex shader. */
	public static final String VERT_SRC = 
			  //vertex attributes
			  "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" 
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" 
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "1;\n\n"
			  //projection matrix
			+ "uniform mat4 " + GenericBatch.U_PROJECTION_MATRIX +";\n\n"
			  //attributes sent to frag shader
			+ "varying vec2 vTexCoord0;\n" 
			+ "varying vec2 vTexCoord1;\n"
			+ "varying vec4 vColor;\n"
			+ "\n"
			+ "void main() {\n" 
			+ "  vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
			+ "  vTexCoord0 = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" 
			+ "  vTexCoord1 = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "1;\n" 
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
			+ "varying vec2 vTexCoord1;\n"
			+ "varying LOWP vec4 vColor;\n\n" //colors use LOWP precision
			  //our samplers, the photo and jigsaw mask
			+ "uniform sampler2D "+GenericBatch.U_TEXTURE+"0;\n"
			+ "uniform sampler2D "+GenericBatch.U_TEXTURE+"1;\n"
			+ "\n"			
			+ "void main(void) {\n"
			+ "  vec4 texColor0 = texture2D("+GenericBatch.U_TEXTURE+"0, vTexCoord0);\n"
			+ "  vec4 texColor1 = texture2D("+GenericBatch.U_TEXTURE+"1, vTexCoord1);\n"
			+ "  texColor0.a = mix(texColor0.a, 0.0, texColor1.r);\n"
			+ "  gl_FragColor = texColor0;\n"
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
	TextureAtlas jigsawAtlas;
	
	TextureRegion[] jigsawPieces;
	TextureRegion[] photoPieces;
	
	OrthographicCamera cam;
	GenericBatch batch;
	ShaderProgram shader;
				
	@Override
	public void create() {
		//important since we aren't using some uniforms and attributes that SpriteBatch expects
		ShaderProgram.pedantic = false;
		shader = createDefaultShader();
		
		//load our jigsaw image
		jigsawAtlas = new TextureAtlas("data/puzzle.pack");
		
		//get each region
		jigsawPieces = new TextureRegion[2];
		jigsawPieces[0] = jigsawAtlas.getRegions().get(0);
		jigsawPieces[1] = jigsawAtlas.getRegions().get(1);
		
		//load our photo
		photo = new Texture("data/photo.png");
		
		//our jigsaws all use the same size
		//important to use int here because TextureRegion's float parameters lead to different results
		int w = jigsawPieces[0].getRegionWidth();
		int h = jigsawPieces[0].getRegionHeight();
		
		//split up our photo in some smarter way than this...
		photoPieces = new TextureRegion[2];
		photoPieces[1] = new TextureRegion(photo, 19, 91, w, h);
		photoPieces[0] = new TextureRegion(photo, 0, 254, w, h);
		
		//create the batch with 2 texture units
		batch = new GenericBatch(2);
		
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
		
		//size of quads will be based on the jigsaw width/height
		float w = jigsawPieces[0].getRegionWidth();
		float h = jigsawPieces[0].getRegionHeight();

		//get mouse position for second piece
		float mouseX = Gdx.input.getX();
		float mouseY = Gdx.input.getY();
		
		//position of first piece... fixed
		float x1 = 25f;
		float y1 = 25f;
		
		//position of second piece, centered on mouse
		float x2 = mouseX - w/2f;
		float y2 = Gdx.graphics.getHeight() - mouseY - h/2f; 
		
		//draw our first piece, fixed
		draw(0, x1, y1, w, h);
		//draw our second piece, at mouse pos
		draw(1, x2, y2, w, h);
				
		batch.end();
	}
 
	void draw(int index, float x, float y, float w, float h) {
		//u_texture0 => photo
		//u_texture1 => jigsaw
		batch.setTexture(0, photoPieces[index]);
		batch.setTexture(1, jigsawPieces[index]);
		
		batch.draw(x, y, w, h);
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
		jigsawAtlas.dispose();
	}	
	
}