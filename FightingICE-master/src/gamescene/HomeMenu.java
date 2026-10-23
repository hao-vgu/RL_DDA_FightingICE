package gamescene;

import java.util.ArrayList;

import enumerate.GameSceneName;
import informationcontainer.MenuItem;
import loader.ResourceLoader;
import manager.GraphicManager;
import manager.InputManager;
import setting.LaunchSetting;
import struct.Key;

/**
 * Handles the menu for starting a match, playing a replay, or exiting the game.
 */
public class HomeMenu extends GameScene {

	/**
	 * Number of items displayed on the screen.
	 */
	private final int NUMBER_OF_ITEM = 3;

	/**
	 * Array containing each displayed item's index, name, and coordinates.
	 */
	private MenuItem[] menuItems;

	/**
	 * List of all replay names in the replay folder.
	 */
	private ArrayList<String> allReplayNames;

	/**
	 * Current cursor position.
	 */
	private int cursorPosition;

	/**
	 * Current selection for the REPLAY item.
	 */
	private int replayIndex;

	/**
	 * Class constructor.
	 */
	public HomeMenu() {
		// The following four statements must be included in constructors of classes in the gamescene package.
		this.gameSceneName = GameSceneName.HOME_MENU;
		this.isGameEndFlag = false;
		this.isTransitionFlag = false;
		this.nextGameScene = null;
		//////////////////////////////////////
	}

	@Override
	public void initialize() {
		InputManager.getInstance().setSceneName(GameSceneName.HOME_MENU);

		this.menuItems = new MenuItem[] {
				new MenuItem("FIGHT ", 50, 50, 0),
				new MenuItem("REPLAY : ", 50, 100, 1),
				new MenuItem("EXIT ", 50, 310, 2)
		};
		this.cursorPosition = 0;
		this.replayIndex = 0;

		this.allReplayNames = ResourceLoader.getInstance().loadFileNames("./log/replay/", ".dat");
		if (this.allReplayNames.size() == 0) {
			this.allReplayNames.add("None");
		}
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
		// When the cursor is on FIGHT.
		case 0:
			if (key.A) {
				FightingMenu fightingMenu = new FightingMenu();
				this.setTransitionFlag(true);
				this.setNextGameScene(fightingMenu);
			}
			break;

		// When the cursor is on REPLAY.
		case 1:
			if (key.R) {
				if (this.replayIndex == this.allReplayNames.size() - 1) {
					this.replayIndex = 0;
				} else {
					this.replayIndex++;
				}
			}

			if (key.L) {
				if (this.replayIndex == 0) {
					this.replayIndex = this.allReplayNames.size() - 1;
				} else {
					this.replayIndex--;
				}
			}

			if (key.A) {
				LaunchSetting.replayName = this.allReplayNames.get(this.replayIndex);
				
				if (!LaunchSetting.replayName.equals("None")) {
					// Set the Launcher's next scene.
					Launcher launcher = new Launcher(GameSceneName.REPLAY);
					this.setTransitionFlag(true); // Set the transition request from the current scene to true.
					this.setNextGameScene(launcher); // Set the next scene.
				}
			}
			break;

		// When the cursor is on EXIT.
		case 2:
			if (key.A) {
				this.setGameEndFlag(true);
			}
			break;

		default:
			break;
		}

		this.drawScreen();

	}

	/**
	 * Draw the match settings menu.
	 */
	private void drawScreen() {
		GraphicManager.getInstance().drawString(this.menuItems[0].getString(), this.menuItems[0].getCoordinateX(),
				this.menuItems[0].getCoordinateY());
		GraphicManager.getInstance().drawString(
				this.menuItems[1].getString() + this.allReplayNames.get(this.replayIndex),
				this.menuItems[1].getCoordinateX(), this.menuItems[1].getCoordinateY());
		GraphicManager.getInstance().drawString(this.menuItems[2].getString(), this.menuItems[2].getCoordinateX(),
				this.menuItems[2].getCoordinateY());
		GraphicManager.getInstance().drawString("=>", this.menuItems[cursorPosition].getCoordinateX() - 30,
				this.menuItems[this.cursorPosition].getCoordinateY());
	}

	@Override
	public void close() {
		this.allReplayNames.clear();
		this.menuItems = null;
	}

}
