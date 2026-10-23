package gamescene;

import java.util.logging.Level;
import java.util.logging.Logger;

import enumerate.GameSceneName;
import loader.ResourceLoader;
import manager.GraphicManager;
import manager.InputManager;
import setting.GameSetting;

/**
 * Initializes the next game scene based on the launch settings and loads the required resources.
 */
public class Launcher extends GameScene {

	/**
	 * The next game scene.
	 */
	private GameSceneName nextGameSceneName;

	/**
	 * Flag indicating whether this is the first update.
	 */
	private boolean isFirstUpdate;

	/**
	 * Class constructor.
	 */
	public Launcher() {
		// The following four statements must be included in constructors of classes in the gamescene package.
		this.gameSceneName = GameSceneName.LAUNCH;
		this.isGameEndFlag = false;
		this.isTransitionFlag = false;
		this.nextGameScene = null;
		//////////////////////////////////////

		this.nextGameSceneName = null;

	}

	/**
	 * Constructor that initializes the Launcher scene and sets the next game scene.
	 *
	 * @param nextGameSceneName
	 *            The name of the next game scene.
	 */
	public Launcher(GameSceneName nextGameSceneName) {
		super();

		this.nextGameSceneName = nextGameSceneName;
		this.isFirstUpdate = true;
	}

	@Override
	public void initialize() {
		InputManager.getInstance().setSceneName(GameSceneName.LAUNCH);
	}

	@Override
	public void update() {
		if (this.isFirstUpdate) {
			GraphicManager.getInstance().drawString("Now loading ...", GameSetting.STAGE_WIDTH / 2 - 80, 200);
			this.isFirstUpdate = false;
		} else {
			switch (this.nextGameSceneName.name()) {
			case "PLAY":
				Logger.getAnonymousLogger().log(Level.INFO, "Transition to PLAY");
				Play play = new Play();
				this.setTransitionFlag(true);
				this.setNextGameScene(play);
				break;
			case "REPLAY":
				Logger.getAnonymousLogger().log(Level.WARNING, "Transition to REPLAY");
				Replay replay = new Replay();
				this.setTransitionFlag(true);
				this.setNextGameScene(replay);
				break;
			default:
				Logger.getAnonymousLogger().log(Level.WARNING, "This scene does not exist");
				this.setGameEndFlag(true);
			}
			
			ResourceLoader.getInstance().loadResource();
		}
	}

	@Override
	public void close() {

	}
}
