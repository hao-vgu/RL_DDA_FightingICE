package informationcontainer;

import java.util.ArrayList;

/**
 * Handles data for AIs stored in the AI folder.<br>
 * Used when the round-robin option (-a) is specified.
 */
public class AIContainer {

	/**
	 * List of all AIs in the AI folder.
	 */
	public static ArrayList<String> allAINameList = new ArrayList<String>();

	/**
	 * Index specifying P1's AI.
	 */
	public static int p1Index = 0;

	/**
	 * Index specifying P2's AI.
	 */
	public static int p2Index = 0;
}
