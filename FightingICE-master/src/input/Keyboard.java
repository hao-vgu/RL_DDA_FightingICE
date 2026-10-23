package input;

import static org.lwjgl.glfw.GLFW.*;

import java.util.Arrays;

import org.lwjgl.glfw.GLFWKeyCallback;

import enumerate.GameSceneName;
import manager.InputManager;

/**
 * Handles keyboard input.
 */
public class Keyboard extends GLFWKeyCallback {

	/**
	 * Array indicating whether each key is pressed.
	 */
	public static boolean[] keys = new boolean[65536];

	/**
	 * Array indicating whether each key was pressed in the previous step.
	 */
	private static boolean[] preKeys = new boolean[65536];

	/**
	 * Class constructor.
	 */
	public Keyboard() {
		Arrays.fill(keys, false);
		Arrays.fill(preKeys, false);
	}

	// The GLFWKeyCallback class is an abstract method that
	// can't be instantiated by itself and must instead be extended
	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		if (key >= 0 && key <= 65536) {
			keys[key] = action != GLFW_RELEASE;
			if (action == GLFW_RELEASE) {
				preKeys[key] = false;
			}
		}
	}

	/**
	 * Check whether the specified key is being pressed.
	 *
	 * @param keycode
	 *            The key to check.
	 * @return {@code true} if the specified key was pressed; {@code false} otherwise
	 */
	public static boolean getKey(int keycode) {
		return keys[keycode];
	}

	/**
	 * Check whether the specified key is held down.
	 *
	 * @param keycode
	 *            The key to check.
	 * @return when the game scene is PLAY
	 *         <p>
	 *         {@code true} if the specified key is pressed,<br>
	 *         {@code false} if the specified key is not pressed.
	 *         <p>
	 *         for any other game scene
	 *         <p>
	 *         {@code true} if the specified key is pressed,<br>
	 *         {@code false} if the key is not pressed or was pressed in the previous step.
	 */
	public static boolean getKeyDown(int keycode) {
		if (InputManager.getInstance().getSceneName() == GameSceneName.PLAY) {
			if (!keys[keycode]) {
				return false;
			} else {
				preKeys[keycode] = true;
				return true;
			}
		} else {
			if (!keys[keycode] || preKeys[keycode]) {
				return false;
			} else {
				preKeys[keycode] = true;
				return true;
			}
		}

	}

	/**
	 * Cleanup.
	 */
	public void close() {

	}
}
