package setting;

/**
 * Stores basic game settings, including screen dimensions, FPS, and available characters.
 */
public class GameSetting {

    /**
     * Game screen width.
     */
    public static final int STAGE_WIDTH = 960;

    /**
     * Game screen height.
     */
    public static final int STAGE_HEIGHT = 640;
    
    public static final String TITLE_NAME = "DareFightingICE";

    /**
     * Game frame rate.
     */
    public static final int FPS = 60;

    /**
     * Array of characters available in the game.
     */
    public static final String[] CHARACTERS = {"GARNET","ZEN", "LUD", "NEZ"};

    /**
     * Array of default match repetition counts.
     */
    public static final int[] REPEAT_NUMBERS = {1, 2, 3, 5, 10, 30, 50, 100};

    /**
     * Total number of frames in one round.
     */
//	public static final int ROUND_FRAME_NUMBER = 3600;
    public static int ROUND_FRAME_NUMBER = 3600;

    /**
     * Break duration between rounds.
     */
    public static final int BREAKTIME_FRAME_NUMBER = 70;

    /**
     * Parameter controlling the effect of gravity.
     */
    public static final int GRAVITY = 1;

    /**
     * Parameter controlling the effect of friction.
     */
    public static final int FRICTION = 1;

    /**
     * Maximum number of key inputs to retain.
     */
    public static final int INPUT_LIMIT = 30;

    /**
     * Maximum number of rounds.
     */
    // public static final int ROUND_MAX = 3;
    public static int ROUND_MAX = 3;

    /**
     * Frame threshold for determining whether a combo continues.<br>
     * A combo continues if the next attack hits within this many frames of the previous hit.
     */
    public static final int COMBO_LIMIT = 30;

    /**
     * Extra frames used for slow motion.
     */
    public static final int ROUND_EXTRAFRAME_NUMBER = 180;

    /**
     * Audio sampling rate
     */
    public static final int SOUND_SAMPLING_RATE = 48000;
    public static final int SOUND_RENDER_SIZE = GameSetting.SOUND_SAMPLING_RATE / GameSetting.FPS;
    public static final int SOUND_BUFFER_SIZE = (int) Math.pow(2, Math.ceil(Math.log(SOUND_RENDER_SIZE) / Math.log(2)));
    
}
