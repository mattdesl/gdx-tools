package mdesl.test;

import mdesl.g2d.batch.GenericBatch;
import mdesl.g2d.compositing.effects.Shaders;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class CompositorTest implements ApplicationListener {

	public static void main(String[] args) {
		new LwjglApplication(new CompositorTest(), "Test", 640, 480, true);
	}

	Texture tex0, tex1, mask;
	GenericBatch gfx;
	OrthographicCamera cam;
	ShaderProgram efx;
	ShapeRenderer shapeRenderer;
	
	/** Converts the components of a color, as specified by the HSB model, to an
	 * equivalent set of values for the default RGB model.
	 * <p>
	 * The <code>saturation</code> and <code>brightness</code> components should
	 * be floating-point values between zero and one (numbers in the range
	 * 0.0-1.0). The <code>hue</code> component can be any floating-point
	 * number. The floor of this number is subtracted from it to create a
	 * fraction between 0 and 1. This fractional number is then multiplied by
	 * 360 to produce the hue angle in the HSB color model.
	 * <p>
	 * The integer that is returned by <code>HSBtoRGB</code> encodes the value
	 * of a color in bits 0-23 of an integer value that is the same format used
	 * by the method {@link #getRGB() <code>getRGB</code>}. This integer can be
	 * supplied as an argument to the <code>Color</code> constructor that takes
	 * a single integer argument.
	 * 
	 * @param hue the hue component of the color
	 * @param saturation the saturation of the color
	 * @param brightness the brightness of the color
	 * @return the RGB value of the color with the indicated hue, saturation,
	 * and brightness.
	 * @see java.awt.Color#getRGB()
	 * @see java.awt.Color#Color(int)
	 * @see java.awt.image.ColorModel#getRGBdefault()
	 * @since JDK1.0 */
	public static int HSBtoRGBA8888(float hue, float saturation, float brightness) {
		int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int)(brightness * 255.0f + 0.5f);
		} else {
			float h = (hue - (float)Math.floor(hue)) * 6.0f;
			float f = h - (float)java.lang.Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int)h) {
			case 0:
				r = (int)(brightness * 255.0f + 0.5f);
				g = (int)(t * 255.0f + 0.5f);
				b = (int)(p * 255.0f + 0.5f);
				break;
			case 1:
				r = (int)(q * 255.0f + 0.5f);
				g = (int)(brightness * 255.0f + 0.5f);
				b = (int)(p * 255.0f + 0.5f);
				break;
			case 2:
				r = (int)(p * 255.0f + 0.5f);
				g = (int)(brightness * 255.0f + 0.5f);
				b = (int)(t * 255.0f + 0.5f);
				break;
			case 3:
				r = (int)(p * 255.0f + 0.5f);
				g = (int)(q * 255.0f + 0.5f);
				b = (int)(brightness * 255.0f + 0.5f);
				break;
			case 4:
				r = (int)(t * 255.0f + 0.5f);
				g = (int)(p * 255.0f + 0.5f);
				b = (int)(brightness * 255.0f + 0.5f);
				break;
			case 5:
				r = (int)(brightness * 255.0f + 0.5f);
				g = (int)(p * 255.0f + 0.5f);
				b = (int)(q * 255.0f + 0.5f);
				break;
			}
		}
		return (r << 24) | (g << 16) | (b << 8) | 0x000000ff;
	}

	@Override
	public void create() {


//		Pixmap px = new Pixmap(16, 16, Format.RGBA8888);
//		for (int x = 0; x < px.getWidth(); x++) {
//			for (int y = 0; y < px.getHeight(); y++) {
//				int brickHeight = 8;
//				int brickWidth = 16;
//
//				// starting x position
//				int nx = x + brickWidth / 4;
//
//				// offset nx every other row
//				if ((y % brickHeight * 2) < brickHeight)
//					nx += brickWidth / 2;
//
//				// if inside mortar
//				if (nx % brickWidth < 1 || y % (brickHeight / 2) < 1)
//					px.setColor(Color.LIGHT_GRAY);
//				// not inside mortar.. must be a brick
//				else
//					px.setColor(0.8f, 0.3f, 0.3f, 1f);
//
//				// draw pixel
//				px.drawPixel(x, y);
//			}
//		}
		
		System.out.println();
		tex0 = new Texture(Gdx.files.internal("data/dirt.png"));
		tex1 = new Texture(Gdx.files.internal("data/dirt.png"));
		mask = new Texture(Gdx.files.internal("data/mask.png"));

		// important since we aren't using some uniforms and attributes that
		// SpriteBatch expects
		ShaderProgram.pedantic = false;

		efx = Shaders.newSimpleShader("gl_FragColor = texColor0;");

		gfx = new GenericBatch(2, 100, true); // blending two textures

		gfx.setTexture(0, tex0);
		gfx.setTexture(1, mask);

		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.setToOrtho(false);
		
		shapeRenderer = new ShapeRenderer();
	}

	@Override
	public void resize(int width, int height) {
		cam.setToOrtho(false, width, height);
		gfx.setProjectionMatrix(cam.combined);
	}
	
	private float rot = 0f;

	@Override
	public void render() {
//		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		Gdx.gl.glEnable(GL10.GL_BLEND);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

//		gfx.begin(efx);
//		gfx.draw(tex0, 25, 25, tex0.getWidth(), tex0.getHeight() * 1f);
//		gfx.end();
		
		shapeRenderer.begin(ShapeType.Rectangle);
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.identity();

		shapeRenderer.translate(100, 100, 0f);
		shapeRenderer.rotate(0.f, 0.f, 1.f, rot+=0.5f);
		shapeRenderer.rect(0, 0, 50, 50);

		shapeRenderer.identity();
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.rotate(0f, 0f, 1f, 45f);
		shapeRenderer.rect(300, 100, 50, 50);
//
//		shapeRenderer.identity();
//		shapeRenderer.setColor(Color.YELLOW);
//		shapeRenderer.rect(300, 100, 50, 50);
		shapeRenderer.end();
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
