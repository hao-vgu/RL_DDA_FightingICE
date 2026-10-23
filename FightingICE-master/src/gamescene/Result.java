package gamescene;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;

import java.util.ArrayList;

import enumerate.GameSceneName;
import informationcontainer.AIContainer;
import informationcontainer.RoundResult;
import input.Keyboard;
import manager.GraphicManager;
import manager.InputManager;
import service.SocketServer;
import setting.FlagSetting;
import setting.GameSetting;
import setting.LaunchSetting;
import util.LogWriter;

/**
 * Handles the results scene.
 */
public class Result extends GameScene {

	/**
	 * List of P1 and P2 remaining health and elapsed time at the end of each round.
	 */
	private ArrayList<RoundResult> roundResults;

	/**
	 * String representing the current date and time.
	 */
	private String timeInfo;

	/**
	 * Number of frames to display the results.
	 */
	private int displayedTime;

	/**
	 * Class constructor.
	 */
	public Result() {
		// The following four statements must be included in constructors of classes in the gamescene package.
		this.gameSceneName = GameSceneName.FIGHTING_MENU;
		this.isGameEndFlag = false;
		this.isTransitionFlag = false;
		this.nextGameScene = null;
		//////////////////////////////////////

		this.roundResults = new ArrayList<RoundResult>();
		this.timeInfo = "0";
		this.displayedTime = 0;
	}

	/**
	 * Constructor that sets the round results and current time information, then initializes the replay scene.
	 *
	 * @param roundResults
	 *            List containing the results of each round.
	 * @param timeInfo
	 *            Current time information.
	 */
	public Result(ArrayList<RoundResult> roundResults, String timeInfo) {
		super();

		this.roundResults = new ArrayList<RoundResult>(roundResults);
		this.timeInfo = timeInfo;
		this.displayedTime = 0;
		roundResults.clear();
	}

	@Override
	public void initialize() {
		InputManager.getInstance().setSceneName(GameSceneName.RESULT);

		// Write the point file.
		LogWriter.getInstance().outputResult(this.roundResults, LogWriter.CSV, this.timeInfo);
	}

	@Override
	public void update() {
		int[] positionX = new int[] { GameSetting.STAGE_WIDTH / 2 - 70, GameSetting.STAGE_WIDTH / 2 + 10 };

		for (int i = 0; i < this.roundResults.size(); i++) {
			String[] score = new String[] { String.valueOf(this.roundResults.get(i).getRemainingHPs()[0]),
					String.valueOf(this.roundResults.get(i).getRemainingHPs()[1]) };

			// Draw the score.
			GraphicManager.getInstance().drawString(score[0], positionX[0], 50 + i * 100);
			GraphicManager.getInstance().drawString(score[1], positionX[1], 50 + i * 100);

			// Print "Win!" or "Draw" beside the score as appropriate.
			switch (getWinPlayer(i)) {
			case 1:
				GraphicManager.getInstance().drawString("Win !", positionX[0] - 100, 50 + i * 100);
				break;
			case -1:
				GraphicManager.getInstance().drawString("Win !", positionX[1] + 80, 50 + i * 100);
				break;
			default:
				GraphicManager.getInstance().drawString("Draw", positionX[0] - 100, 50 + i * 100);
				GraphicManager.getInstance().drawString("Draw", positionX[1] + 80, 50 + i * 100);
				break;
			}
		}

		endProcess();
	}

	@Override
	public void close() {
		this.roundResults.clear();
		this.displayedTime = 0;
	}

	/**
	 * Return which player won the round.
	 *
	 * @param i the round number
	 *
	 * @return 0 for a draw, 1 if P1 won, or -1 if P2 won
	 */
	private int getWinPlayer(int i) {
		int[] remainingHPs = this.roundResults.get(i).getRemainingHPs();

		if (remainingHPs[0] == remainingHPs[1]) {
			return 0;
		} else if (remainingHPs[0] > remainingHPs[1]) {
			return 1;
		} else {
			return -1;
		}
	}

	/**
	 * Check whether all AI round-robin matches are complete.
	 *
	 * @return {@code true} if all AI round-robin matches are complete; {@code false} otherwise
	 */
	private boolean endRoundRobin() {
		return (AIContainer.p1Index + 1) == AIContainer.allAINameList.size()
				&& (AIContainer.p2Index + 1) == AIContainer.allAINameList.size();
	}

	/**
	 * Handle the transition from the Result scene to the next scene.
	 */
	private void endProcess() {
		// Handle startup with -a or -n, or with Repeat Count set to 2 or higher.
		if (FlagSetting.automationFlag || FlagSetting.allCombinationFlag || FlagSetting.enablePyftgMode) {
			if (++this.displayedTime > 300) {
				// If repetitions remain.
				if (FlagSetting.automationFlag && LaunchSetting.repeatedCount + 1 < LaunchSetting.repeatNumber) {
					LaunchSetting.repeatedCount++;

					Launcher launcher = new Launcher(GameSceneName.PLAY);
					this.setTransitionFlag(true);
					this.setNextGameScene(launcher);

					// If the AI round-robin matches are not yet complete.
				} else if (FlagSetting.allCombinationFlag) {
					if (++AIContainer.p1Index == AIContainer.allAINameList.size()) {
						AIContainer.p1Index = 0;
						AIContainer.p2Index++;
					}

					// Check whether the round-robin matches are complete.
					if (!endRoundRobin()) {
						Launcher launcher = new Launcher(GameSceneName.PLAY);
						this.setTransitionFlag(true);
						this.setNextGameScene(launcher);
					} else {
						this.setGameEndFlag(true);
					}

				} else if (FlagSetting.enablePyftgMode) {
					SocketServer.getInstance().notifyTaskFinished();
					Socket grpc = new Socket();
					this.setTransitionFlag(true);
					this.setNextGameScene(grpc);
				} else {
					this.setGameEndFlag(true);
				}
			}

			// For a regular match, display the Results screen until Enter is pressed.
		} else {
			String string = "Press Enter key to return menu";
			GraphicManager.getInstance().drawString(string, GameSetting.STAGE_WIDTH / 2 - string.length() * 5 - 30,
					400);

			if (Keyboard.getKeyDown(GLFW_KEY_ENTER)) {
				HomeMenu homeMenu = new HomeMenu();
				this.setTransitionFlag(true);
				this.setNextGameScene(homeMenu);
			}
		}
	}
}
