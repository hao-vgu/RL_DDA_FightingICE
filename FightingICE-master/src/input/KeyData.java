package input;

import struct.Key;

/**
 * Manages the keys entered by each player during a frame.
 */
public class KeyData {

	/**
	 * Array containing input keys for P1 and P2.<br>
	 * Index 0 is P1, index 1 is P2.
	 */
	private Key[] keys;

	/**
	 * Class constructor.
	 */
	public KeyData() {
		this.keys = new Key[2];
		for (int i = 0; i < this.keys.length; i++) {
			this.keys[i] = new Key();
		}
	}

	/**
	 * Constructor that creates KeyData from each player's key input.
	 *
	 * @param keys
	 *            Array containing input keys for P1 and P2.<br>
	 *            Index 0 is P1, index 1 is P2.
	 */
	public KeyData(Key[] keys) {
		this.keys = new Key[keys.length];
		for (int i = 0; i < keys.length; i++) {
			this.keys[i] = new Key(keys[i]);
		}
	}

	/**
	 * Copy constructor that creates a copy of the supplied KeyData instance.<br>
	 * If the argument is null, initialize the key array and create a new instance.
	 *
	 * @param keyData
	 *            Input key data for P1 and P2.<br>
	 *            Index 0 is P1, index 1 is P2.
	 */
	public KeyData(KeyData keyData) {
		if (keyData != null) {
			this.keys = new Key[keyData.getKeys().length];
			for (int i = 0; i < keyData.getKeys().length; i++) {
				this.keys[i] = new Key(keyData.getKeys()[i]);
			}
		} else {
			this.keys = new Key[2];
			for (int i = 0; i < this.keys.length; i++) {
				this.keys[i] = new Key();
			}
		}
	}

	/**
	 * Return the array containing input keys for P1 and P2.
	 *
	 * @return array containing input keys for P1 and P2<br>
	 *         Index 0 is P1, index 1 is P2.
	 */
	public Key[] getKeys() {
		return this.keys.clone();
	}
}
