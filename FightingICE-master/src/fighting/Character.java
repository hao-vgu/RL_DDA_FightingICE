package fighting;

import core.Game;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import enumerate.Action;
import enumerate.State;
import image.Image;
import loader.ResourceLoader;
import manager.SoundManager;
import setting.FlagSetting;
import setting.GameSetting;
import setting.LaunchSetting;
import struct.AudioSource;
import struct.CharacterData;
import struct.HitArea;
import struct.Key;

/**
 * Updates character state as the game progresses.
 */
public class Character {

    /**
     * The character side's flag.<br>
     * {@code true} if the character is P1, or {@code false} if P2.
     */
    private boolean playerNumber;
    /**
     * These sources are used for different sound effects such as walking landing and different moves and projectiles.
     */
    private AudioSource sourceDefault;
    private AudioSource sourceLanding;
    private AudioSource sourceWalking;
    private AudioSource[] sourceProjectTiles = new AudioSource[3];
    private AudioSource sourceEnergyChange;
    private AudioSource sourceBorderAlert;
    private AudioSource sourceHeartBeat;

    /**
     * The character's HP.
     */
    private int hp;

    /**
     * The character's energy.
     */
    private int energy;

    /**
     * The character graphic's most top-left x-coordinate.
     */
    private int x;

    /**
     * The character graphic's most top-left y-coordinate.
     */
    private int y;

    /**
     * The character's horizontal speed.
     */
    private int speedX;

    /**
     * The character's vertical speed.
     */
    private int speedY;

    /**
     * The character's state: STAND / CROUCH / AIR / DOWN.
     *
     * @see State
     */
    private State state;

    /**
     * The character's action.
     *
     * @see Action
     */
    private Action action;

    /**
     * The character's facing direction.<br>
     * {@code true} if the character is facing right, {@code false} otherwise.
     */
    private boolean front;

    /**
     * The flag whether this character can run a new motion with the motion's
     * command.<br>
     * {@code true} if the character can run a new motion, {@code false}
     * otherwise.
     */
    private boolean control;

    /**
     * The attack data that the character is using.
     */
    private Attack attack;
    private Attack attack2;

    /**
     * The number of frames that the character needs to resume to its normal
     * status.
     */
    private int remainingFrame;

    /**
     * The flag whether the motion hits the opponent or not.<br>
     * {@code true} if the motion hits the opponent, {@code false} otherwise.
     */
    private boolean hitConfirm;

    /**
     * The character's graphic width.
     */
    private int graphicSizeX;

    /**
     * The character's graphic height.
     */
    private int graphicSizeY;

    /**
     * Horizontal offset used to adjust the x-coordinate when determining a character's facing direction.
     */
    private int graphicAdjustX;

    /**
     * Horizontal offset used to adjust the x-coordinate when determining a character's initial position.
     */
    private int graphicAdjustInitialX[];

    /**
     * Last frame on which an attack hit the opponent.
     */
    private int lastHitFrame;

    /**
     * The list storing keys of the action that the character will be executing
     * in the simulator.
     */
    private Deque<Key> inputCommands;

    /**
     * The list storing up to 30 keys that the character executed in the
     * simulator.
     */
    private Deque<Key> processedCommands;

    /**
     * List of all character motions.
     *
     * @see Motion
     */
    private ArrayList<Motion> motionList;

    /**
     * Number of consecutive attack hits.
     */
    private int hitCount;

    /**
     * Flag indicating whether processing is running in the simulator.<br>
     * {@code true} if the process is executed in the simulator, {@code false}
     * otherwise.
     */
    private boolean isSimulateProcess;
    /**
     * For when the projectile is on screen and active.
     */
    private volatile boolean[] isProjectileLive = new boolean[3] ;
    /**
     * For when the other player gets hit by the projectile.
     */
    private volatile boolean[] ProjectileHit = new boolean[3];

    /**
     * To keep the crouch sound from playing over and over again while crouching.
     */
    private String TempName = " ";
    /**
     * To stop the footsteps sound effect from playing while in air.
     */
    private String TempName2 = " ";
    /**
     * For playing the Energy alert.
     */
    private int preEnergy = 0;
    /**
     * For Projectile attack sound movement and hit detections.size is 3 because 1 player can only have 3 projectiles active at a time.
     */
    private int[] sY = new int[3];
    private int[] sX = new int[3];
    private Attack[] projectileAttack =  new Attack[3];

    // same as projecttileAttack (for attack hit detection)
    private Attack[] projectileAttack2 =  new Attack[3];
    
    private HitArea preprocessedHitArea = new HitArea();

    /**
     * Class constructorï¼Ž
     */
    public Character() {
        initializeList();

        this.playerNumber = true;
        this.hp = 0;
        this.energy = 0;
        this.x = 0;
        this.y = 0;
        this.graphicSizeX = 0;
        this.graphicSizeY = 0;
        this.graphicAdjustX = 0;
        this.speedX = 0;
        this.speedY = 0;
        this.state = State.STAND;
        this.action = Action.NEUTRAL;
        this.hitConfirm = false;
        this.front = true;
        this.control = false;
        this.attack = null;
        this.remainingFrame = 0;
        this.lastHitFrame = 0;
        this.hitCount = 0;
        this.isSimulateProcess = false;
        this.initializeSound();

    }

