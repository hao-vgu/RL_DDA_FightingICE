package manager;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_I;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_T;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_U;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Y;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import aiinterface.AIController;
import aiinterface.AIInterface;
import aiinterface.SoundController;
import aiinterface.ThreadController;
import enumerate.GameSceneName;
import informationcontainer.AIContainer;
import input.KeyData;
import input.Keyboard;
import loader.ResourceLoader;
import service.SocketServer;
import setting.FlagSetting;
import setting.LaunchSetting;
import struct.AudioData;
import struct.Key;

/**
 * Manages input-related tasks, including AI and keyboard input.
 */
public class InputManager {

	/**
	 * Buffer containing key input.
	 */
	private KeyData buffer;

	/**
	 * Keyboard instance.
	 */
	private Keyboard keyboard;

	/**
	 * Game scene name.
	 */
	private GameSceneName sceneName;

	/**
	 * Default number of devices.
	 */
	public final static int DEFAULT_DEVICE_NUMBER = 2;

	/**
	 * Constant identifying the keyboard as an input device.
	 */
	public final static char DEVICE_TYPE_KEYBOARD = 0;

	/**
	 * Constant identifying an AI as an input device.
	 */
	public final static char DEVICE_TYPE_AI = 1;
	
	public final static char DEVICE_TYPE_EXTERNAL = 2;

	/**
	 * Array specifying the input devices.
	 */
	private char[] deviceTypes;

	/**
	 * InputManager class constructor.<br>
	 * The default input device is the keyboard.
	 */
	private InputManager() {
		Logger.getAnonymousLogger().log(Level.INFO, "Create instance: " + InputManager.class.getName());

		if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.STANDARD_MODE)) {
			keyboard = new Keyboard();
		}
		
		deviceTypes = new char[DEFAULT_DEVICE_NUMBER];
		sceneName = GameSceneName.HOME_MENU;

		for (int i = 0; i < this.deviceTypes.length; i++) {
			this.deviceTypes[i] = DEVICE_TYPE_KEYBOARD;
		}
	}

	/**
	 * Get the singleton InputManager instance.
	 *
	 * @return the singleton InputManager instance
	 */
	public static InputManager getInstance() {
		return InputManagerHolder.instance;
	}

	/**
	 * Holder class that creates the instance the first time getInstance() is called.
	 */
	private static class InputManagerHolder {
		private static final InputManager instance = new InputManager();
	}

	/**
	 * Get the Keyboard instance held by InputManager.
	 *
	 * @return the Keyboard instance
	 */
	public Keyboard getKeyboard() {
		return this.keyboard;
	}
	
	public void initialize() {
		this.deviceTypes = LaunchSetting.deviceTypes.clone();
	}
	
	/**
	 * Called every frame to retrieve keyboard and AI input.
	 */
	public void update() {
		Key[] keys = new Key[this.deviceTypes.length];
		for (int i = 0; i < this.deviceTypes.length; i++) {
			switch (this.deviceTypes[i]) {
			case DEVICE_TYPE_KEYBOARD:
				keys[i] = getKeyFromKeyboard(i == 0);
				break;
			case DEVICE_TYPE_AI:
			case DEVICE_TYPE_EXTERNAL:
				AIController ai = ThreadController.getInstance().getAIController(i == 0);
				keys[i] = getKeyFromAI(ai);
				break;
			default:
				break;
			}
		}

		this.setKeyData(new KeyData(keys));
	}

	/**
	 * Get the key pressed on the keyboard.<br>
	 * Specify whether the player is P1 (true) or P2 (false).
	 *
	 * @param playerNumber
	 *            Player number.
	 * @return information about the pressed keys
	 * @see Key
	 */
	private Key getKeyFromKeyboard(boolean playerNumber) {
		Key key = new Key();
		
		if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.STANDARD_MODE)) {
			if (playerNumber) {
				key.A = Keyboard.getKeyDown(GLFW_KEY_Z);
				key.B = Keyboard.getKeyDown(GLFW_KEY_X);
				key.C = Keyboard.getKeyDown(GLFW_KEY_C);
				key.U = Keyboard.getKeyDown(GLFW_KEY_UP);
				key.D = Keyboard.getKeyDown(GLFW_KEY_DOWN);
				key.R = Keyboard.getKeyDown(GLFW_KEY_RIGHT);
				key.L = Keyboard.getKeyDown(GLFW_KEY_LEFT);
			} else {
				key.A = Keyboard.getKeyDown(GLFW_KEY_T);
				key.B = Keyboard.getKeyDown(GLFW_KEY_Y);
				key.C = Keyboard.getKeyDown(GLFW_KEY_U);
				key.U = Keyboard.getKeyDown(GLFW_KEY_I);
				key.D = Keyboard.getKeyDown(GLFW_KEY_K);
				key.R = Keyboard.getKeyDown(GLFW_KEY_L);
				key.L = Keyboard.getKeyDown(GLFW_KEY_J);
			}
		}

		return key;
	}

	/**
	 * Stop the AI.
	 */
	public void close() {
		this.buffer = new KeyData();
		
		this.deviceTypes = new char[DEFAULT_DEVICE_NUMBER];
	}

	/**
	 * Get the AI key input.
	 *
	 * @param ai
	 *            Controller containing AI information.
	 *
	 * @return AI key input
	 * @see AIController
	 * @see Key
	 */
	private Key getKeyFromAI(AIController ai) {
		if (ai == null)
			return new Key();
		return new Key(ai.getInput());
	}
	
	public AudioData getAudioData() {
		SoundController sound = ThreadController.getInstance().getSoundController();
		if (sound == null)
			return new AudioData();
		return new AudioData(sound.getAudioData());
	}
	
	public void setInput(boolean playerNumber, Key input) {
		AIController ai = ThreadController.getInstance().getAIController(playerNumber);
		if (ai != null) {
			ai.setInput(input);
		}
	}

	/**
	 * Get the key buffer containing the input keys.
	 *
	 * @return the key buffer
	 */
	public KeyData getKeyData() {
		return this.buffer;
	}

	/**
	 * Set the input keys in the buffer.
	 *
	 * @param data
	 *            Input key data.
	 */
	public void setKeyData(KeyData data) {
		this.buffer = data;
	}

	/**
	 * Get the scene name.
	 *
	 * @return the current scene name
	 */
	public GameSceneName getSceneName() {
		return this.sceneName;
	}

	/**
	 * Set the field to the specified scene name.
	 *
	 * @param sceneName
	 *            Scene name.
	 */
	public void setSceneName(GameSceneName sceneName) {
		this.sceneName = sceneName;
	}

}
