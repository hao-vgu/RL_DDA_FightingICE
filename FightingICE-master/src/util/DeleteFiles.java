package util;

import java.io.File;

/**
 * Singleton class for deleting log files.
 */
public class DeleteFiles {
	private String[] foldersName;

	/**
	 * Class constructor.
	 */
	private DeleteFiles() {
		System.out.println("delete files...");
		// Name of the folder to delete.
		this.foldersName = new String[] { "log/point", "log/replay", "log/sound" };
	}

	/**
	 * Get the singleton DeleteFiles instance.
	 *
	 * @return the singleton DeleteFiles instance
	 */
	public static DeleteFiles getInstance() {
		return DeleteFilesHolder.instance;
	}

	/**
	 * Holder class that creates the instance the first time getInstance() is called.
	 */
	private static class DeleteFilesHolder {
		private static final DeleteFiles instance = new DeleteFiles();
	}

	/**
	 * Delete files in the point and replay folders.
	 */
	public void deleteFiles() {
		for (String folderName : foldersName) {
			File[] fileList = (new File(folderName)).listFiles();
			if (fileList != null) {
				for (File file : fileList) {
					if (!file.getName().equals(".gitkeep"))
						file.delete();
				}
			}
		}
		System.out.println("finish deleting");
	}
}
