package render;

import static org.lwjgl.opengl.GL11.*;

/**
 * Handles rectangle-rendering tasks.
 */
public class QuadTask extends RenderTask {

	/**
	 * Constant specifying that the rectangle should be filled with a color.
	 */
	public static final byte FILLED_QUAD = 0;

	/**
	 * Constant specifying that the rectangle should be drawn as an outline.
	 */
	public static final byte LINE_QUAD = 1;

	/**
	 * Rectangle drawing mode.
	 */
	private byte mode;

	/**
	 * Red component of the fill or outline color.
	 */
	private float red;

	/**
	 * Green component of the fill or outline color.
	 */
	private float green;

	/**
	 * Blue component of the fill or outline color.
	 */
	private float blue;

	/**
	 * Opacity.
	 */
	private float alpha;

	/**
	 * X-coordinate at which to draw the rectangle.
	 */
	private int posX;

	/**
	 * Y-coordinate at which to draw the rectangle.
	 */
	private int posY;

	/**
	 * Rectangle width.
	 */
	private int sizeX;

	/**
	 * Rectangle height.
	 */
	private int sizeY;

	/**
	 * Class constructor.
	 */
	public QuadTask() {
		this.mode = FILLED_QUAD;
		this.posX = -1;
		this.posY = -1;
		this.sizeX = -1;
		this.sizeY = -1;
		this.red = 0;
		this.green = 0;
		this.blue = 0;
		this.alpha = 0;
	}

	/**
	 * Constructor that sets the initial values of a QuadTask instance.
	 *
	 * @param mode
	 *            Drawing mode (FILLED_QUAD or LINE_QUAD).
	 * @param posX
	 *            X-coordinate at which to draw.
	 * @param posY
	 *            Y-coordinate at which to draw.
	 * @param sizeX
	 *            Rectangle width.
	 * @param sizeY
	 *            Rectangle height.
	 * @param r
	 *            Red component.
	 * @param g
	 *            Green component.
	 * @param b
	 *            Blue component.
	 * @param alpha
	 *            Opacity.
	 */
	public QuadTask(byte mode, int posX, int posY, int sizeX, int sizeY, float r, float g, float b, float alpha) {
		this.mode = mode;
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.red = r;
		this.green = g;
		this.blue = b;
		this.alpha = alpha;
	}

	/**
	 * Render a rectangle using the specified mode and color.<br>
	 */
	@Override
	public void render() {
		// Set the color.
		glColor3f(red, green, blue);

		switch (mode) {
		case FILLED_QUAD:
			// Draw a filled rectangle.
			glBegin(GL_QUADS);
			glVertex2i(posX, posY);
			glVertex2i(posX + sizeX, posY);
			glVertex2i(posX + sizeX, posY + sizeY);
			glVertex2i(posX, posY + sizeY);

			glEnd();
			break;

		default:
			// Draw a rectangle outline.
			glBegin(GL_LINE_LOOP);
			glVertex2i(posX, posY);
			glVertex2i(posX + sizeX, posY);
			glVertex2i(posX + sizeX, posY + sizeY);
			glVertex2i(posX, posY + sizeY);
			glEnd();
			break;
		}

		// Reset the color to the default value (white).
		glColor3d(1, 1, 1);
	}

}
