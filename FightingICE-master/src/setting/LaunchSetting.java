package setting;

import enumerate.BackgroundType;

/**
 * Stores settings required for a match, such as maximum character HP and the number of repetitions.
 */
public final class LaunchSetting {

	public final static int LIGHTWEIGHT_MODE = 0;
	public final static int HEADLESS_MODE = 1;
	public final static int STANDARD_MODE = 2;
	
	/**
	 * Array containing the maximum HP for P1 and P2.
	 */
	public static int[] maxHp = { 400, 400 };

	/**
	 * Array containing the maximum energy for P1 and P2.
	 */
	public static int[] maxEnergy = { 300, 300 };

	/**
	 * Array containing the AI names for P1 and P2.<br>
	 * Contains "Keyboard" when the keyboard is used.
	 */
	public static String[] aiNames = { "KeyBoard", "KeyBoard" };

	/**
	 * Character names for P1 and P2.
	 */
	public static String[] characterNames = { "GARNET", "GARNET" };
	
	public static String soundName = "Default";

	/**
	 * Input device types to use.<br>
	 * {@code 0} if the device type is keyboard, or {@code 1} if AI.
	 */
	public static char[] deviceTypes = { 0, 0 };

	/**
	 * Number of times to repeat the match.
	 */
	public static int repeatNumber = 1;

	/**
	 * Player number whose image should be flipped.
	 */
	public static int invertedPlayer = 0;

	/**
	 * Background type.
	 */
	public static BackgroundType backgroundType = BackgroundType.IMAGE;

	/**
	 * Replay data name.
	 */
	public static String replayName = "None";

	/**
	 * Counter for the number of match repetitions.
	 */
	public static int repeatedCount = 0;
	
	public static int processingMode = STANDARD_MODE;

	/**
	 * AI's visual data is disabled or not
	 */
	public static boolean[] noVisual = {false, false};
	
	public static boolean[] nonDelay = {false, false};
	
	public static int serverPort = 31415;
	
	public static boolean isExpectedProcessingMode(int expectedProcessingMode) {
		return processingMode >= expectedProcessingMode;
	}
	
}
