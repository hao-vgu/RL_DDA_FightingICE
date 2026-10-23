package gamescene;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import java.util.ArrayList;

import enumerate.GameSceneName;
import informationcontainer.MenuItem;
import input.Keyboard;
import loader.ResourceLoader;
import manager.GraphicManager;
import manager.InputManager;
import service.SocketPlayer;
import service.SocketServer;
import setting.FlagSetting;
import setting.GameSetting;
import setting.LaunchSetting;
import setting.ResourceSetting;
import struct.Key;

/**
 * Handles the menu for selecting the AI, characters, and number of repetitions.
 */
public class FightingMenu extends GameScene {

	public static final String DEFAULT_AI_NAME = "Keyboard";
	public static final String DEFAULT_SOUND_NAME = "Default";
	public static final int DEFAULT_AI_INDEX = 1;
	public static final int EXTERNAL_AI_INDEX = 0;
	
	/**
	 * Number of items displayed on the screen.
	 */
	private final int NUMBER_OF_ITEM = 8;

	/**
	 * Array containing each displayed item's index, name, and coordinates.
	 */
	private MenuItem[] menuItems;

	/**
	 * List of all AI names in the ai folder.
	 */
	private ArrayList<String> allAiNames;
	
	private ArrayList<String> allSounds;

	/**
	 * Current cursor position.
	 */
	private int cursorPosition;

	/**
	 * Current selection for the Repeat Count item.
	 */
	private int numberIndex;
	
	private int soundIndex;

	/**
	 * Current selection for the PLAYER item.<br>
	 * Index 0: P1; Index 1: P2
	 */
	private int[] playerIndexes;

	/**
	 * Current selection for the CHARACTER item.<br>
	 * Index 0: P1; Index 1: P2
	 */
	private int[] characterIndexes;

	/** Class constructor. */
	public FightingMenu() {
		// The following four statements must be included in constructors of classes in the gamescene package.
		this.gameSceneName = GameSceneName.FIGHTING_MENU;
		this.isGameEndFlag = false;
		this.isTransitionFlag = false;
		this.nextGameScene = null;
		//////////////////////////////////////
	}

	private boolean isGrpcReady() {
		if (SocketServer.getInstance().isOpen()) {
			return true;
		} else if (this.playerIndexes[0] == EXTERNAL_AI_INDEX && SocketServer.getInstance().getPlayer(0).isCancelled()) {
			return false;
		} else if (this.playerIndexes[1] == EXTERNAL_AI_INDEX && SocketServer.getInstance().getPlayer(1).isCancelled()) {
			return false;
		}
		return true;
	}
	
	@Override
	public void initialize() {
		InputManager.getInstance().setSceneName(GameSceneName.FIGHTING_MENU);

		this.menuItems = new MenuItem[] { 
				new MenuItem("PLAY", 50, 50, 0), new MenuItem("Player 1: ", 75, 90, 1),
				new MenuItem("Player 2: ", 75, 130, 2), new MenuItem("Character 1: ", 75, 170, 3),
				new MenuItem("Character 2: ", 75, 210, 4), new MenuItem("Repeat Count: ", 50, 260, 5),
				new MenuItem("Sound: ", 50, 310, 6), new MenuItem("RETURN", 50, 360, 7) 
				};

		this.playerIndexes = new int[2];
		this.characterIndexes = new int[2];
		this.cursorPosition = 0;
		this.numberIndex = 0;
		this.soundIndex = 0;

		this.allAiNames = ResourceLoader.getInstance().loadFileNames("./data/ai", ".jar");
		allAiNames.add(EXTERNAL_AI_INDEX, "External AI");
		allAiNames.add(DEFAULT_AI_INDEX, DEFAULT_AI_NAME);
		
		this.allSounds = ResourceLoader.getInstance().loadSoundNames();
		allSounds.add(0, DEFAULT_SOUND_NAME);
	}

