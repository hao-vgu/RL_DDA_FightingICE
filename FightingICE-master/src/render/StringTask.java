package render;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL11;

import image.Image;
import image.LetterImage;

/**
 * Handles text-rendering tasks.
 */
public class StringTask extends RenderTask {

	/**
	 * Font used to render text.
	 */
	private LetterImage imageFont;

	/**
	 * Text.
	 */
	private String str;

	/**
	 * X-coordinate at which to draw the text image.
	 */
	private int posX;

	/**
	 * Y-coordinate at which to draw the text image.
	 */
	private int posY;

	/**
	 * Class constructor.
	 */
	public StringTask() {
		this.imageFont = null;
		this.str = "";
		this.posX = -1;
		this.posY = -1;
	}

	/**
	 * Constructor that sets the initial values of a StringTask instance.
	 *
	 * @param imageFont
	 *            Font used to render text.
	 * @param str
	 *            Text.
	 * @param x
	 *            X-coordinate at which to draw the text image.
	 * @param y
	 *            Y-coordinate at which to draw the text image.
	 */
	public StringTask(LetterImage imageFont, String str, int x, int y) {
		this.imageFont = imageFont;
		this.str = str;
		this.posX = x;
		this.posY = y;
	}

	/**
	 * Render the text image.<br>
	 * Draw each character of the string in order.
	 */
	@Override
	public void render() {
		int nowPositionX = posX;

		// Draw each character of the string at the specified position.
		for (int i = 0; i < str.length(); i++) {
			Image img = imageFont.getLetterImage(str.charAt(i));
			draw(img, nowPositionX, posY);

			// Draw the next character offset along the X axis by the width of the current character.
			nowPositionX += img.getBufferedImage().getWidth();
		}
	}

	/**
	 * Draw the text image using OpenGL.
	 *
	 * @param img
	 *            Text image.
	 * @param posX
	 *            X-coordinate at which to draw the text image.
	 * @param posY
	 *            Y-coordinate at which to draw the text image.
	 */
	private void draw(Image img, int posX, int posY) {
		GL11.glEnable(GL_TEXTURE_2D);
		glColor3f(1.0f, 1.0f, 1.0f);

		// Bind the texture
		glBindTexture(GL_TEXTURE_2D, img.getTextureId());

		// Draw a textured quad.
		glBegin(GL_QUADS);
		{
			// Top left corner of the texture
			glTexCoord2f(0, 0);
			glVertex2f(posX, posY);

			// Top right corner of the texture
			glTexCoord2f(1, 0);
			glVertex2f(posX + img.getBufferedImage().getWidth(), posY);

			// Bottom right corner of the texture
			glTexCoord2f(1, 1);
			glVertex2f(posX + img.getBufferedImage().getWidth(), posY + img.getBufferedImage().getHeight());

			// Bottom left corner of the texture
			glTexCoord2f(0, 1);
			glVertex2f(posX, posY + img.getBufferedImage().getHeight());

		}
		glEnd();

		GL11.glDisable(GL_TEXTURE_2D);
	}

}
