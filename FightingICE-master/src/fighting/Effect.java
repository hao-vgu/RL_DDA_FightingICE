package fighting;

import image.Image;

/**
 * Base class for managing attacks and their corresponding effect images.
 */
public class Effect {

	/**
	 * Maximum number of frames to display a single image.
	 */
	protected static final int FRAME_PER_IMAGE = 5;

	/**
	 * The attack related to this effect.
	 */
	protected Attack attack;

	/**
	 * The all of effect's images.
	 */
	protected Image[] hitImages;

	/**
	 * Elapsed time since the effect was created.
	 */
	protected int currentFrame;

	/**
	 * Number of frames to display a single image.
	 */
	protected int framesPerImage;

	/**
	 * Constructor that creates an Effect instance from the supplied data.
	 *
	 * @param attack
	 *            Attack instance.
	 * @param hitImages
	 *            All effect images corresponding to the attack.
	 * @param framesPerImage
	 *            Number of frames to display each effect image.
	 */
	public Effect(Attack attack, Image[] hitImages, int framesPerImage) {
		this.attack = attack;
		this.hitImages = hitImages;
		this.currentFrame = 0;
		this.framesPerImage = framesPerImage;
	}

	/**
	 * Constructor that creates an Effect instance from the supplied data.
	 *
	 * @param attack
	 *            Attack instance.
	 * @param hitImages
	 *            All effect images corresponding to the attack.
	 */
	public Effect(Attack attack, Image[] hitImages) {
		this(attack, hitImages, FRAME_PER_IMAGE);
	}

	/**
	 * Updates the effect's state.
	 *
	 * @return {@code true} if the elapsed time since the effect was generated
	 *         has not exceeded the time for displaying the effect,
	 *         {@code false} otherwise
	 */
	public boolean update() {
		return ++this.currentFrame < (this.hitImages!=null ? this.hitImages.length : 6 * this.framesPerImage);
	}

	/**
	 * Returns the effect's image.
	 *
	 * @return the effect's image
	 */
	public Image getImage() {
		return this.hitImages[(this.currentFrame / this.framesPerImage) % this.hitImages.length];
	}

	/**
	 * Returns the all of effect's images.
	 *
	 * @return the all of effect's images
	 */
	public Image[] getImages() {
		return this.hitImages;
	}

	/**
	 * Returns the attack related to this effect.
	 *
	 * @return the attack related to this effect
	 */
	public Attack getAttack() {
		return this.attack;
	}
}
