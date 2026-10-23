package aiinterface;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import informationcontainer.AIContainer;
import informationcontainer.RoundResult;
import loader.ResourceLoader;
import manager.InputManager;
import service.SocketGenerativeSound;
import service.SocketServer;
import service.SocketStream;
import setting.FlagSetting;
import setting.LaunchSetting;
import struct.AudioData;
import struct.FrameData;
import struct.GameData;
import struct.ScreenData;

/**
 * Handles thread-related operations such as AI execution timing.
 */
public class ThreadController {

	/**
	 * The singleton ThreadController instance.
	 */
	private static ThreadController threadController = new ThreadController();
	
	private AIController[] ais;
	private SoundController sound;
	private List<StreamController> streams;
	private List<ControllerInterface> controllerList;
	
	private List<Object> waitObjs;

	private boolean[] processedAIs;
	private boolean processedSound;

	/**
	 * Synchronization object used to start each AI at the same time.
	 */
	private Object endFrame;

	/**
	 * Constructor that initializes the fields.
	 */
	private ThreadController() {
		this.ais = new AIController[2];
		this.streams = new ArrayList<>();
		
		this.waitObjs = new ArrayList<>();
		this.controllerList = new ArrayList<>();
		
		this.processedAIs = new boolean[2];
		resetProcessedFlag();
	}

	/**
	 * Get the singleton ThreadController instance.
	 *
	 * @return the singleton ThreadController instance
	 */
	public static ThreadController getInstance() {
		return threadController;
	}
	
	public AIController getAIController(boolean playerNumber) {
		return this.ais[playerNumber ? 0 : 1];
	}
	
	public SoundController getSoundController() {
		return this.sound;
	}
	
	public void createAIController() {
		// AI Controller
		String[] aiNames = LaunchSetting.aiNames.clone();

		if (FlagSetting.allCombinationFlag) {
			if (AIContainer.p1Index == AIContainer.p2Index) {
				AIContainer.p1Index++;
			}
			aiNames[0] = AIContainer.allAINameList.get(AIContainer.p1Index);
			aiNames[1] = AIContainer.allAINameList.get(AIContainer.p2Index);
		}

		char[] deviceTypes = LaunchSetting.deviceTypes.clone();
		for (int i = 0; i < deviceTypes.length; i++) {
			switch (deviceTypes[i]) {
			case InputManager.DEVICE_TYPE_AI:
				this.ais[i] = ResourceLoader.getInstance().loadAI(aiNames[i]);
				this.controllerList.add(this.ais[i]);
				break;
			case InputManager.DEVICE_TYPE_EXTERNAL:
				this.ais[i] = new AIController(SocketServer.getInstance().getPlayer(i));
				this.controllerList.add(this.ais[i]);
				break;
			}
		}
	}
	
	public void createSoundController() {
		SocketGenerativeSound generativeSound = SocketServer.getInstance().getGenerativeSound();
		if (!generativeSound.isCancelled()) {
			this.sound = new SoundController(generativeSound);
			this.controllerList.add(this.sound);
		}
	}
	
	public void createStreamControllers() {
		for (SocketStream socketStream : SocketServer.getInstance().getStreams()) {
			if (!socketStream.isCancelled()) {
				StreamController stream = new StreamController(socketStream);
				this.streams.add(stream);
				this.controllerList.add(stream);
			}
		}
	}
	
	public Object createWaitObject() {
		Object waitObj = new Object();
		this.waitObjs.add(waitObj);
		return waitObj;
	}
	
	/**
	 * Start the AI controllers.<br>
	 * Initialize the AI controllers using the supplied GameData instance, then start the AIs.
	 *
	 * @param gameData
	 *            GameData instance.
	 * @see GameData
	 */
	public void startAI(GameData gameData) {
		for (int i = 0; i < 2; i++) {
			if (this.ais[i] != null) {
				this.ais[i].initialize(createWaitObject(), gameData, i == 0);
				this.ais[i].start();// start the thread
		        Logger.getAnonymousLogger().log(Level.INFO, String.format("Start P%s AI controller thread", i == 0 ? "1" : "2"));
			}
		}
	}
	
