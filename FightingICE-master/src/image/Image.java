package image;

import java.awt.image.BufferedImage;

/**
 * Stores image data.
 */
public class Image {

	/**
	 * Right-facing direction (true).
	 */
	static final public boolean DIRECTION_RIGHT = true;

	/**
	 * Left-facing direction (false).
	 */
	static final public boolean DIRECTION_LEFT = false;

	/**
	 * Image texture ID.
	 */
	private int textureId;

	/**
	 * Image stored in the buffer.
	 */
	private BufferedImage bimg;

	/**
	 * Constructor that creates an Image instance.<br>
	 * If no arguments are supplied, initialize the texture ID to -1 and the BufferedImage to null.
	 */
	public Image() {
		this.textureId = -1;
		this.bimg = null;
	}

	/**
	 * Constructor that creates an Image instance.<br>
	 * Initialize a new instance using the texture ID and BufferedImage from the supplied Image instance.
	 *
	 * @param image
	 *            Image instance.
	 */
	public Image(Image image) {
		this.textureId = image.getTextureId();
		this.bimg = image.getBufferedImage();
	}

	/**
	 * Constructor that creates a new image instance.<br>
	 * Initialize a new instance using the supplied texture ID and BufferedImage.
	 *
	 * @param id
	 *            Image texture ID.
	 * @param bimg
	 *            BufferedImage
	 */
	public Image(int id, BufferedImage bimg) {
		this.textureId = id;
		this.bimg = bimg;
	}

	/**
	 * Return the image texture ID.
	 *
	 * @return the image texture ID
	 */
	public int getTextureId() {
		return this.textureId;
	}

	/**
	 * Return the image BufferedImage.
	 *
	 * @return the image BufferedImage
	 */
	public BufferedImage getBufferedImage() {
		BufferedImage res = new BufferedImage(this.bimg.getWidth(), this.bimg.getHeight(), this.bimg.getType());
		res.setData(bimg.getData());
		return res;
	}

	/**
	 * Set the image texture ID.
	 *
	 * @param textureId
	 *            Image texture ID.
	 */
	public void setTextureId(int textureId) {
		this.textureId = textureId;
	}

	/**
	 * Set the image BufferedImage.
	 *
	 * @param bimg
	 *            Image BufferedImage.
	 */
	public void setBufferedImage(BufferedImage bimg) {
		this.bimg = new BufferedImage(bimg.getWidth(), bimg.getHeight(), bimg.getType());
		this.bimg.setData(bimg.getData());
	}

	/**
	 * Return the image width.
	 *
	 * @return the image width if the BufferedImage is not null; otherwise, 0
	 */
	public int getWidth() {
		if (this.bimg != null) {
			return this.bimg.getWidth();
		} else {
			return 0;
		}
	}

	/**
	 * Return the image height.
	 *
	 * @return the image height if the BufferedImage is not null; otherwise, 0
	 */
	public int getHeight() {
		if (this.bimg != null) {
			return this.bimg.getHeight();
		} else {
			return 0;
		}

	}
}
