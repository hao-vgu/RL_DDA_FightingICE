package gamescene;

import enumerate.GameSceneName;

/**
 * Base class that contains functionality shared by game scenes such as Launcher and Play.
 */
public class GameScene {

	/*
	 * Name of the current game scene.
	 */
	protected GameSceneName gameSceneName;

	/**
	 * Flag indicating whether the game has ended.
	 */
	protected boolean isGameEndFlag;

	/**
	 * Flag indicating whether a transition to the next game scene was requested.
	 */
	protected boolean isTransitionFlag;

	/**
	 * The next game scene.
	 */
	protected GameScene nextGameScene;

	/**
	 * Class constructor.
	 */
	public GameScene() {
		this.gameSceneName = GameSceneName.HOME_MENU;
		this.isGameEndFlag = false;
		this.isTransitionFlag = false;
		this.nextGameScene = null;
	}

	/**
	 * Copy constructor that creates a copy of the specified gameScene instance.
	 *
	 * @param gameScene
	 *            The specified game scene.
	 */
	public GameScene(GameScene gameScene) {
		this.gameSceneName = gameScene.getCurrentSceneName();
		this.isGameEndFlag = gameScene.isGameEndFlag;
		this.isTransitionFlag = gameScene.isTransitionFlag;
		this.nextGameScene = gameScene.getNextGameScene();

	}

	/**
	 * Constructor that creates an instance from the specified data.
	 *
	 * @param gameSceneName
	 *            The specified game scene name.
	 * @param isEndFlag
	 *            Flag indicating whether the current scene has ended.
	 * @param isTransitionFlag
	 *            Flag indicating whether a transition was requested.
	 * @param nextGameScene
	 *            The next game scene.
	 *
	 */
	public GameScene(GameSceneName gameSceneName, boolean isEndFlag, boolean isTransitionFlag,
			GameScene nextGameScene) {
		this.gameSceneName = gameSceneName;
		this.isGameEndFlag = isEndFlag;
		this.isTransitionFlag = isTransitionFlag;
		this.nextGameScene = nextGameScene;
	}

	/**
	 * Initialize the current game scene.
	 */
	public void initialize() {
	}

	/**
	 * Update the current game scene.
	 */
	public void update() {
	}

	/**
	 * Clean up the current game scene.
	 */
	public void close() {
	}

	/**
	 * Get the next game scene.
	 *
	 * @return the next game scene
	 */
	public GameScene getNextGameScene() {
		return nextGameScene;
	}

	/**
	 * Get the name of the current game scene.
	 *
	 * @return the current game scene name
	 */
	public GameSceneName getCurrentSceneName() {
		return this.gameSceneName;
	}

	/**
	 * Set the next game scene.
	 *
	 * @param next
	 *            The next game scene.
	 */
	public void setNextGameScene(GameScene next) {
		this.nextGameScene = next;
	}

	/**
	 * Set the name of the current game scene.
	 *
	 * @param gameSceneName
	 *            The current game scene name.
	 */
	public void setCurrentSceneName(GameSceneName gameSceneName) {
		this.gameSceneName = gameSceneName;
	}

	/**
	 * Check whether a request to end the game was made.
	 *
	 * @return {@code true} if a request to end the game was made; {@code false} otherwise
	 */
	public boolean isGameEnd() {
		return this.isGameEndFlag;
	}

	/**
	 * Check whether a transition to the next game scene was requested.
	 *
	 * @return {@code true} if a transition to the next game scene was requested; {@code false} otherwise
	 */
	public boolean isTransition() {
		return this.isTransitionFlag;
	}

	/**
	 * Set the flag indicating whether a request to end the game was made.<br>
	 * {@code true} if an end request was made; {@code false} otherwise
	 *
	 * @param isEnd
	 *            Flag indicating whether an end request was made.
	 */
	public void setGameEndFlag(boolean isEnd) {
		this.isGameEndFlag = isEnd;
	}

	/**
	 * Set the flag indicating whether a transition to the next game scene was requested.<br>
	 * {@code true} if a transition was requested; {@code false} otherwise
	 *
	 * @param isTransition
	 *            Flag indicating whether a transition was requested.
	 */
	public void setTransitionFlag(boolean isTransition) {
		this.isTransitionFlag = isTransition;
	}

}