    /**
     * Constructor that creates a new instance using the data from the supplied instance.
     *
     * @param character Character instance containing character data
     */
    public Character(Character character) {
        initializeList();

        this.playerNumber = character.isPlayerNumber();
        this.hp = character.getHp();
        this.energy = character.getEnergy();
        this.x = character.getX();
        this.y = character.getY();
        this.graphicSizeX = character.getGraphicSizeX();
        this.graphicSizeY = character.getGraphicSizeY();
        this.graphicAdjustX = character.getGraphicAdjustX();
        this.graphicAdjustInitialX = character.getGraphicAdjustInitialX();
        this.speedX = character.getSpeedX();
        this.speedY = character.getSpeedY();
        this.state = character.getState();
        this.action = character.getAction();
        this.hitConfirm = character.isHitConfirm();
        this.front = character.isFront();
        this.control = character.isControl();
        this.attack = character.getAttack();
        this.remainingFrame = character.getRemainingFrame();
        this.inputCommands = character.getInputCommand();
        this.processedCommands = character.getProcessedCommand();
        this.motionList = character.getMotionList();
        this.lastHitFrame = character.getLastHitFrame();
        this.hitCount = character.getHitCount();
        this.isSimulateProcess = character.isSimulateProcess();
        initializeSound();

    }

    /**
     * Constructor that creates a Character instance from the supplied data.<br>
     * This constructor is called only by the simulator.
     *
     * @param characterData CharacterData instance containing character data
     * @param motionList list containing all character motions
     * @see CharacterData
     * @see Motion
     */
    public Character(CharacterData characterData, ArrayList<Motion> motionList) {
        initializeList();

        this.playerNumber = characterData.isPlayerNumber();
        this.hp = characterData.getHp();
        this.energy = characterData.getEnergy();
        this.x = characterData.getX();
        this.y = characterData.getY();
        this.graphicSizeX = characterData.getGraphicSizeX();
        this.graphicSizeY = characterData.getGraphicSizeY();
        this.graphicAdjustX = characterData.getGraphicAdjustX();
        this.speedX = characterData.getSpeedX();
        this.speedY = characterData.getSpeedY();
        this.state = characterData.getState();
        this.action = characterData.getAction();
        this.hitConfirm = characterData.isHitConfirm();
        this.front = characterData.isFront();
        this.control = characterData.isControl();
        this.attack = new Attack(characterData.getAttack());
        this.remainingFrame = characterData.getRemainingFrame();
        this.inputCommands = characterData.getInputCommand();
        this.processedCommands = characterData.getProcessedCommand();
        this.motionList = motionList;
        this.lastHitFrame = characterData.getLastHitFrame();
        this.hitCount = characterData.getHitCount();
        this.isSimulateProcess = true;
        initializeSound();
    }

