package aiinterface;

import java.util.LinkedList;

import informationcontainer.RoundResult;
import setting.LaunchSetting;
import struct.AudioData;
import struct.FrameData;
import struct.GameData;
import struct.Key;
import struct.ScreenData;

/**
 * Manages AI threads and processing.
 */
public class AIController extends Thread implements ControllerInterface {

    /**
     * Interface defining the methods that an AI must implement.
     */
    private AIInterface ai;
    
    //private GameData gameData;

    /**
     * The character's side flag.<br>
     * {@code true} if the character is P1, or {@code false} if P2.
     */
    private boolean playerNumber;

    /**
     * Flag indicating whether the match has started.
     */
    private boolean isFighting;

    /**
     * Input keys.
     */
    private Key key;

    /**
     * Frame delay.
     */
    private final static int DELAY = 15;

    /**
     * List containing frame data.
     */
    private LinkedList<FrameData> framesData;

    /**
     * Data containing screen information.
     */
    private ScreenData screenData;

    private AudioData audioData;

    /**
     * Synchronization object used to start each AI at the same time.
     */
    private Object waitObj;
    
    private boolean roundEndFlag;
    private RoundResult roundResult;
    
    //private List<Double> durations = new ArrayList<>();

    /**
     * Constructor that sets the specified AI interface and initializes AIController.
     *
     * @param ai interface defining the methods that an AI must implement
     * @see AIInterface
     */
    public AIController(AIInterface ai) {
        this.ai = ai;
    }
    /**
     * Set the supplied parameters and initialize.
     *
     * @param waitFrame synchronization object used to start each AI at the same time
     * @param gameData instance containing immutable game information such as stage width and maximum HP
     * @param playerNumber the character's side flag.<br>
     *                     {@code true} if the character is P1, or {@code false} if P2.
     * @see GameData
     */
    public void initialize(Object waitFrame, GameData gameData, boolean playerNumber) {
        this.waitObj = waitFrame;
        this.playerNumber = playerNumber;
        this.key = new Key();
        this.framesData = new LinkedList<FrameData>();
        this.clear();
        this.isFighting = true;
        this.roundEndFlag = false;
        
    	this.ai.initialize(gameData, playerNumber);
    }
    
    public Key input() {
    	return this.key;
    }
    
    @Override
    public void run() {
        while (isFighting) {
            synchronized (this.waitObj) {
                try {
                    this.waitObj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            if (!isFighting) break;
            
            if (this.roundEndFlag) {
            	this.ai.roundEnd(roundResult);
            	this.roundEndFlag = false;
            	this.roundResult = null;
            } else {
            	boolean isControl;

                try {
                    isControl = this.framesData.getLast().getCharacter(this.playerNumber).isControl();
                } catch (NullPointerException e) {
                    // while game is not started
                    isControl = false;
                }

                FrameData frameData = !this.framesData.isEmpty() ? new FrameData(this.framesData.removeFirst()) : new FrameData();
                
                // screen raw data isn't provided to sound-only AI
    	        if (!LaunchSetting.noVisual[this.playerNumber ? 0: 1]){
    	            this.ai.getScreenData(this.screenData);
    	        } else {
    	        	frameData.removeVisualData();
    	        }
            	this.ai.getInformation(frameData, isControl);
            	this.ai.getNonDelayFrameData(this.framesData.getLast());
    	        this.ai.getAudioData(this.audioData);
    	        this.ai.processing();
    	        this.setInput(this.ai.input());
            }
	        
	        ThreadController.getInstance().notifyEndAIProcess(this.playerNumber);
        }
    }

    /**
     * Return input from the AI.<br>
     * Return an empty key if there is no input.
     *
     * @return input from the AI
     * @see Key
     */
    public synchronized Key getInput() {
        if (this.key != null) {
            return this.key;
        } else {
            return new Key();
        }
    }

    /**
     * Set the input from the AI.
     *
     * @param key input from the AI
     */
    public synchronized void setInput(Key key) {
        this.key = new Key(key);
    }

    /**
     * Add frame data after match processing to the list.<br>
     * If the list size exceeds DELAY, remove the oldest frame data.
     *
     * @param fd frame data after match processing
     * @see FrameData
     */
    public synchronized void setFrameData(FrameData fd, ScreenData sd, AudioData ad) {
        if (!fd.getEmptyFlag()) {
            this.framesData.addLast(new FrameData(fd));
        } else {
            this.framesData.addLast(new FrameData());
        }

        while (this.framesData.size() > DELAY) {
            this.framesData.removeFirst();
        }

        this.screenData = new ScreenData(sd);
        this.audioData = new AudioData(ad);
    }

    /**
     * Clear the frame data stored in the list.<br>
     * Then add DELAY - 1 empty frames to the list.
     */
    public synchronized void clear() {
        if (this.framesData != null) {
            this.framesData.clear();

            while (this.framesData.size() < DELAY) {
                this.framesData.add(new FrameData());
            }
        }

        this.screenData = new ScreenData();
        this.audioData = new AudioData();
    }
    
    /**
     * Pass the result at the end of the current round to the AI.
     *
     * @param roundResult result at the end of the current round
     * @see RoundResult
     */
    public synchronized void informRoundResult(RoundResult roundResult) {
    	this.roundEndFlag = true;
    	this.roundResult = roundResult;
    }

    /**
     * Notify the AI that the match has ended and shut it down.
     */
    public synchronized void gameEnd() {
        this.isFighting = false;
        this.ai.close();
        synchronized (this.waitObj) {
            this.waitObj.notifyAll();
        }
    }
}
