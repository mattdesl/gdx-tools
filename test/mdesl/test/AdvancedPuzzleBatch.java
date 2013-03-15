package mdesl.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;

public abstract class AdvancedPuzzleBatch {
		
	/** The shader uniform name of the projection/transform matrix - "u_projTrans" */
	public static final String U_PROJECTION_MATRIX = "u_projTrans";
	/** The shader uniform name of the texture0 sampler (source photo) - "u_texture0" */
	public static final String U_TEXTURE0 = "u_texture0";
	/** The shader uniform name of the texture1 sampler (jigsaw mask) - "u_texture1" */
	public static final String U_TEXTURE1 = "u_texture1";
	
	/** The source for the vertex shader. */
	public static final String VERT_SRC = 
			  //vertex attributes
			  "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" 
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" 
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "1;\n\n"
			  //projection matrix
			+ "uniform mat4 " + U_PROJECTION_MATRIX +";\n\n"
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
			+ "uniform sampler2D "+U_TEXTURE0+";\n"
			+ "uniform sampler2D "+U_TEXTURE1+";\n"
			+ "\n"			
			+ "void main(void) {\n"
			+ "  vec4 texColor0 = texture2D("+U_TEXTURE0+", vTexCoord0);\n"
			+ "  vec4 texColor1 = texture2D("+U_TEXTURE1+", vTexCoord1);\n"
			  //For now we'll just return red
			+ "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" 
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
	
	
	protected Mesh mesh;
	protected float[] vertices;
	protected int idx;
	protected int quadSize;
	
	protected boolean drawing;
	protected ShaderProgram shader;
	protected VertexAttribute[] attributes;
	
	protected Matrix4 u_projTrans;
	
	public int renderCalls;	
	
	protected float color = Color.WHITE.toFloatBits();
	private Color tempColor = new Color(Color.WHITE);
	
	public AdvancedPuzzleBatch(int batchSize) {
		//since we are using shaders, we need GL20
		if (!Gdx.graphics.isGL20Available())
			throw new GdxRuntimeException("PuzzleBatch requires GLES20");
					
		//the size of our batch
		int vertexCount = 4 * batchSize;
		int indexCount = 6 * batchSize;
		
		//create the mesh with our specific vertex attributes
		mesh = new Mesh(VertexDataType.VertexArray, false, vertexCount, indexCount,
				new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), 
				new VertexAttribute(Usage.Color, 4, ShaderProgram.COLOR_ATTRIBUTE), 
				new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0"),
				new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"1"));
		
		//indices in the format { 0, 1, 2, 2, 3, 0 } per sprite
		short[] indices = new short[batchSize * 6];
		for (int i = 0, j = 0; i < indices.length; i += 6, j += 4) {
			indices[i + 0] = (short)(j + 0);
			indices[i + 1] = (short)(j + 1);
			indices[i + 2] = (short)(j + 2);
			indices[i + 3] = (short)(j + 2);
			indices[i + 4] = (short)(j + 3);
			indices[i + 5] = (short)(j + 0);
		}
		
		//set the indices on this mesh
		mesh.setIndices(indices);
		
		//the number of floats per vertex, as specified by our VertexAttributes
		final int numComponents = 10;
		
		//the number of floats per quad -- 4 verts * 10 floats
		quadSize = 4 * numComponents;
		
		//our vertex array needs to be able to hold enough floats for each vertex in our batch
		vertices = new float[batchSize * quadSize];
				
		u_projTrans = new Matrix4();
		u_projTrans.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
		
	
	
	/**
	 * Called to render the mesh with the current shader.
	 * @param primitiveCount the primitive count to send to Mesh.render
	 */
	protected void renderMesh(int primitiveCount) {
		mesh.render(shader, GL10.GL_TRIANGLES, 0, primitiveCount);
	}
	
	protected static class VertexPosition {
		float x1, y1; //top left
		float x2, y2; //bottom left
		float x3, y3; //bottom right
		float x4, y4; //top right
				
		public void transform(
				float x, float y, 
				float originX, float originY, 
				float width, float height, 
				float scaleX, float scaleY, 
				float rotationDeg) {
			// bottom left and top right corner points relative to origin
			final float worldOriginX = x + originX;
			final float worldOriginY = y + originY;
			float fx = -originX;
			float fy = -originY;
			float fx2 = width - originX;
			float fy2 = height - originY;

			// scale
			if (scaleX != 1 || scaleY != 1) {
				fx *= scaleX;
				fy *= scaleY;
				fx2 *= scaleX;
				fy2 *= scaleY;
			}

			// construct corner points, start from top left and go counter clockwise
			final float p1x = fx;
			final float p1y = fy;
			final float p2x = fx;
			final float p2y = fy2;
			final float p3x = fx2;
			final float p3y = fy2;
			final float p4x = fx2;
			final float p4y = fy;

			// rotate -- code from LibGDX's SpriteBatch
			if (rotationDeg != 0) {
				final float cos = MathUtils.cosDeg(rotationDeg);
				final float sin = MathUtils.sinDeg(rotationDeg);

				x1 = cos * p1x - sin * p1y;
				y1 = sin * p1x + cos * p1y;

				x2 = cos * p2x - sin * p2y;
				y2 = sin * p2x + cos * p2y;

				x3 = cos * p3x - sin * p3y;
				y3 = sin * p3x + cos * p3y;

				x4 = x1 + (x3 - x2);
				y4 = y3 - (y2 - y1);
			} else {
				x1 = p1x;
				y1 = p1y;
				x2 = p2x;
				y2 = p2y;
				x3 = p3x;
				y3 = p3y;
				x4 = p4x;
				y4 = p4y;
			}

			x1 += worldOriginX;
			y1 += worldOriginY;
			x2 += worldOriginX;
			y2 += worldOriginY;
			x3 += worldOriginX;
			y3 += worldOriginY;
			x4 += worldOriginX;
			y4 += worldOriginY;			
		}
	}
	
	
}

//batch = new AdvSpriteBatch(8, 1, VertexAttribute("Brightness", 2));

