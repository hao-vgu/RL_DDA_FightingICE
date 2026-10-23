package image;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import loader.ResourceLoader;

/**
 * Handles text images.
 */
public class LetterImage {

	/**
	 * Text font.
	 */
	private Font font;

	/**
	 * Whether to apply antialiasing.
	 */
	private boolean antiAliasing;

	/**
	 * Hash map associating characters with their images.
	 */
	private HashMap<Character, Image> letterImageMap;

	/**
	 * Default constructor.
	 */
	public LetterImage() {

	}

	/**
	 * Constructor that creates a LetterImage instance.<br>
	 * Initialize the instance with the specified font and antialiasing setting.<br>
	 * Add images for alphanumeric characters and symbols to the hash map.
	 *
	 * @param font
	 *            Text font.
	 * @param antiAliasing
	 *            Whether to apply antialiasing.
	 */
	public LetterImage(Font font, boolean antiAliasing) {
		this.font = font;
		this.antiAliasing = antiAliasing;
		this.letterImageMap = new HashMap<Character, Image>();

		createLetterImage();
	}

	/**
	 * Return the image for the specified character.
	 *
	 * @param letter
	 *            Character to display as an image.
	 * @return the matching character image
	 */
	public Image getLetterImage(char letter) {
		return letterImageMap.get(letter);
	}

	/**
	 * Creates a standard Java2D BufferedImage of the given letter.
	 *
	 * @param letter
	 *            the letter to create the BufferedImage for
	 *
	 * @return the BufferedImage containing the letter
	 */
	private BufferedImage getLetterBufferedImage(char letter) {
		// Create a temporary image to extract the letter's size
		BufferedImage tempLetterImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) tempLetterImage.createGraphics();

		if (antiAliasing) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.setFont(font);
		FontMetrics fontMetrics = g.getFontMetrics();

		int letterWidth = fontMetrics.charWidth(letter);
		int letterHeight = fontMetrics.getHeight();
		if (letterWidth <= 0) {
			letterWidth = 1;
		}
		if (letterHeight <= 0) {
			letterHeight = font.getSize();
		}
		tempLetterImage = null;

		// Creates another image holding the character we are creating
		BufferedImage letterImage = new BufferedImage(letterWidth, letterHeight, BufferedImage.TYPE_INT_ARGB);
		g = (Graphics2D) letterImage.createGraphics();

		if (antiAliasing) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.setFont(font);
		g.setColor(Color.WHITE);

		g.drawString(String.valueOf(letter), 0, 0 + fontMetrics.getAscent());
		g.dispose();

		return letterImage;

	}

	/**
	 * Create BufferedImages for alphanumeric characters and symbols, then upload their textures to OpenGL.<br>
	 * Extract only the required characters from the ASCII table.
	 */
	private void createLetterImage() {
		for (int i = 32; i <= 126; i++) {
			// get 32-126 characters and then custom letters
			BufferedImage bi = getLetterBufferedImage((char) i);

			this.letterImageMap.put((char) i, ResourceLoader.getInstance().loadTextureFromBufferedImage(bi));
		}
	}
}
