import core.Game;
import manager.DisplayManager;

/**
 * Contains the FightingICE main method.
 */
public class Main {

	/**
	 * Set launch options from the command-line arguments and start the game.<br>
	 * This is the FightingICE main method.
	 *
	 * @param options
	 *            Array containing all command-line arguments.
	 */
	public static void main(String[] options) {
		Game game = new Game();
		game.setOptions(options);
		DisplayManager displayManager = new DisplayManager();

		// Start the game.
		displayManager.start(game);
	}
}
