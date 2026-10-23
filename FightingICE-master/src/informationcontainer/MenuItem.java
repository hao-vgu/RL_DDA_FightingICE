package informationcontainer;

/**
 * Represents a menu item.
 */
public class MenuItem {

	/**
	 * Menu item text.
	 */
	private String string;

	/**
	 * Menu item x-coordinate.
	 */
	private int coordinateX;

	/**
	 * Menu item y-coordinate.
	 */
	private int coordinateY;

	/**
	 * Cursor position index.
	 */
	private int cursorPosition;

	/**
	 * Constructor that creates a MenuItem instance.
	 * Initialize the instance using the text to display, the menu item's X and Y coordinates, and
	 * the cursor index that specifies where to display the selection cursor.
	 *
	 * @param string
	 *            Menu item text.
	 * @param coordinateX
	 *            Menu item x-coordinate.
	 * @param coordinateY
	 *            Menu item y-coordinate.
	 * @param cursorPosition
	 *            Cursor position index.
	 */
	public MenuItem(String string, int coordinateX, int coordinateY, int cursorPosition) {
		this.string = string;
		this.coordinateX = coordinateX;
		this.coordinateY = coordinateY;
		this.cursorPosition = cursorPosition;
	}

	/**
	 * Return the menu item text.
	 *
	 * @return the menu item text
	 */
	public String getString() {
		return this.string;
	}

	/**
	 * Return the menu item x-coordinate.
	 *
	 * @return the menu item x-coordinate
	 */
	public int getCoordinateX() {
		return this.coordinateX;
	}

	/**
	 * Return the menu item y-coordinate.
	 *
	 * @return the menu item y-coordinate
	 */
	public int getCoordinateY() {
		return this.coordinateY;
	}

	/**
	 * Return the menu item cursor index.
	 *
	 * @return the menu item cursor index
	 */
	public int getCursorPosition() {
		return this.cursorPosition;
	}
}