    /**
     * Initialize the character image dimensions using the settings in gSetting.txt.
     *
     * @param characterName the character name
     * @param playerNumber  the character's side flag. {@code true} if the player is P1, or
     *                      {@code false} if P2.
     */
    public void initialize(String characterName, boolean playerNumber) {
        try {
            BufferedReader br = ResourceLoader.getInstance()
                    .openReadFile("./data/characters/" + characterName + "/gSetting.txt");
            String[] size = br.readLine().split(",", 0);
            String[] center = br.readLine().split(",", 0);

            this.graphicSizeX = Integer.valueOf(size[0]);
            this.graphicSizeY = Integer.valueOf(size[1]);
            this.graphicAdjustX = Integer.valueOf(center[0]);
            this.graphicAdjustInitialX[0] = Integer.valueOf(center[2]);
            this.graphicAdjustInitialX[1] = Integer.valueOf(center[3]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.playerNumber = playerNumber;

        setMotionList(characterName);
    }

    /**
     * Initialize the list data.
     */
    public void initializeList() {
        this.inputCommands = new LinkedList<Key>();
        this.processedCommands = new LinkedList<Key>();
        this.motionList = new ArrayList<Motion>();
        this.graphicAdjustInitialX = new int[2];
    }

    public void initializeSound() {
        if (!this.isSimulateProcess) {
            this.sourceDefault = SoundManager.getInstance().createAudioSource();
            this.sourceLanding = SoundManager.getInstance().createAudioSource();
            this.sourceWalking = SoundManager.getInstance().createAudioSource();
            this.sourceProjectTiles[0] = SoundManager.getInstance().createAudioSource();
            this.sourceProjectTiles[1] = SoundManager.getInstance().createAudioSource();
            this.sourceProjectTiles[2] = SoundManager.getInstance().createAudioSource();
            this.sourceEnergyChange = SoundManager.getInstance().createAudioSource();
            this.sourceBorderAlert = SoundManager.getInstance().createAudioSource();
            this.sourceHeartBeat = SoundManager.getInstance().createAudioSource();
        }
    }

    /**
     * Initialize character data at the start of each round.
     */
    public void roundInit() {
        if (FlagSetting.limitHpFlag) {
            this.hp = LaunchSetting.maxHp[this.playerNumber ? 0 : 1];
        } else {
            this.hp = 0;
        }

        if (FlagSetting.trainingModeFlag) {
            this.hp = LaunchSetting.maxHp[this.playerNumber ? 0 : 1];
            this.energy = LaunchSetting.maxEnergy[this.playerNumber ? 0 : 1];
        } else {
            this.energy = 0;
        }

        this.speedX = 0;
        this.speedY = 0;
        this.state = State.STAND;
        this.action = Action.NEUTRAL;
        this.attack = null;
        this.remainingFrame = 1;
        this.control = false;
        this.hitConfirm = false;
        this.hitCount = 0;
        this.lastHitFrame = 0;

        if (this.playerNumber) {
            this.front = true;
            // Initial position.
            this.x = 100 + this.graphicAdjustInitialX[0];
            this.y = 335;
        } else {
            this.front = false;
            // Initial position.
            this.x = 460 + this.graphicAdjustInitialX[1];
            this.y = 335;
        }
        
        for (int i = 0; i < 3; i++) {
        	this.projectileAttack[i] = null;
        	this.isProjectileLive[i] = false;
        	this.ProjectileHit[i] = false;
        }
    }

    /**
     * Set the supplied action as the action currently being performed by this Character instance.
     *
     * @param executeAction the next action to perform
     * @param resetFlag whether to interrupt the action currently in progress
     */
    public void runAction(Action executeAction, boolean resetFlag) {
        Motion exeMotion = this.motionList.get(executeAction.ordinal());
        String Name;

        if (this.action != executeAction) {
            if (resetFlag) {

                for(int a = 0; a < this.projectileAttack.length; a++) {
                    if(this.projectileAttack[a] == null && this.sourceProjectTiles[a] != null) {
                        SoundManager.getInstance().stop(this.sourceProjectTiles[a]);
                    }
                }

                destroyAttackInstance();
            }

            this.remainingFrame = exeMotion.getFrameNumber();
            this.hitConfirm = false;
            this.energy += exeMotion.getAttackStartAddEnergy();
        }

        this.action = executeAction;
        this.state = exeMotion.getState();

        if (exeMotion.getSpeedX() != 0) {
            this.speedX = this.front ? exeMotion.getSpeedX() : -exeMotion.getSpeedX();
        }
        this.speedY += exeMotion.getSpeedY();
        this.control = exeMotion.isControl();
        Name = executeAction.toString();

        // Playing sound effects based on the actions.
        if (!this.isSimulateProcess) {
            if (Arrays.asList("JUMP", "FOR_JUMP", "BACK_JUMP", "THROW_A", "THROW_B", "THROW_HIT", "THROW_SUFFER", "STAND_A", "STAND_B", "CROUCH_A", "CROUCH_B", "AIR_A", "AIR_B", "AIR_DA", "AIR_DB", "STAND_FA", "STAND_FB", "CROUCH_FA", "CROUCH_FB", "AIR_FA", "AIR_FB", "AIR_UA", "AIR_UB", "STAND_F_D_DFA", "STAND_F_D_DFB", "STAND_D_DB_BA", "STAND_D_DB_BB", "AIR_F_D_DFA", "AIR_F_D_DFB", "AIR_D_DB_BA", "AIR_D_DB_BB").contains(Name)) {
                Name = Name + ".wav";

                SoundManager.getInstance().play2(sourceDefault, SoundManager.getInstance().getSoundBuffer(Name), this.x, this.y, false);
            } else if (Arrays.asList("CROUCH").contains(Name)) {
                Name = Name + ".wav";
                if (!TempName.equals(Name)) {
                    SoundManager.getInstance().play2(sourceDefault, SoundManager.getInstance().getSoundBuffer(Name), this.x, this.y, false);
                    TempName = Name;
                }
            } else if (Arrays.asList("FORWARD_WALK", "DASH", "BACK_STEP").contains(Name)) {
                Name = Name + ".wav";
                if (!TempName2.equals(Name)) {
                    SoundManager.getInstance().play2(sourceWalking, SoundManager.getInstance().getSoundBuffer(Name), this.x, this.y, true);
                    TempName2 = Name;
                }
            } else if (Arrays.asList("STAND_D_DF_FA", "STAND_D_DF_FB", "AIR_D_DF_FA", "AIR_D_DF_FB", "STAND_D_DF_FC").contains(Name)) {
                for(int a = 0 ; a<this.isProjectileLive.length ; a++) {
                    if(!this.isProjectileLive[a]) {
                        this.isProjectileLive[a] = true;
                        sY[a] = this.y;
                        sX[a] = this.x;
                        Name = Name + ".wav";
                        SoundManager.getInstance().play2(sourceProjectTiles[a], SoundManager.getInstance().getSoundBuffer(Name), this.x, this.y, true);
                        break;
                    }
                }
                // if (!this.isProjectileLive) {
                // this.isProjectileLive = true;

                // sX = this.x;
                // sY = this.y;
                // }
                // Name = Name + ".wav";
//				SoundManager.getInstance().play2(sourceid4,SoundManager.getInstance().getSoundEffect().get(Name),this.x,this.y,true);
                //SoundManager.getInstance().play2(sourceProjectTiles, SoundManager.getInstance().getSoundBuffers().get(Name), this.x, this.y, true);
            }
        }
    }

    public void resetEnergyCount(){
        this.preEnergy = 0;
    }

    /**
     * Updates character's information.
     */
    public void update() {
        moveX(this.speedX);
        moveY(this.speedY);
        frictionEffect();
        gravityEffect();
        
    	preprocessedHitArea = new HitArea(getHitAreaLeft(), getHitAreaRight(), getHitAreaTop(), getHitAreaBottom());

        if (FlagSetting.trainingModeFlag) {
            this.energy = LaunchSetting.maxEnergy[this.playerNumber ? 0 : 1];
            this.hp = LaunchSetting.maxHp[this.playerNumber ? 0 : 1];
        }

        if (this.energy > LaunchSetting.maxEnergy[this.playerNumber ? 0 : 1]) {
            this.energy = LaunchSetting.maxEnergy[this.playerNumber ? 0 : 1];
        }

        if (getHitAreaBottom() >= GameSetting.STAGE_HEIGHT) {
            // If in AIR, force LANDING or STAND immediately
            if (this.state == State.AIR) {
                if (motionList.get(Action.LANDING.ordinal()).isLandingFlag()) {
                    runAction(Action.LANDING, true);
                } else {
                    runAction(Action.STAND, true);
                }
                setSpeedY(0);
                if (!this.isSimulateProcess) {
                    SoundManager.getInstance().play2(sourceLanding, SoundManager.getInstance().getSoundBuffer("LANDING.wav"), this.x, this.y, false);
                }
            } else if (motionList.get(this.action.ordinal()).isLandingFlag()) {
                runAction(Action.LANDING, true);
                setSpeedY(0);
                if (!this.isSimulateProcess) {
                    SoundManager.getInstance().play2(sourceLanding, SoundManager.getInstance().getSoundBuffer("LANDING.wav"), this.x, this.y, false);
                }
            }
            moveY(GameSetting.STAGE_HEIGHT - this.getHitAreaBottom());
        }

        this.remainingFrame = getRemainingFrame() - 1;

        if (this.remainingFrame <= 0) {
            if (this.action == Action.CHANGE_DOWN) {
                runAction(Action.DOWN, true);
            } else if (this.action == Action.DOWN) {
                runAction(Action.RISE, true);
            } else if (this.state == State.AIR || getHitAreaBottom() < GameSetting.STAGE_HEIGHT) {
                runAction(Action.AIR, true);
            } else if (this.state == State.CROUCH) {
                runAction(Action.CROUCH, true);
            } else {
                runAction(Action.STAND, true);
            }
        }

        createAttackInstance();
        if (this.attack != null) {
            if (this.attack.isProjectile()) {
                for (int a = 0 ; a < this.projectileAttack.length ; a++) {
                    if (this.projectileAttack[a] == null) {
                        this.projectileAttack[a] = new Attack(this.attack, this.isProjectileLive[a]);
                        this.projectileAttack2[a] = this.attack;
                        break;
                    }
                }
            }
        }

        if (!this.inputCommands.isEmpty()) {
            this.processedCommands.addLast(new Key(this.inputCommands.pop()));
        } else {
            this.processedCommands.addLast(new Key());
        }

        if (this.processedCommands.size() > GameSetting.INPUT_LIMIT)
            this.processedCommands.removeFirst();

        // Different Conditions for the sound effects
        if (!this.isSimulateProcess) {
            if (this.energy > this.preEnergy + 50) {
                this.preEnergy = this.energy;
                if (this.playerNumber) {
                    SoundManager.getInstance().play2(sourceEnergyChange, SoundManager.getInstance().getSoundBuffer("EnergyCharge.wav"), 0, 0, false);
                } else {
                    SoundManager.getInstance().play2(sourceEnergyChange, SoundManager.getInstance().getSoundBuffer("EnergyCharge.wav"), GameSetting.STAGE_WIDTH, 0, false);
                }

            }
            
            if (this.getHitAreaLeft() < 0 || this.getHitAreaRight() > GameSetting.STAGE_WIDTH) {
                if (!SoundManager.getInstance().isPlaying(sourceBorderAlert)) {
                    if (this.getHitAreaLeft() < 0) {
                        SoundManager.getInstance().play2(sourceBorderAlert, SoundManager.getInstance().getSoundBuffer("BorderAlert.wav"), 0, 0, false);
                    } else {
                        SoundManager.getInstance().play2(sourceBorderAlert, SoundManager.getInstance().getSoundBuffer("BorderAlert.wav"), GameSetting.STAGE_WIDTH, 0, false);
                    }
                }
            }
            
            if (FlagSetting.limitHpFlag) {
            	if(this.hp < 50) {
            		if(!SoundManager.getInstance().isPlaying(sourceHeartBeat)) {
            			if (this.playerNumber) {
                            SoundManager.getInstance().play2(sourceHeartBeat, SoundManager.getInstance().getSoundBuffer("Heartbeat.wav"), 0, 0, false);
                        } else {
                            SoundManager.getInstance().play2(sourceHeartBeat, SoundManager.getInstance().getSoundBuffer("Heartbeat.wav"), GameSetting.STAGE_WIDTH, 0, false);
                        }
            		}
            	}  
            }

            // This is to make sure crouch sound does not loop while the character is crouching.
            if (this.state != State.CROUCH) {
                TempName = " ";
            }
            
            // This is to make sure Character footsteps sound does not play when character is in air.
            if (this.speedX == 0 || this.state == State.AIR) {
                TempName2 = " ";
                SoundManager.getInstance().stop(sourceWalking);
            } else {
                SoundManager.getInstance().setSourcePos(sourceWalking, this.x, this.y);
            }

            for (int a  = 0 ; a < this.projectileAttack.length ; a++) {
                if (this.projectileAttack[a] != null) {
                    if (this.isProjectileLive[a]) {
                        if (this.projectileAttack[a].updateProjectileAttack() && !this.ProjectileHit[a]) {
                            this.sX[a] = this.sX[a] + (this.projectileAttack[a].getSpeedX());
                            this.sY[a] = this.sX[a] + (this.projectileAttack[a].getSpeedY());
                            SoundManager.getInstance().setSourcePos(sourceProjectTiles[a], this.sX[a], this.sY[a]);
                        } else {
                            SoundManager.getInstance().stop(sourceProjectTiles[a]);
                            this.projectileAttack[a] = null;
                            this.isProjectileLive[a] = false;
                            this.ProjectileHit[a] = false;
                        }
                    }
                }
            }
        }
    }

    /**
     * Process an attack hit.
     *
     * @param opponent the opponent character instance
     * @param attack this character's Attack instance
     * @param currentFrame current frame number in the round
     */
    public void hitAttack(Character opponent, Attack attack, int currentFrame) {
        if (attack.isProjectile()) {
            for (int a = 0; a < this.projectileAttack.length; a++) {
                if (opponent.projectileAttack[a] != null) {
                    if (attack == opponent.projectileAttack2[a]) {
                        opponent.ProjectileHit[a] = true;
                        break;
                    }
                }
            }
        }

        int direction = opponent.getHitAreaCenterX() <= getHitAreaCenterX() ? 1 : -1;
        opponent.setHitCount(opponent.getHitCount() + 1);
        opponent.setLastHitFrame(currentFrame);

        if (isGuard(attack)) {
            setHp(this.hp - attack.getGuardDamage() - opponent.getExtraDamage());
            setEnergy(this.energy + attack.getGiveEnergy());
            setSpeedX(direction * attack.getImpactX() / 2); // Half the normal knockback.
            setRemainingFrame(attack.getGiveGuardRecov());
            opponent.setEnergy(opponent.getEnergy() + attack.getGuardAddEnergy());

            if (!this.isSimulateProcess) {
                SoundManager.getInstance().play2(sourceLanding, SoundManager.getInstance().getSoundBuffer("WeakGuard.wav"), this.x, this.y, false);
            }
        } else {
            // Handle a throw.
            if (attack.getAttackType() == 4) {
                if (this.state != State.AIR && this.state != State.DOWN) {
                    runAction(Action.THROW_SUFFER, false);

                    if (opponent.getAction() != Action.THROW_SUFFER) {
                        opponent.runAction(Action.THROW_HIT, false);
                    }

                    setHp(this.hp - attack.getHitDamage() - opponent.getExtraDamage());
                    setEnergy(this.energy + attack.getGiveEnergy());
                    opponent.setEnergy(opponent.getEnergy() + attack.getHitAddEnergy());
                }

                // Handle a non-throw attack.
            } else {
                setHp(this.hp - attack.getHitDamage() - opponent.getExtraDamage());
                setEnergy(this.energy + attack.getGiveEnergy());
                if (this.x <= -180 || this.x >= 720 && attack.isProjectile() == false) {
                    setSpeedX(0);
                    opponent.setSpeedX(-direction * attack.getImpactX());
                } else {
                setSpeedX(direction * attack.getImpactX());
                }
                setSpeedY(attack.getImpactY());
                opponent.setEnergy(opponent.getEnergy() + attack.getHitAddEnergy());

                // Handle a knockdown attack.
                if (attack.isDownProp()) {
                    runAction(Action.CHANGE_DOWN, false);
                    setRemainingFrame(this.motionList.get(this.action.ordinal()).getFrameNumber());

                    if (!this.isSimulateProcess) {
                        SoundManager.getInstance().play2(sourceLanding, SoundManager.getInstance().getSoundBuffer("HitB.wav"), this.x, this.y, false);
                    }
                } else {
                    switch (this.state) {
                        case STAND:
                            runAction(Action.STAND_RECOV, false);
                            break;

                        case CROUCH:
                            runAction(Action.CROUCH_RECOV, false);
                            break;

                        case AIR:
                            runAction(Action.AIR_RECOV, false);
                            break;

                        default:
                            break;
                    }

                    if (!this.isSimulateProcess) {
                        SoundManager.getInstance().play2(sourceLanding, SoundManager.getInstance().getSoundBuffer("HitA.wav"), this.x, this.y, false);
                    }
                }
            }
        }
    }

    /**
     * Check whether the character successfully guarded when hit by an opponent's attack.<br>
     * If the guard succeeded, transition to the recovery state corresponding to the type of guard.
     *
     * @param attack the opponent's Attack instance
     * @return {@code true} if the guard is successful, {@code false} otherwise
     */
    private boolean isGuard(Attack attack) {
        boolean isGuard = false;

        switch (this.action) {
            case STAND_GUARD:
                if (attack.getAttackType() == 1 || attack.getAttackType() == 2) {
                    runAction(Action.STAND_GUARD_RECOV, false);
                    isGuard = true;
                }
                break;

            case CROUCH_GUARD:
                if (attack.getAttackType() == 1 || attack.getAttackType() == 3) {
                    runAction(Action.CROUCH_GUARD_RECOV, false);
                    isGuard = true;
                }
                break;

            case AIR_GUARD:
                if (attack.getAttackType() == 1 || attack.getAttackType() == 2) {
                    runAction(Action.STAND_GUARD_RECOV, false);
                    isGuard = true;
                }
                break;

            case STAND_GUARD_RECOV:
                runAction(Action.STAND_GUARD_RECOV, false);
                isGuard = true;
                break;

            case CROUCH_GUARD_RECOV:
                runAction(Action.CROUCH_GUARD_RECOV, false);
                isGuard = true;
                break;

            case AIR_GUARD_RECOV:
                runAction(Action.AIR_GUARD_RECOV, false);
                isGuard = true;
                break;

            default:
                isGuard = false;
                break;
        }

        return isGuard;
    }

    /**
     * Create an Attack instance using the action data held by this Character instance.<br>
     * This method creates the attack hitbox.
     */
    private void createAttackInstance() {
        Motion motion = this.motionList.get(this.action.ordinal());

        if (startActive(motion)) {
            this.attack = new Attack(motion.getAttackHitArea(), motion.getAttackSpeedX(), motion.getAttackSpeedY(),
                    motion.getAttackStartUp(), motion.getAttackActive(), motion.getAttackHitDamage(),
                    motion.getAttackGuardDamage(), motion.getAttackStartAddEnergy(), motion.getAttackHitAddEnergy(),
                    motion.getAttackGuardAddEnergy(), motion.getAttackGiveEnergy(), motion.getAttackImpactX(),
                    motion.getAttackImpactY(), motion.getAttackGiveGuardRecov(), motion.getAttackType(),
                    motion.isAttackDownProp());

            this.attack.initialize(this.playerNumber, this.x, this.y, this.graphicSizeX, this.front);
            
            this.attack2 = attack;
        }
    }

    /**
     * Check whether the elapsed attack frames have reached the first active frame.
     *
     * @param motion Motion instance containing the attack's total frame count, damage, and other data
     * @return {@code true} if the elapsed attack frames equal the first active frame; {@code false} otherwise
     * otherwise
     * @see Motion
     */
    public boolean startActive(Motion motion) {
        int startActive = motion.getFrameNumber() - motion.getAttackStartUp();
        return startActive == this.remainingFrame;
    }

    /**
     * Moves the character on the x-axis.
     *
     * @param relativePosition the amount of the movement on the x-axis
     */
    public void moveX(int relativePosition) {
        setX(getX() + relativePosition);
    }

    /**
     * Moves the character on the y-axis.
     *
     * @param relativePosition the amount of the movement on the y-axis
     */
    public void moveY(int relativePosition) {
        setY(getY() + relativePosition);
    }

    /**
     * Apply friction when the character is on the ground.
     */
    public void frictionEffect() {
        if (getHitAreaBottom() >= GameSetting.STAGE_HEIGHT) {
            if (this.speedX > 0) {
                setSpeedX(this.speedX - GameSetting.FRICTION);
            } else if (this.speedX < 0) {
                setSpeedX(this.speedX + GameSetting.FRICTION);
            }
        }
    }

    /**
     * Apply gravity while the character is airborne.
     */
    public void gravityEffect() {
        if (getHitAreaBottom() >= GameSetting.STAGE_HEIGHT) {
            setSpeedY(0);
        } else if (getHitAreaTop() <= 0) {
            setSpeedY(GameSetting.GRAVITY);
        } else {
            setSpeedY(this.speedY + GameSetting.GRAVITY);
        }
    }

    /**
     * Defines character's orientation.
     *
     * @param opponentCenterX the opponent character's hit box's center x-coordinate
     */
    public void frontDecision(int opponentCenterX) {
        if (this.front) {
            if (getHitAreaCenterX() < opponentCenterX) {
                this.front = true;
            } else {
                this.x = this.x - this.graphicSizeX + this.graphicAdjustX * 2;
                this.front = false;
            }

        } else {
            if (getHitAreaCenterX() < opponentCenterX) {
                this.x = this.x + this.graphicSizeX - this.graphicAdjustX * 2;
                this.front = true;
            } else {
                this.front = false;
            }
        }
    }

    /**
     * Destroys the actual motion.
     */
    public void destroyAttackInstance() {
        this.attack = null;
    }

    /**
     * Returns the character side's flag.
     *
     * @return {@code true} if the character is P1, or {@code false} if P2
     */
    public boolean isPlayerNumber() {
        return this.playerNumber;
    }

    /**
     * Returns the character's facing direction.
     *
     * @return {@code true} if the character is facing right, {@code false}
     * otherwise
     */
    public boolean isFront() {
        return this.front;
    }

    /**
     * Returns the flag whether this character can run a new motion with the
     * motion's command.
     *
     * @return {@code true} if the character can run a new motion, {@code false}
     * otherwise
     */
    public boolean isControl() {
        return this.control;
    }

    /**
     * Returns the character's HP.
     *
     * @return the character's HP
     */
    public int getHp() {
        return this.hp;
    }

    /**
     * Returns the character's energy.
     *
     * @return the character's energy
     */
    public int getEnergy() {
        return this.energy;
    }

    /**
     * Returns the character graphic's most top-left x-coordinate.
     *
     * @return the character graphic's most top-left x-coordinate
     */
    public int getX() {
        return this.x;
    }

    /**
     * Returns the character graphic's most top-left y-coordinate.
     *
     * @return the character graphic's most top-left y-coordinate
     */
    public int getY() {
        return this.y;
    }

    /**
     * Returns the character's horizontal speed.
     *
     * @return the character's horizontal speed
     */
    public int getSpeedX() {
        return this.speedX;
    }

    /**
     * Returns the character's vertical speed.
     *
     * @return the character's vertical speed
     */
    public int getSpeedY() {
        return this.speedY;
    }

    /**
     * Returns the character's state: STAND / CROUCH / AIR / DOWN.
     *
     * @return the character's state: STAND / CROUCH / AIR / DOWN
     * @see State
     */
    public State getState() {
        return this.state;
    }

    /**
     * Returns the character's action.
     *
     * @return the character's action
     * @see Action
     */
    public Action getAction() {
        return this.action;
    }

    /**
     * Returns the character's hit box's most-right x-coordinate.
     *
     * @return the character's hit box's most-right x-coordinate
     */
    public int getHitAreaRight() {
        HitArea area = this.motionList.get(this.action.ordinal()).getCharacterHitArea();

        return this.front ? area.getRight() + x : this.graphicSizeX - area.getLeft() + x;
    }

    /**
     * Returns the character's hit box's most-left x-coordinate.
     *
     * @return the character's hit box's most-left x-coordinate
     */
    public int getHitAreaLeft() {
        HitArea area = this.motionList.get(this.action.ordinal()).getCharacterHitArea();

        return this.front ? area.getLeft() + x : this.graphicSizeX - area.getRight() + x;
    }

    /**
     * Returns the character's hit box's most-top y-coordinate.
     *
     * @return the character's hit box's most-top y-coordinate
     */
    public int getHitAreaTop() {
        return this.motionList.get(this.action.ordinal()).getCharacterHitArea().getTop() + y;
    }

    /**
     * Returns the character's hit box's most-bottom y-coordinate.
     *
     * @return the character's hit box's most-bottom y-coordinate
     */
    public int getHitAreaBottom() {
        return this.motionList.get(this.action.ordinal()).getCharacterHitArea().getBottom() + y;

    }

    /**
     * Returns the character's hit box's center x-coordinate.
     *
     * @return the character's hit box's center x-coordinate
     */
    public int getHitAreaCenterX() {
        return (getHitAreaRight() + getHitAreaLeft()) / 2;
    }

    /**
     * Returns the character's hit box's center y-coordinate.
     *
     * @return the character's hit box's center y-coordinate
     */
    public int getHitAreaCenterY() {
        return (getHitAreaTop() + getHitAreaBottom()) / 2;
    }

    /**
     * Reverses the horizontal speed.
     */
    public void reversalSpeedX() {
        this.speedX = -(this.speedX / 2);
    }

    /**
     * Returns the flag whether the motion hits the opponent or not
     *
     * @return {@code true} if the motion hits the opponent, {@code false}
     * otherwise
     */
    public boolean isHitConfirm() {
        return this.hitConfirm;
    }

    /**
     * Returns the number of frames that the character needs to resume to its
     * normal status.
     *
     * @return the number of frames that the character needs to resume to its
     * normal status
     */
    public int getRemainingFrame() {
        return this.remainingFrame;
    }

    /**
     * Returns the attack that the character is using.
     *
     * @return the attack that the character is using
     * @see Attack
     */
    public Attack getAttack() {
        return this.attack;
    }
    
    public Attack getAttack2() {
    	return this.attack2;
    }

    /**
     * Return the horizontal offset used to adjust the x-coordinate when determining the character's facing direction.
     *
     * @return the horizontal offset used to adjust the x-coordinate when determining the character's facing direction
     */
    public int getGraphicAdjustX() {
        return this.graphicAdjustX;
    }

    /**
     * Return the horizontal offset used to adjust the x-coordinate when determining the character's initial position.
     *
     * @return the horizontal offset used to adjust the x-coordinate when determining the character's initial position
     */
    public int[] getGraphicAdjustInitialX() {
        return this.graphicAdjustInitialX;
    }

    /**
     * Returns the character's graphic width.
     *
     * @return the character's graphic width
     */
    public int getGraphicSizeX() {
        return this.graphicSizeX;
    }

    /**
     * Returns the character's graphic height.
     *
     * @return the character's graphic height
     */
    public int getGraphicSizeY() {
        return this.graphicSizeY;
    }

    /**
     * Return the list of all character motions.
     *
     * @return the list of all character motions
     */
    public ArrayList<Motion> getMotionList() {
        ArrayList<Motion> temp = new ArrayList<Motion>();
        for (Motion motion : this.motionList) {
            temp.add(motion);
        }

        return temp;
    }

    /**
     * Returns a list storing keys of the action that the character will be
     * executing in the simulator
     *
     * @return a list storing keys of the action that the character will be
     * executing in the simulator
     */
    public Deque<Key> getInputCommand() {
        LinkedList<Key> temp = new LinkedList<Key>();
        for (Key key : this.inputCommands) {
            temp.add(key);
        }

        return temp;
    }

    /**
     * Returns a list storing up to 30 keys that the character executed in the
     * simulator
     *
     * @return a list storing up to 30 keys that the character executed in the
     * simulator
     */
    public Deque<Key> getProcessedCommand() {
        LinkedList<Key> temp = new LinkedList<Key>();
        for (Key key : this.processedCommands) {
            temp.add(key);
        }

        return temp;
    }

    /**
     * Returns the current image of the character according to the current
     * action and frame number.
     *
     * @return the current image of the character according to the current
     * action and frame number
     */
    public Image getNowImage() {
        Motion motion = motionList.get(this.action.ordinal());

        return motion.getImage(Math.abs(this.remainingFrame) % motion.getFrameNumber());
    }

    /**
     * Return the number of consecutive attack hits.
     *
     * @return the number of consecutive attack hits
     */
    public int getHitCount() {
        return this.hitCount;
    }

    /**
     * Return the last frame on which an attack hit the opponent.
     *
     * @return the last frame on which an attack hit the opponent
     */
    public int getLastHitFrame() {
        return this.lastHitFrame;
    }

    /**
     * Return bonus damage based on the number of consecutive attack hits.<br>
     *
     * @return bonus damage based on the number of consecutive attack hits
     */
    public int getExtraDamage() {
        int requireHit = 4; // Minimum number of hits required for bonus damage.
        int damage = 5; // Bonus damage.

        return this.hitCount < requireHit ? 0 : damage * requireHit / this.hitCount;
    }
    
    public Attack getProjectileAttack(int index) {
    	return this.projectileAttack[index];
    }
    
    public boolean getProjectileLive(int index) {
    	return this.isProjectileLive[index];
    }
    
    public boolean getProjectileHit(int index) {
    	return this.ProjectileHit[index];
    }
    
    public HitArea getPreprocessedHitArea() {
    	return this.preprocessedHitArea;
    }
    
    /**
     * Sets the character's HP.
     *
     * @param hp the amount of HP
     */
    public void setHp(int hp) {
        this.hp = hp;
    }

    /**
     * Sets the character's energy.
     *
     * @param energy the amount of energy
     */
    public void setEnergy(int energy) {
        this.energy = energy;
    }

    /**
     * Sets the character's horizontal position.
     *
     * @param x the character's horizontal position
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the character's vertical position.
     *
     * @param y the character's vertical position
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Sets the character's horizontal speed.
     *
     * @param speedX the character's horizontal speed
     */
    public void setSpeedX(int speedX) {
        this.speedX = speedX;
    }

    /**
     * Sets the character's vertical speed.
     *
     * @param speedY the character's vertical speed
     */
    public void setSpeedY(int speedY) {
        this.speedY = speedY;
    }

    /**
     * Sets the character's state.
     *
     * @param state a given state
     * @see State
     */
    public void setState(State state) {
        this.state = state;
    }

    /**
     * Sets the character's action.
     *
     * @param action a given action
     * @see Action
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Sets the flag whether the motion hits the opponent or not.
     *
     * @param hitConfirm the flag whether the motion hits the opponent or not.
     *                   {@code true} if the motion hits the opponent, {@code false}
     *                   otherwise.
     */
    public void setHitConfirm(boolean hitConfirm) {
        this.hitConfirm = hitConfirm;
    }

    /**
     * Sets the character's facing direction.
     *
     * @param front the character's facing direction. {@code true} if the
     *              character is facing right, {@code false} otherwise.
     */
    public void setFront(boolean front) {
        this.front = front;
    }

    /**
     * Sets the flag whether this character can run a new motion with the
     * motion's command.
     *
     * @param control the flag whether this character can run a new motion with the
     *                motion's command. {@code true} if the character can run,
     *                {@code false} otherwise.
     */
    public void setControl(boolean control) {
        this.control = control;
    }

    /**
     * Sets the number of frames that the character needs to resume to its
     * normal status.
     *
     * @param remainingFrame the number of frames that the character needs to resume to its
     *                       normal status you want to set
     */
    public void setRemainingFrame(int remainingFrame) {
        this.remainingFrame = remainingFrame;
    }

    /**
     * Sets the character's attack.
     *
     * @param attack the attack you want to set
     * @see Attack
     */
    public void setAttack(Attack attack) {
        this.attack = attack;
    }

    /**
     * Sets the width of the character's graphic.
     *
     * @param graphicSizeX the width of the character's graphic
     */
    public void setGraphicSizeX(int graphicSizeX) {
        this.graphicSizeX = graphicSizeX;
    }

    /**
     * Sets the height of the character's graphic.
     *
     * @param graphicSizeY the height of the character's graphic
     */
    public void setGraphicSizeY(int graphicSizeY) {
        this.graphicSizeY = graphicSizeY;
    }

    /**
     * Sets all of possible motions of the given character.
     *
     * @param characterName the character's name
     */
    private void setMotionList(String characterName) {
        try {
            BufferedReader br = ResourceLoader.getInstance()
                    .openReadFile("./data/characters/" + characterName + "/Motion.csv");

            String line;
            br.readLine(); // ignore header

            while ((line = br.readLine()) != null) {
                String[] st = line.split(",", 0);
                Motion motion = new Motion(st, characterName, this.playerNumber ? 0 : 1);
                this.motionList.add(motion);
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the number of consecutive attack hits.
     *
     * @param hitCount number of consecutive attack hits
     */
    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    /**
     * Set the current frame as the last frame on which an attack hit the opponent.
     *
     * @param currentFrame the current frame
     */
    public void setLastHitFrame(int currentFrame) {
        this.lastHitFrame = currentFrame;
    }

    /**
     * Sets a list storing keys of the action that the character will be
     * executing in the simulator.
     *
     * @param inputCommands a list storing keys of the action that the character will be
     *                      executing in the simulator
     */
    public void setInputCommand(Deque<Key> inputCommands) {
        this.inputCommands = inputCommands;
    }

    /**
     * Sets a list storing up to 30 keys that the character executed in the
     * simulator.
     *
     * @param inputCommands a list storing up to 30 keys that the character executed in
     *                      the simulator
     */
    public void setProcessedCommand(Deque<Key> inputCommands) {
        this.processedCommands = inputCommands;
    }

    /**
     * Gets a boolean value whether the combo is still valid or not.
     *
     * @param nowFrame the current frame.
     * @return {@code true} if the combo is still valid, {@code false}
     * otherwise.
     */
    public boolean isComboValid(int nowFrame) {
        return (nowFrame - this.lastHitFrame) <= GameSetting.COMBO_LIMIT;
    }

    /**
     * Check whether the character is in a hit-stun state.
     *
     * @return {@code true} if the character is in hitstun, {@code false} otherwise.
     */
    public boolean isInHitstun() {
        return this.state != State.DOWN && this.remainingFrame > 0;
    }

    /**
     * Check whether processing is running in the simulator.
     *
     * @return {@code true} if the process is executed in the simulator,
     * {@code false} otherwise.
     */
    public boolean isSimulateProcess() {
        return this.isSimulateProcess;
    }

    public void close(){
        // close all sound sources
        sourceDefault.close();
        sourceLanding.close();
        sourceWalking.close();
        sourceProjectTiles[0].close();
        sourceProjectTiles[1].close();
        sourceProjectTiles[2].close();
        sourceEnergyChange.close();
        sourceBorderAlert.close();
        sourceHeartBeat.close();
    }
}