	@Override
	public void update() {
		Key key = InputManager.getInstance().getKeyData().getKeys()[0];

		if (key.U) {
			if (this.cursorPosition == 0) {
				this.cursorPosition = this.menuItems[this.NUMBER_OF_ITEM - 1].getCursorPosition();
			} else {
				this.cursorPosition = this.menuItems[this.cursorPosition - 1].getCursorPosition();
			}
		}

		if (key.D) {
			if (this.cursorPosition == this.NUMBER_OF_ITEM - 1) {
				this.cursorPosition = this.menuItems[0].getCursorPosition();
			} else {
				this.cursorPosition = this.menuItems[this.cursorPosition + 1].getCursorPosition();
			}
		}

		switch (this.cursorPosition) {
		// When the cursor is on PLAY.
		case 0:
			if (key.A && this.isGrpcReady()) {
				// Set the launch settings.
				for (int i = 0; i < 2; i++) {
					LaunchSetting.aiNames[i] = this.allAiNames.get(this.playerIndexes[i]);
					LaunchSetting.characterNames[i] = GameSetting.CHARACTERS[this.characterIndexes[i]];

					if (LaunchSetting.aiNames[i].equals(DEFAULT_AI_NAME)) {
						LaunchSetting.deviceTypes[i] = InputManager.DEVICE_TYPE_KEYBOARD;
					} else if (this.playerIndexes[i] == EXTERNAL_AI_INDEX) {
						LaunchSetting.aiNames[i] = SocketServer.getInstance().getPlayer(i).getName();
						LaunchSetting.deviceTypes[i] = InputManager.DEVICE_TYPE_EXTERNAL;
					} else {
						LaunchSetting.deviceTypes[i] = InputManager.DEVICE_TYPE_AI;
					}
				}

				LaunchSetting.repeatNumber = GameSetting.REPEAT_NUMBERS[this.numberIndex];
				if (LaunchSetting.repeatNumber > 1) {
					FlagSetting.automationFlag = true;
				}

				LaunchSetting.soundName = this.allSounds.get(this.soundIndex);
				if (LaunchSetting.soundName.equals(DEFAULT_SOUND_NAME)) {
					ResourceSetting.SOUND_DIRECTORY = "./data/sounds/";
				} else {
					ResourceSetting.SOUND_DIRECTORY = String.format("./data/sounds/%s/", LaunchSetting.soundName);
				}

				// Set the Launcher's next scene.
				Launcher launcher = new Launcher(GameSceneName.PLAY);
				this.setTransitionFlag(true);
				this.setNextGameScene(launcher);
			}
			break;

		// When the cursor is on PLAYER1.
		case 1:
			if (key.R) {
				if (this.playerIndexes[0] == this.allAiNames.size() - 1) {
					this.playerIndexes[0] = 0;
				} else {
					this.playerIndexes[0]++;
				}
			}
			if (key.L) {
				if (this.playerIndexes[0] == 0) {
					this.playerIndexes[0] = this.allAiNames.size() - 1;
				} else {
					this.playerIndexes[0]--;
				}
			}
			break;

		// When the cursor is on PLAYER2.
		case 2:
			if (key.R) {
				if (this.playerIndexes[1] == this.allAiNames.size() - 1) {
					this.playerIndexes[1] = 0;
				} else {
					this.playerIndexes[1]++;
				}
			}
			if (key.L) {
				if (this.playerIndexes[1] == 0) {
					this.playerIndexes[1] = this.allAiNames.size() - 1;
				} else {
					this.playerIndexes[1]--;
				}
			}
			break;

		// When the cursor is on CHARACTER1.
		case 3:
			if (key.R) {
				if (this.characterIndexes[0] == GameSetting.CHARACTERS.length - 1) {
					this.characterIndexes[0] = 0;
				} else {
					this.characterIndexes[0]++;
				}
			}
			if (key.L) {
				if (this.characterIndexes[0] == 0) {
					this.characterIndexes[0] = GameSetting.CHARACTERS.length - 1;
				} else {
					this.characterIndexes[0]--;
				}
			}
			break;

		// When the cursor is on CHARACTER2.
		case 4:
			if (key.R) {
				if (this.characterIndexes[1] == GameSetting.CHARACTERS.length - 1) {
					this.characterIndexes[1] = 0;
				} else {
					this.characterIndexes[1]++;
				}
			}
			if (key.L) {
				if (this.characterIndexes[1] == 0) {
					this.characterIndexes[1] = GameSetting.CHARACTERS.length - 1;
				} else {
					this.characterIndexes[1]--;
				}
			}
			break;
		// When the cursor is on Repeat Count.
		case 5:
			if (key.R) {
				if (this.numberIndex == GameSetting.REPEAT_NUMBERS.length - 1) {
					this.numberIndex = 0;
				} else {
					this.numberIndex++;
				}
			}
			if (key.L) {
				if (this.numberIndex == 0) {
					this.numberIndex = GameSetting.REPEAT_NUMBERS.length - 1;
				} else {
					this.numberIndex--;
				}
			}
			break;
		// Sound
		case 6:
			if (key.R) {
				if (this.soundIndex == this.allSounds.size() - 1) {
					this.soundIndex = 0;
				} else {
					this.soundIndex++;
				}
			}
			if (key.L) {
				if (this.soundIndex == 0) {
					this.soundIndex = this.allSounds.size() - 1;
				} else {
					this.soundIndex--;
				}
			}
			break;
		// When the cursor is on RETURN.
		case 7:
			if (key.A) {
				HomeMenu homeMenu = new HomeMenu();
				this.setTransitionFlag(true);
				this.setNextGameScene(homeMenu);
			}
			break;

		default:
			break;
		}

		if (Keyboard.getKeyDown(GLFW_KEY_ESCAPE)) {
			HomeMenu homeMenu = new HomeMenu();
			this.setTransitionFlag(true);
			this.setNextGameScene(homeMenu);
		}
		// Draw the screen.
		this.drawScreen();
	}