	public void startSound(GameData gameData) {
        if (this.sound != null) {
            this.sound.initialize(createWaitObject(), gameData);
            this.sound.start();
        	Logger.getAnonymousLogger().log(Level.INFO, "Start Sound controller thread");
        }
	}
	
	public void startStreams(GameData gameData) {
		for (int i = 0; i < this.streams.size(); i++) {
			StreamController stream = this.streams.get(i);
			stream.initialize(createWaitObject(), gameData);
            stream.start();
        	Logger.getAnonymousLogger().log(Level.INFO, String.format("Start Stream controller thread #%d", i + 1));
		}
	}
	
	public void startAllThreads(GameData gameData) {
		startAI(gameData);
		startSound(gameData);
		startStreams(gameData);
		
		while (!checkThreadState(Thread.State.WAITING)) {
			try {
    			Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
		}
	}
	
	public void resetAllObjects() {
		for (Object waitObj : this.waitObjs) {
			synchronized (waitObj) {
				waitObj.notifyAll();
			}
		}
		
		if (FlagSetting.inputSyncFlag) {
			while (!(isAIProcessed() && isSoundProcessed() && checkThreadState(Thread.State.WAITING))) {
				try {
	    			Thread.sleep(1);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
			}
			
			resetProcessedFlag();
		}
	}
	
	public void setFrameData(FrameData frameData, ScreenData screenData, AudioData audioData) {
		for (ControllerInterface controller : this.controllerList) {
			controller.setFrameData(frameData, screenData, audioData);
		}
		
		resetAllObjects();
	}
	
	public void sendRoundResult(RoundResult roundResult) {
		for (ControllerInterface controller : this.controllerList) {
			controller.informRoundResult(roundResult);
		}
		
		resetAllObjects();
	}
	
	public void gameEnd() {
		for (ControllerInterface controller : this.controllerList) {
			controller.gameEnd();
		}
		
		resetProcessedFlag();
	}
	
	/**
	 * Clear the frame data stored in each AI controller.
	 */
	public void clear() {
		for (ControllerInterface controller : this.controllerList) {
			controller.clear();
		}
	}

	/**
	 * Return the object indicating that one frame of game processing has completed.
	 *
	 * @return object indicating that one frame of game processing has completed
	 */
	public Object getEndFrame() {
		return this.endFrame;
	}

	/**
	 * Set the flag indicating whether each AI has finished processing to {@code false}.<br>
	 * Used only in FastMode.
	 */
	private void resetProcessedFlag() {
		this.processedAIs[0] = false;
		this.processedAIs[1] = false;
		this.processedSound = false;
	}

	/**
	 * Mark the specified character's one-frame processing as complete.<br>
	 * After setting the flag, check whether both AIs have finished processing.<br>
	 * Used only in FastMode.
	 *
	 * @param playerNumber
	 *            The character's side flag.<br>
	 *            {@code true} if the character is P1, or {@code false} if P2.
	 */
	public void notifyEndAIProcess(boolean playerNumber) {
		this.processedAIs[playerNumber ? 0 : 1] = true;
	}
	
	public void notifyEndSoundProcess() {
		this.processedSound = true;
	}
	
	private boolean isAIProcessed() {
		boolean ans = true;
		for (int i = 0; i < 2; i++) {
			ans = ans && (LaunchSetting.deviceTypes[i] == InputManager.DEVICE_TYPE_KEYBOARD || this.processedAIs[i]);
		}
		return ans;
	}
	
	private boolean isSoundProcessed() {
		SocketGenerativeSound generativeSound = SocketServer.getInstance().getGenerativeSound();
		return generativeSound.isCancelled() || this.processedSound;
	}
	
	private boolean checkThreadState(Thread.State threadState) {
		boolean ans = true;
		for (int i = 0; i < 2; i++) {
			if (this.ais[i] != null) {
				ans = ans && this.ais[i].getState() == threadState;
			}
		}
		
		if (this.sound != null) {
			ans = ans && this.sound.getState() == threadState;
		}
		
		return ans;
	}
	
	public void close() {
		this.ais = new AIController[2];
		this.sound = null;
		this.streams.clear();
		this.waitObjs.clear();
		this.controllerList.clear();
	}

}
