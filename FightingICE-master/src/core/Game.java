package core;

import enumerate.BackgroundType;
import enumerate.GameSceneName;
import gamescene.HomeMenu;
import gamescene.Launcher;
import gamescene.Socket;
import image.LetterImage;
import informationcontainer.AIContainer;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import loader.ResourceLoader;
import manager.GameManager;
import manager.GraphicManager;
import manager.InputManager;
import service.SocketServer;
import setting.FlagSetting;
import setting.GameSetting;
import setting.LaunchSetting;
import setting.ResourceSetting;
import util.DeleteFiles;

/**
 * Sets the game launch options and the initial game scene.
 */
public class Game extends GameManager {

    /**
     * Constructor that initializes the GameManager base class.
     */
    public Game() {
        super();
    }

    /**
     * Set the launch options based on the command-line arguments.
     *
     * @param options array containing all command-line arguments
     */
    public void setOptions(String[] options) {
        // Reads the configurations here
        for (int i = 0; i < options.length; ++i) {
            switch (options[i]) {
                case "-a":
                case "--all":
                    FlagSetting.allCombinationFlag = true;
                    LaunchSetting.deviceTypes = new char[]{1, 1};
                    break;
                case "-n":
                case "--number":
                    LaunchSetting.repeatNumber = Integer.parseInt(options[++i]);
                    FlagSetting.automationFlag = true;
                    break;
                case "--a1":
                    LaunchSetting.aiNames[0] = options[++i];
                    LaunchSetting.deviceTypes[0] = InputManager.DEVICE_TYPE_AI;
                    break;
                case "--a2":
                    LaunchSetting.aiNames[1] = options[++i];
                    LaunchSetting.deviceTypes[1] = InputManager.DEVICE_TYPE_AI;
                    break;
                case "--c1":
                    LaunchSetting.characterNames[0] = getCharacterName(options[++i]);
                    break;
                case "--c2":
                    LaunchSetting.characterNames[1] = getCharacterName(options[++i]);
                    break;
                case "-da":
                    FlagSetting.debugActionFlag = true;
                    break;
                case "-df":
                    FlagSetting.debugFrameDataFlag = true;
                    break;
                case "-t":
                    FlagSetting.trainingModeFlag = true;
                    break;
                case "-del":
                    DeleteFiles.getInstance().deleteFiles();
                    break;
                case "-r":
                    // -r 100 -> 1 game has 100 rounds
                    GameSetting.ROUND_MAX = Integer.parseInt(options[++i]);
                    break;
                case "-f":
                    // -f 360 -> 1 round has 6 second
                    GameSetting.ROUND_FRAME_NUMBER = Integer.parseInt(options[++i]);
                    break;
                case "--black-bg":
                    LaunchSetting.backgroundType = BackgroundType.BLACK;
                    break;
                case "--grey-bg":
                    LaunchSetting.backgroundType = BackgroundType.GREY;
                    break;
                case "--inverted-player":
                    LaunchSetting.invertedPlayer = Integer.parseInt(options[++i]);
                    break;
                case "--mute":
                    FlagSetting.muteFlag = true;
                    break;
                case "--lightweight-mode":
                    LaunchSetting.processingMode = LaunchSetting.LIGHTWEIGHT_MODE;
                    FlagSetting.automationFlag = true;
                    break;
                case "--headless-mode":
                    LaunchSetting.processingMode = LaunchSetting.HEADLESS_MODE;
                    FlagSetting.automationFlag = true;
                    break;
                case "--input-sync":
                    FlagSetting.inputSyncFlag = true;
                    break;
                case "--json":
                    FlagSetting.jsonFlag = true;
                    break;
                case "--limithp":
                    // --limithp P1_HP P2_HP
                    FlagSetting.limitHpFlag = true;
                    LaunchSetting.maxHp[0] = Integer.parseInt(options[++i]);
                    LaunchSetting.maxHp[1] = Integer.parseInt(options[++i]);
                    break;
                case "--slow":
                    FlagSetting.slowmotion = true;
                    break;
                case "--err-log":
                    FlagSetting.outputErrorAndLogFlag = true;
                    break;
                case "--blind-player":
	                int blindPlayer = Integer.parseInt(options[++i]);
	                if (blindPlayer == 2) {
	                	LaunchSetting.noVisual[0] = LaunchSetting.noVisual[1] = true;
	                } else {
	                    LaunchSetting.noVisual[blindPlayer] = true;
	                }
                    break;
                case "--sound":
                    LaunchSetting.soundName = options[++i];
                	break;
                case "--non-delay":
                	int player = Integer.parseInt(options[++i]);
                	if (player == 2) {
                		LaunchSetting.nonDelay[0] = LaunchSetting.nonDelay[1] = true;
                	} else {
                		LaunchSetting.nonDelay[player] = true;
                	}
                	break;
                case "--port":
                	int port = Integer.parseInt(options[++i]);
                    LaunchSetting.serverPort = port;
                    break;
                case "--pyftg-mode":
                	FlagSetting.enablePyftgMode = true;
                	break;
                case "--no-vision":
                	FlagSetting.visualVisibleOnRender = false;
                	break;
                case "--enable-builtin-sound":
                	FlagSetting.enableBuiltinSound = true;
                	FlagSetting.enableReplaySound = false;
                	FlagSetting.enableAudioPlayback = false;
                	break;
                case "--enable-replay-sound":
                	FlagSetting.enableBuiltinSound = false;
                	FlagSetting.enableReplaySound = true;
                	FlagSetting.enableAudioPlayback = false;
                	break;
                case "--enable-audio-playback":
                	FlagSetting.enableBuiltinSound = false;
                	FlagSetting.enableReplaySound = false;
                	FlagSetting.enableAudioPlayback = true;
                	break;
                case "--save-sound-on-replay":
                	FlagSetting.saveSoundOnReplay = true;
                	break;
                default:
                    Logger.getAnonymousLogger().log(Level.WARNING, "Arguments error: unknown format is exist. -> " + options[i] + " ?");
            }
        }

    }