	private String getAIName(int i) {
		SocketPlayer player = SocketServer.getInstance().getPlayer(i);
		if (this.playerIndexes[i] == EXTERNAL_AI_INDEX && !player.isCancelled()) {
			return String.format("%s (%s AI)", player.getName(), player.isBlind() ? "Blind" : "Visual");
		}
		return this.allAiNames.get(this.playerIndexes[i]);
	}
	
	/**
	 * Draw the match settings menu.
	 */
	private void drawScreen() {
		GraphicManager.getInstance().drawString(this.menuItems[0].getString(), this.menuItems[0].getCoordinateX(), 
				this.menuItems[0].getCoordinateY());
		GraphicManager.getInstance().drawString(this.menuItems[1].getString() + this.getAIName(0),
				this.menuItems[1].getCoordinateX(), this.menuItems[1].getCoordinateY());
		GraphicManager.getInstance().drawString(this.menuItems[2].getString() + this.getAIName(1),
				this.menuItems[2].getCoordinateX(), this.menuItems[2].getCoordinateY());
		GraphicManager.getInstance().drawString(this.menuItems[3].getString() + GameSetting.CHARACTERS[this.characterIndexes[0]],
				this.menuItems[3].getCoordinateX(), this.menuItems[3].getCoordinateY());
		GraphicManager.getInstance().drawString(this.menuItems[4].getString() + GameSetting.CHARACTERS[this.characterIndexes[1]],
				this.menuItems[4].getCoordinateX(), this.menuItems[4].getCoordinateY());
		GraphicManager.getInstance().drawString(this.menuItems[5].getString() + GameSetting.REPEAT_NUMBERS[this.numberIndex],
				this.menuItems[5].getCoordinateX(), this.menuItems[5].getCoordinateY());
		GraphicManager.getInstance().drawString(this.menuItems[6].getString() + this.allSounds.get(this.soundIndex),
				this.menuItems[6].getCoordinateX(), this.menuItems[6].getCoordinateY());
		GraphicManager.getInstance().drawString(this.menuItems[7].getString(), this.menuItems[7].getCoordinateX(), 
				this.menuItems[7].getCoordinateY());
		GraphicManager.getInstance().drawString("=>", this.menuItems[this.cursorPosition].getCoordinateX() - 30,
				this.menuItems[this.cursorPosition].getCoordinateY());
	}

	@Override
	public void close() {
		this.menuItems = null;
		this.allAiNames.clear();
		this.allSounds.clear();
		this.characterIndexes = null;
		this.playerIndexes = null;
	}

}