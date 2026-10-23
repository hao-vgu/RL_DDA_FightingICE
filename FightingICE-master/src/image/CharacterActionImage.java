package image;

/**
 * Represents images for a character action.
 */
public class CharacterActionImage {

	/**
	 * Character name.
	 */
	private String characterName;

	/**
	 * Action name.
	 */
	private String actionName;

	/**
	 * Total number of frames in the action.
	 */
	private int frameNumber;

	/**
	 * Array containing all images for the action.
	 */
	private Image[] actionImage;

	/**
	 * Constructor that creates a CharacterActionImage instance.
	 * Initialize the instance with the character name, action name, total frame count, and all action images.
	 *
	 * @param characterName
	 *            Character name.
	 * @param actionName
	 *            Action name.
	 * @param frameNumber
	 *            Total number of frames in the action.
	 * @param actionImage
	 *            All images for the action.
	 */
	public CharacterActionImage(String characterName, String actionName, int frameNumber, Image[] actionImage) {
		this.characterName = characterName;
		this.actionName = actionName;
		this.frameNumber = frameNumber;
		this.actionImage = actionImage;
	}

	/**
	 * Constructor that creates a CharacterActionImage instance containing only the character and action names.
	 * Used to search for a CharacterActionImage instance.
	 *
	 * @param characterName
	 *            Character name.
	 * @param actionName
	 *            Action name.
	 */
	public CharacterActionImage(String characterName, String actionName) {
		this.characterName = characterName;
		this.actionName = actionName;
	}

	/**
	 * Compare the supplied character and action names with those in this CharacterActionImage instance.
	 *
	 * @return {@code true} if the character and action names match; {@code false} otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CharacterActionImage) {
			CharacterActionImage temp = (CharacterActionImage) obj;

			return this.characterName.equals(temp.getCharacterName()) && this.actionName.equals(temp.getActionName());
		} else {
			return false;
		}
	}

	/**
	 * Return the character name.
	 *
	 * @return the character name
	 */
	public String getCharacterName() {
		return this.characterName;
	}

	/**
	 * Return the action name.
	 *
	 * @return the action name
	 */
	public String getActionName() {
		return this.actionName;
	}

	/**
	 * Return the total number of frames in the action.
	 *
	 * @return the total number of frames in the action
	 */
	public int getFrameNumber() {
		return this.frameNumber;
	}

	/**
	 * Return all images for the action.
	 *
	 * @return all images for the action
	 */
	public Image[] getActionImage() {
		return this.actionImage.clone();
	}

}
