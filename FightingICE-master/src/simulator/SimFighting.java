package simulator;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import aiinterface.CommandCenter;
import command.CommandTable;
import enumerate.Action;
import fighting.Attack;
import fighting.Character;
import fighting.Fighting;
import fighting.LoopEffect;
import fighting.Motion;
import setting.GameSetting;
import struct.AttackData;
import struct.CharacterData;
import struct.FrameData;
import struct.Key;

/**
 * Processes matches and updates character data during simulation.
 */
public class SimFighting extends Fighting {

	/**
	 * Key input.
	 */
	private ArrayList<Deque<Key>> inputKeys;

	/**
	 * Action selected by the input.
	 */
	private ArrayList<Deque<Action>> inputActions;

	/**
	 * Array containing CommandCenter instances.
	 */
	private CommandCenter[] commandCenter;

	/**
	 * Class constructor.
	 */
	public SimFighting() {
		this.playerCharacters = new Character[2];
		this.projectileDeque = new LinkedList<LoopEffect>();
		this.commandTable = new CommandTable();

		this.inputKeys = new ArrayList<Deque<Key>>(2);
		this.inputActions = new ArrayList<Deque<Action>>(2);
		this.commandCenter = new CommandCenter[2];
	}

	/**
	 * Initialize.
	 *
	 * @param motionList
	 *            List containing P1 and P2 motions.
	 * @param actionList
	 *            List containing P1 and P2 actions.
	 * @param frameData
	 *            frame data at the start of simulation
	 * @param playerNumber
	 *            boolean value which identifies P1/P2. {@code true} if the
	 *            player is P1, or {@code false} if P2.
	 */
	public void initialize(ArrayList<ArrayList<Motion>> motionList, ArrayList<Deque<Action>> actionList,
			FrameData frameData, boolean playerNumber) {

		for (int i = 0; i < 2; i++) {
			this.playerCharacters[i] = new Character(frameData.getCharacter(i == 0), motionList.get(i));

			this.inputKeys.add(this.playerCharacters[i].getProcessedCommand());
			this.inputActions.add(actionList.get(i));

			this.commandCenter[i] = new CommandCenter();
			this.commandCenter[i].setFrameData(frameData, i == 0);
		}

		Deque<AttackData> projectiles = frameData.getProjectiles();
		for (AttackData temp : projectiles) {
			this.projectileDeque.addLast(new LoopEffect(new Attack(temp), null));
		}
	}

	/**
	 * Process one frame of the match.<br>
	 * Processing is performed in the following order:<br>
	 * <ol>
	 * <li>Perform actions based on key input</li>
	 * <li>Process attack hit detection and update character parameters such as HP</li>
	 * <li>Update attack parameters</li>
	 * <li>Update character states</li>
	 * </ol>
	 *
	 * @param currentFrame
	 *            The current frame.
	 */
	public void processingFight(int currentFrame) {
		// 1. Execute commands and process the match.
		processingCommands();
		// 2. Process hit detection.
		calculationHit(currentFrame);
		// 3. Update attack parameters.
		updateAttackParameter();
		// 4. Update character data.
		updateCharacter();
	}

	/**
	 * Perform actions based on the key input and actions supplied when the simulation started.
	 */
	public void processingCommands() {

		for (int i = 0; i < 2; i++) {
			Deque<Key> keyList = this.inputKeys.get(i);
			Deque<Action> actList = this.inputActions.get(i);

			if (keyList.size() > GameSetting.INPUT_LIMIT-1) {
				keyList.removeFirst();
			}

			if (!this.playerCharacters[i].getInputCommand().isEmpty()) {
				Deque<Key> temp = this.playerCharacters[i].getProcessedCommand();
				keyList.addLast(temp.removeFirst());
				this.playerCharacters[i].setInputCommand(temp);
				keyList.add(new Key(this.playerCharacters[i].getInputCommand().getFirst()));

				Action act = this.commandTable.interpretationCommandFromKey(this.playerCharacters[i], keyList);
				if (ableAction(this.playerCharacters[i], act)) {
					this.playerCharacters[i].runAction(act, true);
				}

			} else if (actList != null) {
				if (!actList.isEmpty()) {

					if (ableAction(this.playerCharacters[i], actList.getFirst()) && !commandCenter[i].getSkillFlag()) {
						this.commandCenter[i].commandCall(actList.removeFirst().name());

					} else if (this.playerCharacters[i].isControl() && !this.commandCenter[i].getSkillFlag()) {
						actList.removeFirst();
					}
				}

				this.inputKeys.get(i).add(this.commandCenter[i].getSkillKey());
				Action act = this.commandTable.interpretationCommandFromKey(this.playerCharacters[i], keyList);
				this.playerCharacters[i].setInputCommand(this.commandCenter[i].getSkillKeys());

				if (ableAction(this.playerCharacters[i], act)) {
					this.playerCharacters[i].runAction(act, true);
				}
			}
		}
	}

