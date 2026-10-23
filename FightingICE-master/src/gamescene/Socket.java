package gamescene;

import java.util.logging.Level;
import java.util.logging.Logger;

import enumerate.GameSceneName;
import manager.GraphicManager;
import service.GameService;
import setting.FlagSetting;

/**
 * Runs the game started by the Python process.
 */
public class Socket extends GameScene {

	/**
	 * Class constructor.
	 */
	public Socket() {
		// The following four statements must be included in constructors of classes in the gamescene package.
		this.gameSceneName = GameSceneName.SOCKET;
		this.isGameEndFlag = false;
		this.isTransitionFlag = false;
		this.nextGameScene = null;
		//////////////////////////////////////
	}

	@Override
	public void initialize() {
		FlagSetting.isPyftgReady = true;
		
		Logger.getAnonymousLogger().log(Level.INFO, "Waiting to launch a game");
	}
	
	@Override
	public void update() {
		GraphicManager.getInstance().drawString("Waiting to launch a game", 330, 200);
		
		if (GameService.getInstance().getCloseFlag()) {
			this.setGameEndFlag(true);
			
		} else if (GameService.getInstance().getRunFlag()) {
			GameService.getInstance().setRunFlag(false);
			FlagSetting.isPyftgReady = false;

			Launcher launcher = new Launcher(GameSceneName.PLAY);
			this.setTransitionFlag(true);
			this.setNextGameScene(launcher);
			
		}
	}

	@Override
	public void close() {
		
	}

}
