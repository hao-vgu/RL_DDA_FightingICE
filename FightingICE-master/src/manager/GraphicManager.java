package manager;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glFlush;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import image.CharacterActionImage;
import image.Image;
import image.LetterImage;
import render.ImageTask;
import render.QuadTask;
import render.RenderTask;
import render.StringTask;
import setting.GameSetting;
import setting.LaunchSetting;

/**
 * Manages image rendering.
 */
public class GraphicManager {

	/**
	 * List of rendering tasks.
	 */
	private LinkedList<RenderTask> renderTaskList;

	/**
	 * Text image.
	 */
	private LetterImage letterImage;

	/**
	 * List containing each character's images.
	 */
	private ArrayList<CharacterActionImage> characterImageContainer;

	/**
	 * List containing projectile images.
	 */
	private ArrayList<Image> projectileImageContainer;

	/**
	 * List containing special-move images.
	 */
	private ArrayList<Image> ultimateAttackImageContainer;

	/**
	 * List containing the "Hit" image.
	 */
	private ArrayList<Image> hitTextImageContainer;

	/**
	 * List containing images for the digits 1–9.
	 */
	private ArrayList<Image> counterTextImageContainer;

	/**
	 * Two-dimensional array containing uppercut images.
	 */
	private Image[][] upperImageContainer;

	/**
	 * Two-dimensional array containing images for effects displayed when an attack hits.
	 */
	private Image[][] hitEffectImageContainer;

	/**
	 * List containing background images.
	 */
	private ArrayList<Image> backGroundImage;

	/**
	 * Rendering data.
	 */
	private BufferedImage screen;

	private Graphics2D screenGraphic;