	@Override
	protected void calculationHit(int currentFrame) {
		boolean[] isHit = { false, false };

		// Process projectiles.
		int dequeSize = this.projectileDeque.size();
		for (int i = 0; i < dequeSize; i++) {
			LoopEffect projectile = this.projectileDeque.removeFirst();
			int opponentIndex = projectile.getAttack().isPlayerNumber() ? 1 : 0;

			if (detectionHit(this.playerCharacters[opponentIndex], projectile.getAttack())) {
				int myIndex = opponentIndex == 0 ? 1 : 0;
				this.playerCharacters[opponentIndex].hitAttack(this.playerCharacters[myIndex], projectile.getAttack(),
						currentFrame);

			} else {
				this.projectileDeque.addLast(projectile);
			}
		}

		// Process normal attacks.
		for (int i = 0; i < 2; i++) {
			int opponentIndex = i == 0 ? 1 : 0;
			Attack attack = this.playerCharacters[i].getAttack();

			if (detectionHit(this.playerCharacters[opponentIndex], attack)) {
				isHit[i] = true;
				// Update parameters such as HP.
				// Character is shared with Fighting, so this would also trigger sound playback.
				this.playerCharacters[opponentIndex].hitAttack(this.playerCharacters[i], attack, currentFrame);
			}
		}

		for (int i = 0; i < 2; i++) {
			if (isHit[i]) {
				this.playerCharacters[i].setHitConfirm(true);
				this.playerCharacters[i].destroyAttackInstance();
			}

			if (!this.playerCharacters[i].isComboValid(currentFrame)) {
				this.playerCharacters[i].setHitCount(0);
			}
		}
	}

	@Override
	protected void updateAttackParameter() {
		// Updates the parameters of all of projectiles appearing in the stage
		int dequeSize = this.projectileDeque.size();
		for (int i = 0; i < dequeSize; i++) {

			LoopEffect projectile = this.projectileDeque.removeFirst();
			if (projectile.getAttack().updateProjectileAttack()) {
				this.projectileDeque.addLast(projectile);
			}
		}

		// Updates the parameters of all of attacks excepted projectile
		// conducted by both characters
		for (int i = 0; i < 2; ++i) {
			if (this.playerCharacters[i].getAttack() != null) {
				if (!this.playerCharacters[i].getAttack().update(this.playerCharacters[i])) {
					this.playerCharacters[i].destroyAttackInstance();
				}
			}
		}
	}

	@Override
	protected void updateCharacter() {
		for (int i = 0; i < 2; ++i) {
			// update each character.
			this.playerCharacters[i].update();

			// enque object attack if the data is missile decision
			if (this.playerCharacters[i].getAttack() != null) {
				if (this.playerCharacters[i].getAttack().isProjectile()) {

					this.projectileDeque.addLast(new LoopEffect(this.playerCharacters[i].getAttack(), null));
					this.playerCharacters[i].destroyAttackInstance();
				}
			}

			// change player's direction
			if (playerCharacters[i].isControl()) {
				playerCharacters[i].frontDecision(playerCharacters[i == 0 ? 1 : 0].getHitAreaCenterX());
			}
		}
		// run pushing effect
		detectionPush();
		// run collision of first and second character.
		detectionFusion();
		// run effect when character's are in the end of stage.
		decisionEndStage();
	}

	@Override
	public FrameData createFrameData(int nowFrame, int round) {
		CharacterData[] characterData = new CharacterData[2];
		for (int i = 0; i < 2; i++) {
			characterData[i] = new CharacterData(this.playerCharacters[i]);
			characterData[i].setProcessedCommand(this.inputKeys.get(i));
		}

		Deque<AttackData> newAttackDeque = new LinkedList<AttackData>();
		for (LoopEffect loopEffect : this.projectileDeque) {
			newAttackDeque.addLast(new AttackData(loopEffect.getAttack()));
		}

		return new FrameData(characterData, nowFrame, round, newAttackDeque);
	}

}
