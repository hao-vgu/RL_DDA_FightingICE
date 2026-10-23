package informationcontainer;

import protoc.MessageProto.GrpcRoundResult;
import setting.FlagSetting;
import struct.FrameData;

/**
 * Stores the result of a round.
 */
public class RoundResult {

	/**
	 * Current round number.
	 */
	private int currentRound;

	/**
	 * Array containing the remaining HP for P1 and P2.
	 */
	private int[] remainingHPs;

	/**
	 * Elapsed frames in the round.
	 */
	private int elapsedFrame;

	/**
	 * Class constructor.
	 */
	public RoundResult() {
		this.currentRound = -1;
		this.remainingHPs = new int[2];
		this.elapsedFrame = -1;
	}

	/**
	 * Constructor that initializes RoundResult with the specified values.
	 *
	 * @param round
	 *            Round number.
	 * @param hp
	 *            Remaining HP for P1 and P2.
	 * @param frame
	 *            Elapsed frame count.
	 */
	public RoundResult(int round, int[] hp, int frame) {
		this.currentRound = round;
		this.remainingHPs = hp;
		this.elapsedFrame = frame;
	}

	/**
	 * Constructor that updates RoundResult using result data from the supplied frame data.
	 *
	 * @param frameData
	 *            Game data for the frame.
	 */
	public RoundResult(FrameData frameData) {
		this.currentRound = frameData.getRound();
		this.elapsedFrame = frameData.getFramesNumber() + 1;
		this.remainingHPs = new int[] { frameData.getCharacter(true).getHp(), frameData.getCharacter(false).getHp() };

		if (FlagSetting.limitHpFlag) {
			this.remainingHPs[0] = Math.max(this.remainingHPs[0], 0);
			this.remainingHPs[1] = Math.max(this.remainingHPs[1], 0);
		}
	}

	/**
	 * Return the current round number.
	 *
	 * @return the current round number
	 */
	public int getRound() {
		return this.currentRound;
	}

	/**
	 * Return the array containing the remaining HP for P1 and P2.
	 *
	 * @return the array containing the remaining HP for P1 and P2
	 */
	public int[] getRemainingHPs() {
		return this.remainingHPs.clone();
	}

	/**
	 * Return the elapsed frame count.
	 *
	 * @return the elapsed frame count
	 */
	public int getElapsedFrame() {
		return this.elapsedFrame;
	}
	
	public GrpcRoundResult toProto() {
		return GrpcRoundResult.newBuilder()
    			.setCurrentRound(this.currentRound)
    			.addRemainingHps(this.remainingHPs[0])
    			.addRemainingHps(this.remainingHPs[1])
    			.setElapsedFrame(this.elapsedFrame)
    			.build();
	}
	
}