    @Override
    public void initialize() {
        if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.HEADLESS_MODE)) {
        	// Initialize the font.
            Font awtFont = new Font("Times New Roman", Font.BOLD, 24);
            GraphicManager.getInstance().setLetterFont(new LetterImage(awtFont, true));
        }

        createLogDirectories();

        try {
			SocketServer.getInstance().startServer(LaunchSetting.serverPort);
	    	Logger.getAnonymousLogger().log(Level.INFO, "Socket server is started, listening on " + LaunchSetting.serverPort);
		} catch (IOException e) {
			e.printStackTrace();
            Logger.getAnonymousLogger().log(Level.INFO, "Fail to start gRPC server");
		}
        
        if (FlagSetting.enablePyftgMode) {
        	Socket grpc = new Socket();
        	this.startGame(grpc);
        } else if ((FlagSetting.automationFlag || FlagSetting.allCombinationFlag)) {
            // If -n or -a is specified, launch the game directly without showing the menu.
            if (FlagSetting.allCombinationFlag) {
                AIContainer.allAINameList = ResourceLoader.getInstance().loadFileNames("./data/ai", ".jar");

                if (AIContainer.allAINameList.size() < 2) {
                    Logger.getAnonymousLogger().log(Level.INFO, "Cannot launch FightingICE with Round-robin mode.");
                    this.isExitFlag = true;
                }
            }
            
            if (!LaunchSetting.soundName.equals("Default")) {
				ResourceSetting.SOUND_DIRECTORY = String.format("./data/sounds/%s/", LaunchSetting.soundName);
            }

            Launcher launcher = new Launcher(GameSceneName.PLAY);
            this.startGame(launcher);
        } else {
            // Otherwise, start the game from the menu.
            HomeMenu homeMenu = new HomeMenu();
            this.startGame(homeMenu);
        }

    }

    /**
     * Check whether the specified character name is available and return it if found.<br>
     * If it is unavailable, print a warning and return ZEN as the default character.
     *
     * @param characterName character name to search for
     * @return selected character name
     */
    private String getCharacterName(String characterName) {
        for (String character : GameSetting.CHARACTERS) {
            if (character.equals(characterName)) {
                return character;
            }
        }
        Logger.getAnonymousLogger().log(Level.WARNING, characterName + " is does not exist. Please check the set character name.");
        return "GARNET"; // Default character
    }

    /**
     * Creates log directories if they do not exist.
     */
    private void createLogDirectories() {
        new File("log").mkdir();
        new File("log/point").mkdir();
        new File("log/replay").mkdir();
        new File("log/sound").mkdir();
        new File("log/style").mkdir();
    }

    @Override
    public void close() {
        this.currentGameScene = null;
    }

}
