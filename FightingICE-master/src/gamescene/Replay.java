package gamescene;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import aiinterface.ThreadController;
import enumerate.GameSceneName;
import fighting.Fighting;
import informationcontainer.RoundResult;
import input.KeyData;
import input.Keyboard;
import manager.GraphicManager;
import manager.InputManager;
import manager.SoundManager;
import setting.FlagSetting;
import setting.GameSetting;
import setting.LaunchSetting;
import struct.AudioBuffer;
import struct.AudioData;
import struct.AudioSource;
import struct.FrameData;
import struct.GameData;
import struct.Key;
import struct.ScreenData;
import util.ResourceDrawer;

/**
 * Plays back a replay.
 */
public class Replay extends GameScene {

	/**
	 * Instance of the class that processes a match.
	 */
	private Fighting fighting;

	/**
	 * Input stream used to read the log from a replay file.
	 */
	protected DataInputStream dis;

	/**
	 * Current frame.
	 */
	private int nowFrame;

	private AudioSource audioSource;
	
	private AudioBuffer audioBuffer;

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
	
	private long roundStartTime;
	private long currentFrameTime;

	/**
	 * Frame data containing game information, such as character data, after match processing.
	 */
	protected FrameData frameData;

	/**
	 * Screen data after match processing.
	 */
	private ScreenData screenData;
	
	private AudioData audioData;

	/**
	 * P1 and P2 input used to process the match.
	 */
	private KeyData keyData;

	/**
	 * Index specifying the playback speed.
	 */
	private int playSpeedIndex;

	/**
	 * Array of playback speeds.
	 */
	private int[] playSpeedArray;

	/**
	 * Flag indicating whether the round has ended.
	 */
	private boolean isFinished;

	/**
	 * Class constructor.<br>
	 * Open the replay file to be read.
	 */
	public Replay() {
		// The following four statements must be included in constructors of classes in the gamescene package.
		this.gameSceneName = GameSceneName.REPLAY;
		this.isGameEndFlag = false;
		this.isTransitionFlag = false;
		this.nextGameScene = null;
		//////////////////////////////////////
	}

