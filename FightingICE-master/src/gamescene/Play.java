package gamescene;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import aiinterface.ThreadController;
import enumerate.GameSceneName;
import fighting.Fighting;
import informationcontainer.RoundResult;
import input.KeyData;
import input.Keyboard;
import loader.ResourceLoader;
import manager.GraphicManager;
import manager.InputManager;
import manager.SoundManager;
import setting.FlagSetting;
import setting.GameSetting;
import setting.LaunchSetting;
import struct.AudioData;
import struct.AudioSource;
import struct.FrameData;
import struct.GameData;
import struct.ScreenData;
import util.DebugActionData;
import util.LogWriter;
import util.ResourceDrawer;
import util.WaveFileWriter;
import util.StyleCSVLogger;
import styletrackers.CharacterStyleTracker;

/**
 * Handles the gameplay scene.
 */
public class Play extends GameScene {

	/**
	 * Instance of the class that processes a match.
	 */
	private Fighting fighting;

	/**
	 * Current frame.
	 */
	private int nowFrame;

	/**
	 * Elapsed frames during the initialization performed before each round.
	 */
	private int elapsedBreakTime;

	/**
	 * Current round.
	 */
	private int currentRound;

	/**
	 * Flag indicating whether a round is starting.
	 */
	private boolean roundStartFlag;
	private boolean roundEndFlag;

	/**
	 * Frame data containing game information, such as character data, after match processing.
	 */
	private FrameData frameData;

	/**
	 * Screen data after match processing.
	 */
	private ScreenData screenData;

	/**
	 * P1 and P2 input used to process the match.
	 */
	private KeyData keyData;

	/**
	 * List of P1 and P2 remaining health and elapsed time at the end of each round.
	 */
	private ArrayList<RoundResult> roundResults;

	/**
	 * Output stream used to write the replay file.
	 */
	private DataOutputStream dos;

	/**
	 * String representing the current date and time.
	 */
	private String timeInfo;

	private int endFrame;
	
	private long roundStartTime;
	private long currentFrameTime;

	private AudioData audioData;
	
	private AudioSource audioSource;

	
	/**
	 * Logger for style information.
	 */
	private StyleCSVLogger styleLogger;

	
	/**
	 * Class constructor.
	 */
	public Play() {
		// The following four statements must be included in constructors of classes in the gamescene package.
		this.gameSceneName = GameSceneName.PLAY;
		this.isGameEndFlag = false;
		this.isTransitionFlag = false;
		this.nextGameScene = null;
		//////////////////////////////////////
	}

	@Override
	public void initialize() {
		InputManager.getInstance().setSceneName(GameSceneName.PLAY);

		this.fighting = new Fighting();
		this.fighting.initialize();

		this.nowFrame = 0;
		this.elapsedBreakTime = 0;
		this.currentRound = 1;
		this.roundStartFlag = true;
		this.roundEndFlag = false;
		this.endFrame = -1;
		this.roundStartTime = 0;
		this.currentFrameTime = 0;
		this.frameData = new FrameData();
		this.screenData = new ScreenData();
		this.audioData = new AudioData();
		this.keyData = new KeyData();
		this.roundResults = new ArrayList<RoundResult>();
		
		this.audioSource = SoundManager.getInstance().createAudioSource();

		this.timeInfo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd-HH.mm.ss", Locale.ENGLISH));

		if (!FlagSetting.trainingModeFlag) {
			openReplayFile();
		}

		if (FlagSetting.debugActionFlag) {
			DebugActionData.getInstance().initialize();
		}

		if (FlagSetting.jsonFlag) {
			String jsonName = LogWriter.getInstance().createOutputFileName("./log/replay/", this.timeInfo);
			LogWriter.getInstance().initJson(jsonName + ".json");
		}

		GameData gameData = new GameData(this.fighting.getCharacters());
		
		InputManager.getInstance().initialize();

		ThreadController.getInstance().createAIController();
		ThreadController.getInstance().createSoundController();
		ThreadController.getInstance().createStreamControllers();
		ThreadController.getInstance().startAllThreads(gameData);
	}