	/**
	 * Class constructor.
	 */
	private GraphicManager() {
		Logger.getAnonymousLogger().log(Level.INFO, "Create instance: " + GraphicManager.class.getName());

		this.renderTaskList = new LinkedList<RenderTask>();
		this.letterImage = new LetterImage();

		this.characterImageContainer = new ArrayList<CharacterActionImage>();

		this.projectileImageContainer = new ArrayList<Image>();
		this.ultimateAttackImageContainer = new ArrayList<Image>();

		this.counterTextImageContainer = new ArrayList<Image>();
		this.hitTextImageContainer = new ArrayList<Image>();

		this.upperImageContainer = new Image[2][3];
		this.hitEffectImageContainer = new Image[4][4];
		this.backGroundImage = new ArrayList<Image>();

		screen = new BufferedImage(GameSetting.STAGE_WIDTH, GameSetting.STAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
		screenGraphic = screen.createGraphics();
	}

	/**
	 * Get the singleton GraphicManager instance.
	 *
	 * @return the singleton GraphicManager instance
	 */
	public static GraphicManager getInstance() {
		return GraphicManagerHolder.instance;
	}

	/**
	 * Holder class that creates the instance the first time getInstance() is called.
	 */
	private static class GraphicManagerHolder {
		private static final GraphicManager instance = new GraphicManager();
	}

	/**
	 * Get the list containing each character's images.
	 *
	 * @return list containing each character's images
	 */
	public ArrayList<CharacterActionImage> getCharacterImageContainer() {
		return this.characterImageContainer;
	}

	/**
	 * Get the list containing projectile images.
	 *
	 * @return list containing projectile images
	 */
	public ArrayList<Image> getProjectileImageContainer() {
		return this.projectileImageContainer;
	}

	/**
	 * Get the list containing images for the digits 1–9.
	 *
	 * @return list containing images for the digits 1–9
	 */
	public ArrayList<Image> getCounterTextImageContainer() {
		return this.counterTextImageContainer;
	}

	/**
	 * Get the list containing special-move images.
	 *
	 * @return list containing special-move images
	 */
	public ArrayList<Image> getUltimateAttackImageContainer() {
		return this.ultimateAttackImageContainer;
	}

	/**
	 * Get the list containing the "Hit" image.
	 *
	 * @return list containing the "Hit" image
	 */
	public ArrayList<Image> getHitTextImageContainer() {
		return this.hitTextImageContainer;
	}

	/**
	 * Get the two-dimensional array containing uppercut images.
	 *
	 * @return the two-dimensional array containing uppercut images
	 */
	public Image[][] getUpperImageContainer() {
		return this.upperImageContainer;
	}

	/**
	 * Get the two-dimensional array containing images for effects displayed when an attack hits.
	 *
	 * @return the two-dimensional array containing images for effects displayed when an attack hits
	 */
	public Image[][] getHitEffectImageContaier() {
		return this.hitEffectImageContainer;
	}

	/**
	 * Get the list containing background images.
	 *
	 * @return list containing background images
	 */
	public ArrayList<Image> getBackgroundImage() {
		return this.backGroundImage;
	}

	/**
	 * Execute the tasks in the rendering task list and render the images.<br>
	 *
	 * @see DisplayManager#gameLoop(GameManager)
	 */
	public void render() {
		if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.STANDARD_MODE)) {
			// Set the clear color to black.
			glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			// Clear the buffer by filling it with the specified color.
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			// Execute the remaining rendering tasks and draw the images to the buffer.
			while (!this.renderTaskList.isEmpty()) {
				this.renderTaskList.removeFirst().render();
			}
			
			// Render the buffer contents to the screen.
			glFlush();
		}
	}

	/**
	 * Create an image-rendering task and add it to the task list.
	 *
	 * @param img
	 *            Image.
	 * @param x
	 *            X-coordinate at which to render the image.
	 * @param y
	 *            Y-coordinate at which to render the image.
	 * @param direction
	 *            Image orientation (true for right-facing).
	 */
	public void drawImage(Image img, int x, int y, boolean direction) {
		this.drawImage(img, x, y, direction, true);
	}
	
	public void drawImage(Image img, int x, int y, boolean direction, boolean shouldRender) {
		this.drawImage(img, x, y, img.getWidth(), img.getHeight(), direction, -img.getWidth(), 0, shouldRender);
	}

	/**
	 * Create an image-rendering task and add it to the task list.
	 *
	 * @param img
	 *            Image to render.
	 * @param x
	 *            X-coordinate at which to render the image.
	 * @param y
	 *            Y-coordinate at which to render the image.
	 * @param sizeX
	 *            Image width.
	 * @param sizeY
	 *            Image height.
	 * @param direction
	 *            Image orientation (true for right-facing).
	 */
	public void drawImage(Image img, int x, int y, int sizeX, int sizeY, boolean direction, double tx, double ty, boolean shouldRender) {
		if (!LaunchSetting.isExpectedProcessingMode(LaunchSetting.HEADLESS_MODE)) return;
		
		this.drawImageInScreenData(img, x, y, sizeX, sizeY, direction, tx, ty);
		
		if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.STANDARD_MODE) && shouldRender) {
			ImageTask task = new ImageTask(img.getTextureId(), x, y, sizeX, sizeY, direction);
			this.renderTaskList.add(task);
		}
	}

	public void drawImageInScreenData(Image img, int x, int y, int sizeX, int sizeY, boolean direction, double tx, double ty) {
		if (direction) {
			screenGraphic.drawImage(img.getBufferedImage(), x, y, sizeX, sizeY, null);
		} else {
			AffineTransform transform = AffineTransform.getScaleInstance(-1d, 1d);
			transform.translate(tx, ty);
			AffineTransformOp flip = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			screenGraphic.drawImage(flip.filter(img.getBufferedImage(), null), x, y, sizeX, sizeY, null);
		}
	}

	/**
	 * Create a text-rendering task and add it to the task list.
	 *
	 * @param string
	 *            Text to render.
	 * @param x
	 *            X-coordinate at which to render the text image.
	 * @param y
	 *            Y-coordinate at which to render the text image.
	 */
	public void drawString(String string, int x, int y) {
		this.drawString(string, x, y, true);
	}
	
	public void drawString(String string, int x, int y, boolean shouldRender) {
		if (!LaunchSetting.isExpectedProcessingMode(LaunchSetting.HEADLESS_MODE)) return;
		
		this.drawStringInScreenData(string, x, y);
		
		if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.STANDARD_MODE) && shouldRender) {
			StringTask task = new StringTask(letterImage, string, x, y);
			this.renderTaskList.add(task);
		}
	}
	
	public void drawStringInScreenData(String string, int x, int y) {
		int nowPositionX = x;

		for (int i = 0; i < string.length(); i++) {
			Image img = letterImage.getLetterImage(string.charAt(i));
			screenGraphic.drawImage(img.getBufferedImage(), nowPositionX, y, img.getWidth(), img.getHeight(), null);

			nowPositionX += img.getBufferedImage().getWidth();
		}
	}

	/**
	 * Create a task to render a solid-colored rectangle and add it to the task list.<br>
	 * The fill color can be specified as an argument.
	 *
	 * @param x
	 *            X-coordinate at which to render the rectangle.
	 * @param y
	 *            Y-coordinate at which to render the rectangle.
	 * @param sizeX
	 *            Rectangle width.
	 * @param sizeY
	 *            Rectangle height.
	 * @param red
	 *            Red component of the fill color.
	 * @param green
	 *            Green component of the fill color.
	 * @param blue
	 *            Blue component of the fill color.
	 * @param alpha
	 *            Opacity of the fill color.
	 */
	public void drawQuad(int x, int y, int sizeX, int sizeY, float red, float green, float blue, float alpha) {
		this.drawQuad(x, y, sizeX, sizeY, red, green, blue, alpha, true);
	}
	
	public void drawQuad(int x, int y, int sizeX, int sizeY, float red, float green, float blue, float alpha, boolean shouldRender) {
		if (!LaunchSetting.isExpectedProcessingMode(LaunchSetting.HEADLESS_MODE)) return;
		
		this.drawQuadInScreenData(x, y, sizeX, sizeY, red, green, blue, alpha);
		
		if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.STANDARD_MODE) && shouldRender) {
			QuadTask task = new QuadTask(QuadTask.FILLED_QUAD, x, y, sizeX, sizeY, red, green, blue, alpha);
			this.renderTaskList.add(task);
		}
	}
	
	public void drawQuadInScreenData(int x, int y, int sizeX, int sizeY, float red, float green, float blue, float alpha) {
		screenGraphic.setColor(new Color(red, green, blue));
		
		if (sizeX < 0) {
			x += sizeX;
			sizeX *= -1;
		}
		
		if (sizeY < 0) {
			y += sizeY;
			sizeY *= -1;
		}
		
		screenGraphic.fillRect(x, y, sizeX, sizeY);
	}

	/**
	 * Create a task to render a rectangle outline and add it to the task list.<br>
	 * The outline color can be specified as an argument.
	 *
	 * @param x
	 *            X-coordinate at which to render the rectangle.
	 * @param y
	 *            Y-coordinate at which to render the rectangle.
	 * @param sizeX
	 *            Rectangle width.
	 * @param sizeY
	 *            Rectangle height.
	 * @param red
	 *            Red component of the outline color.
	 * @param green
	 *            Green component of the outline color.
	 * @param blue
	 *            Blue component of the outline color.
	 * @param alpha
	 *            Opacity of the outline color.
	 */
	public void drawLineQuad(int x, int y, int sizeX, int sizeY, float red, float green, float blue, float alpha) {
		this.drawLineQuad(x, y, sizeX, sizeY, red, green, blue, alpha, true);
	}
	
	public void drawLineQuad(int x, int y, int sizeX, int sizeY, float red, float green, float blue, float alpha, boolean shouldRender) {
		if (!LaunchSetting.isExpectedProcessingMode(LaunchSetting.HEADLESS_MODE)) return;
		
		this.drawLineQuadinScreenData(x, y, sizeX, sizeY, red, green, blue, alpha);
		
		if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.STANDARD_MODE) && shouldRender) {
			QuadTask task = new QuadTask(QuadTask.LINE_QUAD, x, y, sizeX, sizeY, red, green, blue, alpha);
			this.renderTaskList.add(task);
		}
	}

	public void drawLineQuadinScreenData(int x, int y, int sizeX, int sizeY, float red, float green, float blue, float alpha){
		screenGraphic.setColor(new Color(red, green, blue));
		screenGraphic.drawRect(x, y, sizeX, sizeY);
	}

	/**
	 * Set the specified font.
	 *
	 * @param lf
	 *            Text font.
	 */
	public void setLetterFont(LetterImage lf) {
		this.letterImage = lf;
	}

	public void resetScreen() {
		disposeScreenGraphic();
		
		screen = new BufferedImage(screen.getWidth(), screen.getHeight(), BufferedImage.TYPE_INT_RGB);
		screenGraphic = screen.createGraphics();
		screenGraphic.setColor(new Color(128, 128, 128));
	}

	public void disposeScreenGraphic() {
		screenGraphic.dispose();
	}

	public BufferedImage getScreenImage() {
		return screen;
	}

	/**
	 * Clear the GraphicManager fields.
	 */
	public void close() {
		this.renderTaskList.clear();
		this.letterImage = null;
		this.characterImageContainer.clear();
		this.projectileImageContainer.clear();
		this.ultimateAttackImageContainer.clear();
		this.counterTextImageContainer.clear();
		this.hitTextImageContainer.clear();
		this.upperImageContainer = null;
		this.hitEffectImageContainer = null;
		this.backGroundImage.clear();
	}

}