	@Override
	public void initialize() {
		InputManager.getInstance().setSceneName(GameSceneName.REPLAY);

		this.fighting = new Fighting();
		this.fighting.initialize();

		this.nowFrame = 0;
		this.elapsedBreakTime = 0;
		this.currentRound = 1;
		this.roundStartFlag = true;
		this.roundStartTime = 0;
		this.currentFrameTime = 0;

		this.frameData = new FrameData();
		this.screenData = new ScreenData();
		this.audioData = new AudioData();
		this.keyData = new KeyData();
		this.playSpeedIndex = 1;
		this.playSpeedArray = new int[] { 0, 1, 2, 4 };
		this.isFinished = false;
		
		this.audioSource = SoundManager.getInstance().createAudioSource();
		this.audioBuffer = SoundManager.getInstance().createAudioBuffer();

		GameData gameData = new GameData(this.fighting.getCharacters());
		
		ThreadController.getInstance().createSoundController();
		ThreadController.getInstance().startAllThreads(gameData);
		
		try {
			String replayPath = "./log/replay/" + LaunchSetting.replayName + ".dat";
			this.dis = new DataInputStream(new FileInputStream(new File(replayPath)));
			
			readHeader();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update() {
		GraphicManager.getInstance().resetScreen();
		
		if (this.currentRound <= GameSetting.ROUND_MAX) {
			// Initialize at the start of a round.
			if (this.roundStartFlag) {
				initRound();

			} else if (this.elapsedBreakTime < GameSetting.BREAKTIME_FRAME_NUMBER) {
				// break time
				processingBreakTime();
				this.elapsedBreakTime++;

			} else {
				// Update the playback speed.
				updatePlaySpeed();

				// processing
				for (int i = 0; i < this.playSpeedArray[this.playSpeedIndex] && !this.isFinished; i++) {
					processingGame();
					// End the round if a player's health reaches 0 or time runs out.
					if (isBeaten() || isTimeOver()) {
						processingRoundEnd();
						this.nowFrame++;
						break;
					}
					this.nowFrame++;
				}
				
				if (!this.isFinished) {
					// Draw the screen using the Drawer class.
					ResourceDrawer.getInstance().drawResource(this.fighting.getCharacters(),
							this.fighting.getProjectileDeque(), this.fighting.getHitEffectList(),
							this.frameData.getRemainingTimeMilliseconds(), this.currentRound);

					GraphicManager.getInstance().drawString("PlaySpeed:" + this.playSpeedArray[this.playSpeedIndex], 50, 550);
					
					this.screenData = new ScreenData(GraphicManager.getInstance().getScreenImage());
				}
			}

		} else {
			this.processingGameEnd();
			transitionProcess();
		}

		if (Keyboard.getKeyDown(GLFW_KEY_ESCAPE)) {
			// Stop the background music.
			SoundManager.getInstance().stop(audioSource);
			transitionProcess();
		}

	}

	@Override
	public void close() {
		this.fighting = null;
		this.frameData = null;
		this.screenData = null;
		this.keyData = null;
		
		InputManager.getInstance().close();
		ThreadController.getInstance().close();

		try {
			this.dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process the interval at the start of each round.
	 */
	private void processingBreakTime() {
		this.roundStartTime = System.nanoTime();
		GraphicManager.getInstance().drawString("Waiting for Round Start", 350, 200);
	}

	/**
	 * Process the match.<br>
	 *
	 * 1. Receive input from P1 and P2.<br>
	 * 2. Process the match.<br>
	 * 3. Get the FrameData after match processing.<br>
	 */
	private void processingGame() {
		this.currentFrameTime = System.nanoTime();
		this.keyData = createKeyData();
		
		if (this.isFinished) return;
		
		if (this.nowFrame == 0) {
			this.audioData = new AudioData();

			SoundManager.getInstance().play2(audioSource,
					SoundManager.getInstance().getBackGroundMusicBuffer(), GameSetting.STAGE_WIDTH / 2, GameSetting.STAGE_HEIGHT / 2, true);
			if (FlagSetting.enableReplaySound) {
				SoundManager.getInstance().play(this.audioSource, this.audioBuffer);
			}
		} else {
			this.audioData = InputManager.getInstance().getAudioData();
		}

		this.fighting.processingFight(this.nowFrame, this.keyData);
		this.frameData = this.fighting.createFrameData(this.nowFrame, this.currentRound);
		ThreadController.getInstance().setFrameData(this.frameData, this.screenData, this.audioData);
		
		SoundManager.getInstance().playback(audioSource, audioData.getRawShortDataAsBytes());
	}

	/**
	 * Process the end of each round.
	 */
	private void processingRoundEnd() {
		this.isFinished = true;
		this.currentRound++;
		this.roundStartFlag = true;
		
		Logger.getAnonymousLogger().log(Level.INFO, String.format("Round Duration: %.3f seconds (Expected %.3f)", 
				(double) (currentFrameTime - roundStartTime) / 1e9, (double) (this.nowFrame + 1) / 60));
		
		RoundResult roundResult = new RoundResult(this.frameData);
		ThreadController.getInstance().sendRoundResult(roundResult);

		SoundManager.getInstance().stopAll();
		SoundManager.getInstance().stopPlayback(audioSource);
		
		if (FlagSetting.enableReplaySound) {
			this.audioSource.clearBuffer();
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
	 *
	 * @return {@code true} if the time limit for one round has elapsed; {@code false} otherwise
	 */
	private boolean isTimeOver() {
		return this.nowFrame == GameSetting.ROUND_FRAME_NUMBER - 1;
	}
	
	/**
	 * Initialize match information, the current frame, and other state at the start of each round.
	 */
	private void initRound() {
		this.fighting.initRound();
		this.nowFrame = 0;
		this.roundStartFlag = false;
		this.elapsedBreakTime = 0;
		this.isFinished = false;
		
		if (FlagSetting.enableReplaySound) {
			String soundPath = "./log/sound/" + LaunchSetting.replayName + "_" + this.currentRound + ".wav";
			this.audioBuffer.registerSound(soundPath);
		}
	}

	/**
	 * Create key input data for P1 and P2 to use in match processing.<br>
	 *
	 * @return key input data for P1 and P2
	 */
	private KeyData createKeyData() {
		Key[] temp = new Key[2];

		for (int i = 0; i < 2; i++) {
			temp[i] = new Key();
			byte keyByte = 0;

			try {
				this.dis.readBoolean(); // front
				this.dis.readByte(); // remaingFrame
				this.dis.readByte(); // actionOrdinal
				this.dis.readInt(); // hp
				this.dis.readInt(); // energy
				this.dis.readInt(); // x
				this.dis.readInt(); // y
				keyByte = this.dis.readByte();
			} catch (EOFException e) {
				Logger.getAnonymousLogger().log(Level.INFO, "The replay file was finished in the middle");
				try {
					this.dis.close();
					this.isFinished = true;
				} catch (IOException e1) {
					// TODO: Auto-generated catch block.
					e1.printStackTrace();
				}
				this.processingRoundEnd();
				this.processingGameEnd();
				transitionProcess();

				break;
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			temp[i].U = convertItoB(keyByte / 64);
			keyByte %= 64;
			temp[i].R = convertItoB(keyByte / 32);
			keyByte %= 32;
			temp[i].L = convertItoB(keyByte / 16);
			keyByte %= 16;
			temp[i].D = convertItoB(keyByte / 8);
			keyByte %= 8;
			temp[i].C = convertItoB(keyByte / 4);
			keyByte %= 4;
			temp[i].B = convertItoB(keyByte / 2);
			keyByte %= 2;
			temp[i].A = convertItoB(keyByte / 1);
			keyByte %= 1;
		}

		return new KeyData(temp);
	}

	/**
	 * Convert an int value to a boolean.
	 *
	 * @param i
	 *            The int value to convert.
	 *
	 * @return {@code true} if the argument is 1; {@code false} otherwise
	 */
	private boolean convertItoB(int i) {
		return i == 1 ? true : false;
	}

	/**
	 * Read header information such as the selected characters and maximum HP.
	 */
	protected void readHeader() {
		for (int i = 0; i < 2; i++) {
			try {
				int checkMode = dis.readInt();

				// Checks whether fighting mode is limited HP mode or not
				// If it is HP mode, checkMode is less than 0 (e.g. -1)
				if (checkMode < 0) {
					LaunchSetting.maxHp[i] = dis.readInt();
					LaunchSetting.characterNames[i] = GameSetting.CHARACTERS[dis.readInt()];
					FlagSetting.limitHpFlag = true;
				} else {
					LaunchSetting.characterNames[i] = GameSetting.CHARACTERS[checkMode];
					FlagSetting.limitHpFlag = false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Update the replay playback speed.
	 */
	private void updatePlaySpeed() {
		Key key = InputManager.getInstance().getKeyData().getKeys()[0];

		if (key.U) {
			this.playSpeedIndex = ++this.playSpeedIndex % this.playSpeedArray.length;
		}
		if (key.D) {
			this.playSpeedIndex = (--this.playSpeedIndex + this.playSpeedArray.length) % this.playSpeedArray.length;
		}
	}

	/**
	 * Transition to the next scene.
	 */
	private void transitionProcess() {
		HomeMenu homeMenu = new HomeMenu();
		this.setTransitionFlag(true);
		this.setNextGameScene(homeMenu);
	}

	/**
	 * Get the frame data.
	 *
	 * @return the frame data
	 * @see FrameData
	 */
	public FrameData getFrameData() {
		return new FrameData(this.frameData);
	}

	/**
	 * Get the ScreenData.
	 *
	 * @return screen data
	 * @see ScreenData
	 */
	public ScreenData getScreenData() {
		return new ScreenData(this.screenData);
	}

}
