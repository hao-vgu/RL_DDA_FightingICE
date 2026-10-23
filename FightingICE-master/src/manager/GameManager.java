package manager;

import gamescene.GameScene;

/**
 * Manages initialization, updates, and cleanup for each game scene.
 */
public abstract class GameManager {

	/**
	 * Current game scene.<br>
	 * Can be Menu, Launch, Play, Result, and other scenes.
	 *
	 * @see GameScene
	 */
	protected GameScene currentGameScene;

	/**
	 * Flag indicating whether a request to end the game was made.
	 */
	protected boolean isExitFlag;

	/**
	 * Class constructor.<br>
	 * Initialize the instance with no current game scene and no request to end the game (false).
	 */
	public GameManager() {
		this.currentGameScene = null;
		this.isExitFlag = false;
	}

	/**
	 * Abstract initialization method.<br>
	 * Override this method in subclasses of GameManager.
	 */
	public abstract void initialize();

	/**
	 * Abstract cleanup method.<br>
	 * Override this method in subclasses of GameManager.
	 */
	public abstract void close();

	/**
	 * Update the current game scene.<br>
	 * If the game has not ended and no transition was requested, update the current game scene.<br>
	 * If the game has not ended and a transition was requested, clean up the current scene and perform the transition.<br>
	 * If the game has ended, set the end-request flag to true.
	 */
	public void update() {
		InputManager.getInstance().update();

		if (!currentGameScene.isGameEnd()) {
			if (currentGameScene.isTransition()) {

				// Clean up the current scene.
				currentGameScene.close();

				// Set the next scene as the current scene and initialize it.
				currentGameScene = currentGameScene.getNextGameScene();
				currentGameScene.initialize();
			}

			// Update the current scene.
			currentGameScene.update();
		} else {
			this.isExitFlag = true;
		}
	}

	/**
	 * Start the game.<br>
	 * Set the specified game scene as the current scene and start the game.
	 *
	 * @param startGameScene
	 *            Game scene to start.
	 */
	public void startGame(GameScene startGameScene) {
		this.currentGameScene = startGameScene;
		this.currentGameScene.initialize();
	}

	/**
	 * Return the flag indicating whether a request to end the game was made.<br>
	 * Used to determine whether GameManager should shut down.
	 *
	 * @return the flag indicating whether a request to end the game was made
	 */
	public boolean isExit() {
		return this.isExitFlag;
	}

}
