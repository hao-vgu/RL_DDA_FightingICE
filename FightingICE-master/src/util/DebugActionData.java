package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fighting.Character;
import loader.ResourceLoader;
import setting.LaunchSetting;

public class DebugActionData {

	/**
	 * List of maps from tracked action names to their total frame counts for P1 and P2.<br>
	 * Index 0: P1; Index 1: P2
	 */
	private ArrayList<HashMap<String, Integer>> actionList;

	/**
	 * List of maps from tracked action names to their counts during a round for P1 and P2.<br>
	 * Index 0: P1; Index 1: P2
	 */
	private ArrayList<HashMap<String, Integer>> countedActionContainer;

	/**
	 * BufferedReader used to read files.<br>
	 * Index 0: P1; Index 1: P2
	 */
	private BufferedReader[] bReaders;

	/**
	 * PrintWriter used to write files.<br>
	 * Index 0: P1; Index 1: P2
	 */
	private PrintWriter[] pWriters;

	/**
	 * Array containing all action names.
	 */
	private final String[] motionName = { "FORWARD_WALK", "DASH", "BACK_STEP", "JUMP", "FOR_JUMP", "BACK_JUMP",
			"STAND_GUARD", "CROUCH_GUARD", "AIR_GUARD", "THROW_A", "THROW_B", "STAND_A", "STAND_B", "CROUCH_A",
			"CROUCH_B", "AIR_A", "AIR_B", "AIR_DA", "AIR_DB", "STAND_FA", "STAND_FB", "CROUCH_FA", "CROUCH_FB",
			"AIR_FA", "AIR_FB", "AIR_UA", "AIR_UB", "STAND_D_DF_FA", "STAND_D_DF_FB", "STAND_F_D_DFA", "STAND_F_D_DFB",
			"STAND_D_DB_BA", "STAND_D_DB_BB", "AIR_D_DF_FA", "AIR_D_DF_FB", "AIR_F_D_DFA", "AIR_F_D_DFB", "AIR_D_DB_BA",
			"AIR_D_DB_BB", "STAND_D_DF_FC" };

	/**
	 * Get the singleton DebugActionData instance.
	 *
	 * @return the singleton DebugActionData instance
	 */
	public static DebugActionData getInstance() {
		return DebugActionDataHolder.instance;
	}

	/**
	 * Holder class that creates the instance the first time getInstance() is called.
	 */
	private static class DebugActionDataHolder {
		private static final DebugActionData instance = new DebugActionData();
	}

	/**
	 * Class constructor.
	 */
	private DebugActionData() {
		Logger.getAnonymousLogger().log(Level.INFO, "Create instance: " + DebugActionData.class.getName());
		Logger.getAnonymousLogger().log(Level.INFO, "start debug action mode...");
	}

	/**
	 * Initialize DebugActionData.<br>
	 * 1. Initialize the fields.<br>
	 * 2. Open the output file and write the action names as header information.<br>
	 * 3. Add the tracked action names and their total frame counts to a list.<br>
	 */
	public void initialize() {
		this.actionList = new ArrayList<HashMap<String, Integer>>(2);
		this.countedActionContainer = new ArrayList<HashMap<String, Integer>>(2);
		this.pWriters = new PrintWriter[2];
		this.bReaders = new BufferedReader[2];

		for (int i = 0; i < 2; i++) {
			this.actionList.add(new HashMap<String, Integer>());
			this.countedActionContainer.add(new HashMap<String, Integer>());
		}

		String path = "./debugActionData";
		new File(path).mkdir();

		// Open the input and output files.
		for (int i = 0; i < 2; i++) {
			String fileName = "/" + (i == 0 ? "P1" : "P2") + "ActionFile.csv";
			this.pWriters[i] = ResourceLoader.getInstance().openWriteFile(path + fileName, true);
			this.bReaders[i] = ResourceLoader.getInstance().openReadFile(path + fileName);

			writeHeader(i);
			readMotionData(i);
		}
	}

	/**
	 * Count how many times each action is performed by P1 and P2.<br>
	 * Count an action only when it starts.<br>
	 * Do not count untracked actions or actions already in progress.
	 *
	 * @param characters
	 *            Array containing the P1 and P2 character data.<br>
	 *            Index 0: P1; Index 1: P2
	 */
	public void countPlayerAction(Character[] characters) {
		String[] actionNames = new String[] { characters[0].getAction().name(), characters[1].getAction().name() };
		int[] remainingFrames = new int[] { characters[0].getRemainingFrame(), characters[1].getRemainingFrame() };

		for (int i = 0; i < 2; i++) {
			if (canCount(this.countedActionContainer.get(i), actionNames[i], remainingFrames[i])) {
				this.countedActionContainer.get(i).replace(actionNames[i],
						this.countedActionContainer.get(i).get(actionNames[i]) + 1);
			}
		}
	}

	/**
	 * Write the action counts for P1 and P2 to a CSV file.
	 */
	public void outputActionCount() {
		for (int i = 0; i < 2; i++) {
			for (String string : this.motionName) {
				this.pWriters[i].print(this.countedActionContainer.get(i).get(string) + ",");
				this.countedActionContainer.get(i).replace(string, 0);
			}

			this.pWriters[i].println();
			this.pWriters[i].flush();
		}
	}

	/**
	 * Close the output file and clear the list.
	 */
	public void closeAllWriters() {
		for (int i = 0; i < 2; i++) {
			this.pWriters[i].close();
		}
		this.actionList.clear();
		this.countedActionContainer.clear();
	}

	/**
	 * Write the action names to the output file as header information.
	 *
	 * @param i
	 *            Player number.<br>
	 *            0: P1; 1: P2
	 */
	private void writeHeader(int i) {
		try {
			if (this.bReaders[i].read() == -1) {
				for (String string : this.motionName) {
					this.pWriters[i].print(string + ",");
				}
				this.pWriters[i].println();
			} else {
				this.pWriters[i].println();
				this.pWriters[i].println();
			}

			this.bReaders[i].close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read the tracked action names and their total frame counts, then add them to the list.
	 *
	 * @param i
	 *            Player number.<br>
	 *            0: P1; 1: P2
	 */
	private void readMotionData(int i) {
		String fileName = "./data/characters/" + LaunchSetting.characterNames[i] + "/Motion.csv";
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String s;
			while ((s = br.readLine()) != null) {
				String array[] = s.split(","); // Split on commas.

				for (String string : this.motionName) {
					if (string.equals(array[0])) {
						this.actionList.get(i).put(string, Integer.parseInt(array[1]));
						this.countedActionContainer.get(i).put(string, 0);
					}
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check whether the action should be counted.
	 *
	 * @param temp
	 *            Map of tracked action names and their total frame counts.
	 * @param actionName
	 *            Name of the character's current action.
	 * @param remainingFrame
	 *            Frames remaining in the character's current action.
	 *
	 * @return true to count the action; false otherwise
	 */
	private boolean canCount(HashMap<String, Integer> temp, String actionName, int remainingFrame) {
		if (temp.containsKey(actionName)) {
			return temp.get(actionName) == remainingFrame - 1;
		} else {
			return false;
		}
	}
}
