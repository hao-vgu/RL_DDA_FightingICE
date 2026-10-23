package setting;

/**
 * Stores flags for game features.
 */
public class FlagSetting {

    /**
     * Flag indicating whether to repeat matches.
     */
    public static boolean automationFlag = false;

    /**
     * Flag indicating whether to run matches for every AI combination.
     */
    public static boolean allCombinationFlag = false;

    /**
     * Flag indicating whether to use a background image.
     */
    public static boolean enableBackground = true;

    /**
     * Flag indicating whether to log the number of times each action is performed.
     */
    public static boolean debugActionFlag = false;

    /**
     * Unused.
     */
    public static boolean debugFrameDataFlag = false;

    /**
     * Flag indicating whether to use Training mode.
     */
    public static boolean trainingModeFlag = false;

    /**
     * Flag indicating whether to set maximum HP for P1 and P2.
     */
    public static boolean limitHpFlag = false;

    /**
     * Flag indicating whether to mute audio.
     */
    public static boolean muteFlag = false;

    /**
     * Flag indicating whether to write match data to a JSON file.
     */
    public static boolean jsonFlag = false;

    /**
     * Flag indicating whether to write error logs.
     */
    public static boolean outputErrorAndLogFlag = false;

    /**
     * Flag indicating whether to start in FastMode.
     */
    public static boolean inputSyncFlag = false;

    /**
     * Flag indicating whether to use slow motion at the end of a round.
     */
    public static boolean slowmotion = false;
    
    public static boolean enablePyftgMode = false;
    public static boolean isPyftgReady = false;
    
    public static boolean visualVisibleOnRender = true;
    public static boolean enableBuiltinSound = false;
    public static boolean enableReplaySound = false;
    public static boolean enableAudioPlayback = false;
    public static boolean saveSoundOnReplay = false;
    
}
