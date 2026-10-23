package render;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL11;

import image.Image;

/**
 * Handles image-rendering tasks.
 */
public class ImageTask extends RenderTask {

	/**
	 * Image texture ID.
	 */
	private int textureId;

	/**
	 * Image width.
	 */
	private int width;

	/**
	 * Image height.
	 */
	private int height;

	/**
	 * X-coordinate at which to draw the image.
	 */
	private int posX;

	/**
	 * Y-coordinate at which to draw the image.
	 */
	private int posY;

	/**
	 * Image orientation (true for right-facing, false for left-facing).
	 */
	private boolean direction;

	/** Constructor. */
	public ImageTask() {
		this.textureId = -1;
		this.posX = -1;
		this.posY = -1;
		this.width = -1;
		this.height = -1;
		this.direction = true;
	}

	/**
	 * Constructor that sets the initial values of an ImageTask instance.<br>
	 * Specify an image directly to retrieve its texture ID, width, and height.
	 *
	 * @param img
	 *            Image.
	 * @param x
	 *            X-coordinate at which to draw the image.
	 * @param y
	 *            Y-coordinate at which to draw the image.
	 * @param dir
	 *            Image orientation (true for right-facing).
	 */
	public ImageTask(Image img, int x, int y, boolean dir) {
		this.textureId = img.getTextureId();
		this.posX = x;
		this.posY = y;
		this.width = img.getBufferedImage().getWidth();
		this.height = img.getBufferedImage().getHeight();
		this.direction = dir;
	}

	/**
	 * Constructor that sets the initial values of an ImageTask instance.
	 *
	 * @param id
	 *            Image texture ID.
	 * @param x
	 *            X-coordinate at which to draw the image.
	 * @param y
	 *            Y-coordinate at which to draw the image.
	 * @param width
	 *            Image width.
	 * @param height
	 *            Image height.
	 * @param dir
	 *            Image orientation (true for right-facing).
	 */
	public ImageTask(int id, int x, int y, int width, int height, boolean dir) {
		this.textureId = id;
		this.posX = x;
		this.posY = y;
		this.width = width;
		this.height = height;
		this.direction = dir;
	}

	/**
	 * Render the image.<br>
	 * Render the image on a textured rectangle.
	 */
	@Override
	public void render() {
		GL11.glEnable(GL_TEXTURE_2D);

		// Bind the texture
		glBindTexture(GL_TEXTURE_2D, textureId);

		// Draw a textured quad.
		glBegin(GL_QUADS);
		{
			if (direction) {
				// Top left corner of the texture
				glTexCoord2f(0, 0);
				glVertex2f(posX, posY);

				// Top right corner of the texture
				glTexCoord2f(1, 0);
				glVertex2f(posX + width, posY);

				// Bottom right corner of the texture
				glTexCoord2f(1, 1);
				glVertex2f(posX + width, posY + height);

				// Bottom left corner of the texture
				glTexCoord2f(0, 1);
				glVertex2f(posX, posY + height);
			} else {
				// Top right corner of the texture
				glTexCoord2f(0, 0);
				glVertex2f(posX + width, posY);

				// Top left corner of the texture
				glTexCoord2f(1, 0);
				glVertex2f(posX, posY);

				// Bottom left corner of the texture
				glTexCoord2f(1, 1);
				glVertex2f(posX, posY + height);

				// Bottom right corner of the texture
				glTexCoord2f(0, 1);
				glVertex2f(posX + width, posY + height);
			}
		}
		glEnd();

		GL11.glDisable(GL_TEXTURE_2D);

	}

}
