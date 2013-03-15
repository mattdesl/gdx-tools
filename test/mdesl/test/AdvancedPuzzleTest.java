package mdesl.test;

import java.util.Arrays;


import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AdvancedPuzzleTest implements ApplicationListener {

	public static void main(String[] args) {
		new LwjglApplication(new AdvancedPuzzleTest(), "Test", 640, 480, true);
	}

	Texture tex0, tex1;
	OrthographicCamera cam;
	Mesh mesh;
	Matrix4 u_projTrans;
	SpriteBatch batch;
	ShaderProgram shader;

	static final String VERT = 
			  //vertex attributes
			  "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" 
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" 
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "1;\n\n"
			  //projection matrix
			+ "uniform mat4 u_projTrans;\n\n"
			  //attributes sent to frag shader
			+ "varying vec2 vTexCoord0;\n" 
			+ "varying vec2 vTexCoord1;\n"
			+ "varying vec4 vColor;\n"
			+ "\n"
			+ "void main() {\n" 
			+ "  vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
			+ "	 vTexCoord0 = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" 
			+ "	 vTexCoord1 = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "1;\n" 
			+ "	 gl_Position = u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" 
			+ "}";

	static final String FRAG =
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
			+ "uniform sampler2D u_texture0;\n"
			+ "uniform sampler2D u_texture1;\n"
			+ "\n"			
			+ "void main(void) {\n"
			+ "  vec4 texColor0 = texture2D(u_texture0, vTexCoord0);\n"
			+ "  vec4 texColor1 = texture2D(u_texture1, vTexCoord1);\n"
			  //For now we'll just return red
			+ "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" 
			+ "}";

	public static ShaderProgram createDefaultShader() {
		ShaderProgram prog = new ShaderProgram(VERT, FRAG);
		if (!prog.isCompiled())
			throw new GdxRuntimeException("could not compile splat batch: " + prog.getLog());
		if (prog.getLog().length() != 0)
			Gdx.app.log("PuzzleBatch", prog.getLog());
		return prog;
	}

	@Override
	public void create() {
		System.out.println(AdvancedPuzzleBatch.VERT_SRC);
		System.out.println();
		System.out.println(AdvancedPuzzleBatch.FRAG_SRC);
		
		tex1 = new Texture(Gdx.files.internal("data/dirt.png"));
		tex0 = new Texture(Gdx.files.internal("data/grass.png"));
		
		//important since we aren't using some uniforms and attributes that SpriteBatch expects
		ShaderProgram.pedantic = false;
		shader = createDefaultShader();
		
		mesh = new Mesh(VertexDataType.VertexArray, false, 4, 6, 
					new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
					new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE),
					new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"),
					new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"1"));
		
		float[] verts = new float[] {
				0f, 0f, 
				1f, 1f, 1f, 1f,
				0f, 0f,
				0f, 0f,
				
				0f, 25f,
				1f, 1f, 1f, 1f,
				0f, 0f,
				0f, 0f,
				
				25f, 25f,
				1f, 1f, 1f, 1f,
				0f, 0f, 
				0f, 0f,
				
				25f, 0f,
				1f, 1f, 1f, 1f,
				0f, 0f,
				0f, 0f				
		};
		
		// 
		short[] indices = new short[6];
		for (int i = 0, j = 0; i < indices.length; i += 6, j += 4) {
			indices[i + 0] = (short)(j + 0);
			indices[i + 1] = (short)(j + 1);
			indices[i + 2] = (short)(j + 2);
			indices[i + 3] = (short)(j + 2);
			indices[i + 4] = (short)(j + 3);
			indices[i + 5] = (short)(j + 0);
		}
		mesh.setVertices(verts);
		mesh.setIndices(indices);
		
		batch = new SpriteBatch(1000, shader);
		batch.setShader(shader);
		
		
		
		//order
//		0.0 0.0
//		0.0 256.0
//		256.0 256.0
//		256.0 0.0
		
//		texcoord
//		0.0 1.0
//		0.0 0.0
//		1.0 0.0
//		1.0 1.0
		
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.setToOrtho(false);
		
		u_projTrans = new Matrix4();
		u_projTrans.set(cam.combined);
		u_projTrans.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void resize(int width, int height) {
		cam.setToOrtho(false, width, height);
		u_projTrans.set(cam.combined);
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		tex1.bind(1);
		tex0.bind(0);

		shader.begin();

		shader.setUniformi("u_texture0", 0);
		shader.setUniformi("u_texture1", 1);
		shader.setUniformMatrix("u_projTrans", u_projTrans);

		mesh.render(shader, GL10.GL_TRIANGLES, 0, 6);

		shader.end();
		
//		batch.begin();
//		batch.draw(tex0, 0, 0, 25, 25);
//		batch.end();
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {
	}

}
