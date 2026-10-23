package fighting;

import image.Image;

/**
 * Manages projectile effects.<br>
 * Extends the Effect class.
 */
public class LoopEffect extends Effect {

	/**
	 * Constructor that creates a LoopEffect instance from the specified data.
	 *
	 * @param attack
	 *            Attack object.
	 * @param hitImages
	 *            All effect images corresponding to the attack object.
	 */
	public LoopEffect(Attack attack, Image[] hitImages) {
		super(attack, hitImages);
		attack.setIsLive(true);
	}

	/**
	 * Updates the effect's state.<br>
	 * If effect display time has elapsed, set the elapsed frame to 0;
	 *
	 * @return {@code true}
	 */
	public boolean update() {
		if (!super.update()) {
			this.currentFrame = 0;
		}

		return true;
	}

}