	@Override
	public void update() {
		if (this.currentRound <= GameSetting.ROUND_MAX) {
			// Initialize at the start of a round.
			if (this.roundStartFlag) {
				initRound();

			} else if (this.elapsedBreakTime < GameSetting.BREAKTIME_FRAME_NUMBER) {
				// break time
				processingBreakTime();
				this.elapsedBreakTime++;

			} else if (this.roundEndFlag) {
				// Log style tracker results at the end of the round
				CharacterStyleTracker styleTracker = fighting.getCharacterStyleTracker();
				CharacterStyleTracker styleTrackerP2 = fighting.getCharacterStyleTrackerP2();
				if (styleTracker != null && styleTrackerP2 != null) {
					try {
						if (styleLogger == null) {
							styleTracker.normalizeScore(this.nowFrame);
							styleTrackerP2.normalizeScore(this.nowFrame);
							styleLogger = new StyleCSVLogger(this.timeInfo);
							styleLogger.writeDualHeader(styleTracker, styleTrackerP2);
						}
						styleLogger.logBoth(styleTracker, styleTrackerP2);
					} catch (IOException e) {
						System.err.println("Failed to write style log: " + e.getMessage());
					}
				} else if (styleTracker != null) {
					try {
						if (styleLogger == null) {
							styleTracker.normalizeScore(this.nowFrame);
							styleLogger = new StyleCSVLogger(this.timeInfo);
							styleLogger.writeHeader(styleTracker);
						}
						styleLogger.log(styleTracker);
					} catch (IOException e) {
						System.err.println("Failed to write style log: " + e.getMessage());
					}
				}
				else if (styleTrackerP2 != null) {	
					try {
						if (styleLogger == null) {
							styleTrackerP2.normalizeScore(this.nowFrame);
							styleLogger = new StyleCSVLogger(this.timeInfo);
							styleLogger.writeHeader(styleTrackerP2);
						}
						styleLogger.log(styleTrackerP2);
					} catch (IOException e) {
						System.err.println("Failed to write style log: " + e.getMessage());
					}
				}
				// round end
				processingRoundEnd();
			} else {
				// processing
				processingGame();
				if (this.endFrame == -1 || this.endFrame % 30 == 0) {
					this.nowFrame++;
				}
			}

		} else {
			processingGameEnd();
			Logger.getAnonymousLogger().log(Level.INFO, "Game over");

			Result result = new Result(this.roundResults, this.timeInfo);
			this.setTransitionFlag(true);
			this.setNextGameScene(result);
		}

		if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.STANDARD_MODE)) {
			if (Keyboard.getKeyDown(GLFW_KEY_SPACE)) {
				System.out.println("P1 x:" + this.frameData.getCharacter(true).getCenterX() + "\n" + "P2 x:"
						+ this.frameData.getCharacter(false).getCenterX() + "\n" + "P1 Left:"
						+ this.frameData.getCharacter(true).getLeft() + "\n" + "P1 Right:"
						+ this.frameData.getCharacter(true).getRight() + "\n" + "P2 Left:"
						+ this.frameData.getCharacter(false).getLeft() + "\n" + "P2 Right:"
						+ this.frameData.getCharacter(false).getRight() + "\n");
			}

			if (Keyboard.getKeyDown(GLFW_KEY_ESCAPE)) {
				HomeMenu homeMenu = new HomeMenu();
				this.setTransitionFlag(true);
				this.setNextGameScene(homeMenu);
			}
		}
	}

	/**
	 * Initialize match information, the current frame, and other state at the start of each round.
	 */
	private void initRound() {
		this.fighting.initRound();
		this.nowFrame = 0;
		this.roundStartFlag = false;
		this.elapsedBreakTime = 0;
		this.keyData = new KeyData();

		ThreadController.getInstance().clear();
		
		String fileName = LogWriter.getInstance().createOutputFileName("./log/sound/", this.timeInfo + "_" + this.currentRound + ".wav");
		WaveFileWriter.getInstance().initializeWaveFile(fileName);
	}

	/**
	 * Process the interval at the start of each round.
	 */
	private void processingBreakTime() {
		this.roundStartTime = System.nanoTime();

		GraphicManager.getInstance().resetScreen();
		GraphicManager.getInstance().drawString("Waiting for Round Start", 350, 200);
	}
	
	/**
	 * Process the match.<br>
	 *
	 * 1. Receive input from P1 and P2.<br>
	 * 2. Process the match.<br>
	 * 3. Get the FrameData after match processing.<br>
	 * 4. Write the log to the replay file.<br>
	 * 5. Draw the game screen.<br>
	 * 6. Get the ScreenData after match processing.<br>
	 * 7. Pass the FrameData and ScreenData to the AI.<br>
	 * 8. Check whether the round has ended.<br>
	 */
	private void processingGame() {
		this.currentFrameTime = System.nanoTime();
		
		if (this.nowFrame == 0) {
			this.audioData = new AudioData();
			
			SoundManager.getInstance().play2(audioSource, 
					SoundManager.getInstance().getBackGroundMusicBuffer(), GameSetting.STAGE_WIDTH / 2, GameSetting.STAGE_HEIGHT / 2, true);
		} else if (FlagSetting.enableBuiltinSound) {
			this.audioData = new AudioData(SoundManager.getInstance().getVirtualRenderer().sampleAudio());
		} else {
			this.audioData = InputManager.getInstance().getAudioData();
		}
		
		if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.HEADLESS_MODE)) {
			this.screenData = new ScreenData(GraphicManager.getInstance().getScreenImage());
		}
		
		if (this.endFrame != -1) {
			this.keyData = new KeyData();
			if (this.endFrame % 30 == 0) {
				this.fighting.processingFight(this.nowFrame, this.keyData);
			}
		} else {
			this.keyData = new KeyData(InputManager.getInstance().getKeyData());
			this.fighting.processingFight(this.nowFrame, this.keyData);
		}

		this.frameData = this.fighting.createFrameData(this.nowFrame, this.currentRound);
		
		// Set the FrameData for the AI.
		ThreadController.getInstance().setFrameData(this.frameData, this.screenData, this.audioData);
		
		SoundManager.getInstance().playback(this.audioSource, this.audioData.getRawShortDataAsBytes());
		WaveFileWriter.getInstance().addSample(this.audioData.getRawShortDataAsBytes());
		
		GraphicManager.getInstance().resetScreen();
		if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.HEADLESS_MODE)) {
			// Draw the screen using the Drawer class.
			ResourceDrawer.getInstance().drawResource(this.fighting.getCharacters(), this.fighting.getProjectileDeque(),
					this.fighting.getHitEffectList(), this.frameData.getRemainingTimeMilliseconds(), this.currentRound, FlagSetting.visualVisibleOnRender);
		}

		// Write the replay log.
		if (!FlagSetting.trainingModeFlag) {
			LogWriter.getInstance().outputLog(this.dos, this.keyData, this.fighting.getCharacters());
		}

		if (FlagSetting.jsonFlag) {
			LogWriter.getInstance().updateJson(this.frameData, this.keyData);
		}

		// Count the number of times each action was performed by P1 and P2.
		if (FlagSetting.debugActionFlag) {
			DebugActionData.getInstance().countPlayerAction(this.fighting.getCharacters());
		}
		
		// End the round if a player's health reaches 0 or time runs out.
		if (isBeaten() || isTimeOver()) {
			this.roundEndFlag = true;
		}
	}

	/**
	 * Process the end of a round.
	 */
	private void processingRoundEnd() {
		Logger.getAnonymousLogger().log(Level.INFO, String.format("Round Duration: %.3f seconds (Expected %.3f)", 
				(double) (currentFrameTime - roundStartTime) / 1e9, (double) this.nowFrame / 60));
		
		SoundManager.getInstance().stopAll();
		SoundManager.getInstance().stopPlayback(this.audioSource);
		WaveFileWriter.getInstance().writeToFile();

		if (FlagSetting.slowmotion) {
			if (this.endFrame > GameSetting.ROUND_EXTRAFRAME_NUMBER) {
				this.fighting.processingRoundEnd();
				RoundResult roundResult = new RoundResult(this.frameData);
				this.roundResults.add(roundResult);

				// Pass the result to the AI.
				ThreadController.getInstance().sendRoundResult(roundResult);
				
				this.currentRound++;
				this.roundStartFlag = true;
				this.roundEndFlag = false;
				this.endFrame = -1;

				// Write the counts of actions performed by P1 and P2 to a CSV file.
				if (FlagSetting.debugActionFlag) {
					DebugActionData.getInstance().outputActionCount();
				}
			} else {
				this.endFrame++;
			}

		} else {
			this.endFrame = 0;
			this.fighting.processingRoundEnd();
			RoundResult roundResult = new RoundResult(this.frameData);
			this.roundResults.add(roundResult);

			// Pass the result to the AI.
			ThreadController.getInstance().sendRoundResult(roundResult);
			
			this.currentRound++;
			this.roundStartFlag = true;
			this.roundEndFlag = false;
			this.endFrame = -1;

			// Write the counts of actions performed by P1 and P2 to a CSV file.
			if (FlagSetting.debugActionFlag) {
				DebugActionData.getInstance().outputActionCount();
			}
		}
	}
	
	private void processingGameEnd() {
		ThreadController.getInstance().gameEnd();
	}

	/**
	 * Check whether a character has been defeated.
	 *
	 * @return {@code true} if P1 or P2 was defeated; {@code false} otherwise
	 */
	private boolean isBeaten() {
		return FlagSetting.limitHpFlag
				&& (this.frameData.getCharacter(true).getHp() <= 0 || this.frameData.getCharacter(false).getHp() <= 0);
	}

	/**
	 * Check whether the time limit for one round has elapsed.<br>
	 * In Training mode, compare against the maximum value of an Integer.
	 *
	 * @return {@code true} if the time limit for one round has elapsed; {@code false} otherwise
	 */
	private boolean isTimeOver() {
		if (FlagSetting.trainingModeFlag) {
			return false;
		} else {
			return this.nowFrame >= GameSetting.ROUND_FRAME_NUMBER - 1;
		}
	}

	/**
	 * Create a replay file and write header information, such as the indices of the selected characters.
	 */
	private void openReplayFile() {
		String fileName = LogWriter.getInstance().createOutputFileName("./log/replay/", this.timeInfo);
		this.dos = ResourceLoader.getInstance().openDataOutputStream(fileName + ".dat");

		LogWriter.getInstance().writeHeader(this.dos);
	}

	@Override
	public void close() {
		this.fighting.close();
		this.fighting = null;
		this.frameData = null;
		this.screenData = null;
		this.keyData = null;
		this.roundResults.clear();
		
		InputManager.getInstance().close();
		ThreadController.getInstance().close();

		if (FlagSetting.debugActionFlag) {
			DebugActionData.getInstance().closeAllWriters();
		}

		try {
			if (this.dos != null) {
				this.dos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (FlagSetting.jsonFlag) {
			LogWriter.getInstance().finalizeJson();
		}
		if (styleLogger != null) {
			styleLogger.close();
		}
	}

}
