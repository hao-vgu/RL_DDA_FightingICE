package loader;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import aiinterface.AIController;
import aiinterface.AIInterface;
import image.CharacterActionImage;
import image.Image;
import manager.GraphicManager;
import manager.SoundManager;
import setting.FlagSetting;
import setting.GameSetting;
import setting.LaunchSetting;
import setting.ResourceSetting;

/**
 * Singleton class for loading character settings, images, and other resources.
 */
public class ResourceLoader {

	/**
	 * List of loaded resources.
	 */
	private ArrayList<String> loadedResources;

	/**
	 * Class constructor.
	 */
	private ResourceLoader() {
		Logger.getAnonymousLogger().log(Level.INFO, "Create instance: " + ResourceLoader.class.getName());
		this.loadedResources = new ArrayList<String>();
	}

	/**
	 * Get the singleton ResourceLoader instance.
	 *
	 * @return the singleton ResourceLoader instance
	 */
	public static ResourceLoader getInstance() {
		return ResourceLoaderHolder.instance;
	}

	/**
	 * Holder class that creates the instance the first time getInstance() is called.
	 */
	private static class ResourceLoaderHolder {
		private static final ResourceLoader instance = new ResourceLoader();
	}

	/**
	 * Load all images and sounds required by the game.
	 */
	public void loadResource() {
		if (!LaunchSetting.isExpectedProcessingMode(LaunchSetting.HEADLESS_MODE)) return;
		
		Logger.getAnonymousLogger().log(Level.INFO, "Loading the resources");
		String graphicPath = "./data/graphics/";
		String characterGraphicPath = "./data/characters/";

		// Load projectile images.
		if (!isLoaded("hadouken")) {
			loadImages(GraphicManager.getInstance().getProjectileImageContainer(),
					graphicPath + ResourceSetting.PROJECTILE_DIRECTORY);

			addLoadedResource("hadouken");
			Logger.getAnonymousLogger().log(Level.INFO, "Hadouken images have been loaded.");
		}

		// Load special-move images.
		if (!isLoaded("super")) {
			loadImages(GraphicManager.getInstance().getUltimateAttackImageContainer(),
					graphicPath + ResourceSetting.SUPER_DIRECTORY);

			addLoadedResource("super");
			Logger.getAnonymousLogger().log(Level.INFO, "Ultimate attack images have been loaded.");
		}

		// Load the digit images (0–9).
		if (!isLoaded("hitCounter")) {
			loadImages(GraphicManager.getInstance().getCounterTextImageContainer(),
					graphicPath + ResourceSetting.COUNTER_DIRECTORY);

			addLoadedResource("hitCounter");
			Logger.getAnonymousLogger().log(Level.INFO, "Hit counter text images have been loaded.");
		}

		// Load the "Hit" text image.
		if (!isLoaded("hitText")) {
			loadImages(GraphicManager.getInstance().getHitTextImageContainer(),
					graphicPath + ResourceSetting.HIT_TEXT_DIRECTORY);

			addLoadedResource("hitText");
			Logger.getAnonymousLogger().log(Level.INFO, "Hit text image has been loaded.");
		}

		// Load hit effects.
		if (!isLoaded("hitEffect")) {
			loadHitEffectImage(graphicPath + ResourceSetting.HIT_DIRECTORY);

			addLoadedResource("hitEffect");
			Logger.getAnonymousLogger().log(Level.INFO, "Hit effect images have been loaded.");
		}

		// Load background images.
		if (!isLoaded("background")) {
			loadBackgroundImage(GraphicManager.getInstance().getBackgroundImage(),
					graphicPath + ResourceSetting.BACKGROUND_DIRECTORY);

			addLoadedResource("background");
			Logger.getAnonymousLogger().log(Level.INFO, "Background image has been loaded.");
		}

		// Load uppercut images.
		loadUpperImages(graphicPath + ResourceSetting.UPPER_DIRECTORY);
		Logger.getAnonymousLogger().log(Level.INFO, "Upper attack images have been loaded.");

		// Load character images.
		loadCharacterImages(characterGraphicPath);
		Logger.getAnonymousLogger().log(Level.INFO, "Character images have been loaded.");

		// Load sound effects.
		if (!isLoaded("soundEffect") && FlagSetting.enableBuiltinSound) {
			loadSoundEffect();

			addLoadedResource("soundEffect");
			Logger.getAnonymousLogger().log(Level.INFO, "Sound effects have been loaded.");
		}

		// Load the background music.
		if (!isLoaded("BGM") && FlagSetting.enableBuiltinSound) {
			loadBackGroundMusic();

			addLoadedResource("BGM");
			Logger.getAnonymousLogger().log(Level.INFO, "BGM has been loaded.");
		}

	}

	/**
	 * Open the specified file and return its BufferedReader.
	 *
	 * @param filePath
	 *            Path to the file to load.
	 *
	 *
	 * @return BufferedReader
	 */
	public BufferedReader openReadFile(String filePath) {
		try {
			File file = new File(filePath);
			return new BufferedReader(new FileReader(file));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Open the specified file and return its output stream.
	 *
	 * @param filePath
	 *            Path to the file to load.
	 *
	 * @return the output stream
	 */
	public DataOutputStream openDataOutputStream(String filePath) {
		try {
			File file = new File(filePath);
			return new DataOutputStream(new FileOutputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Open the specified file and return its BufferedReader.
	 *
	 * @param filePath
	 *            Path to the file to load.
	 * @param mode
	 *            Whether to append bytes to the end of the file.
	 *
	 * @return BufferedReader
	 */
	public PrintWriter openWriteFile(String filePath, boolean mode) {
		try {
			File file = new File(filePath);
			return new PrintWriter(new BufferedWriter(new FileWriter(file, mode)));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Read all AI names from the AI directory and return a list containing them.
	 *
	 * @return list containing the AI names
	 */
	public ArrayList<String> loadAllAINames() {
		String[] files = new File("./data/ai").list();
		ArrayList<String> temp = new ArrayList<String>();

		for (int i = 0; i < files.length; i++) {
			if (files[i].endsWith(".jar")) {
				temp.add(files[i].substring(0, files[i].indexOf(".jar")));
			}
		}

		return temp;
	}

	/**
	 * Load the JAR file for the specified AI and return a controller containing its information.
	 *
	 * @param aiName
	 *            Name of the AI to load.
	 *
	 * @return controller containing the loaded AI information<br>
	 *         Returns null if the AI could not be loaded.
	 */
	public AIController loadAI(String aiName) {
		File file = new File("./data/ai/" + aiName + ".jar");

		try {
			ClassLoader cl = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() });
			Class<?> c = cl.loadClass(aiName);
			AIInterface ai = (AIInterface) c.getDeclaredConstructor().newInstance();
			return new AIController(ai);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Return the names, without extensions, of all files in the specified directory.<br>
	 * Specify the file extension to load as an argument.
	 *
	 * @param directoryPath
	 *            Path to the directory containing the files to list.
	 * @param extension
	 *            Extension of the files to load.
	 *
	 * @return array containing the names of all loaded files without their extensions<br>
	 *         Returns null if no files were loaded.
	 */
	public ArrayList<String> loadFileNames(String directoryPath, String extension) {
		File[] files = new File(directoryPath).listFiles();
		sortByFileName(files);
		ArrayList<String> fileNames = new ArrayList<String>();

		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith(extension)) {
				String fileName = files[i].getName();
				fileNames.add(fileName.substring(0, fileName.lastIndexOf(".")));
			}
		}

		return fileNames;
	}
	
	public ArrayList<String> loadSoundNames() {
		File[] files = new File(ResourceSetting.SOUND_DIRECTORY).listFiles();
		sortByFileName(files);
		ArrayList<String> soundNames = new ArrayList<String>();
		
		for (File file: files) {
			if (file.isDirectory()) {
				soundNames.add(file.getName());
			}
		}
		return soundNames;
	}

	/**
	 * Load character images and add them to the list.
	 *
	 * @param path
	 *            Path to the directory containing each character's images.
	 */
	public void loadCharacterImages(String path) {
		for (int i = 0; i < 2; i++) {
			if (!isLoaded(LaunchSetting.characterNames[i] + "_Graphic") ) {
				try {
					BufferedReader br = openReadFile(path + LaunchSetting.characterNames[i] + "/Motion.csv");

					String line;
					br.readLine(); // ignore header

					while ((line = br.readLine()) != null) {
						String[] data = line.split(",", 0);
						String actionName = data[0];
						int frameNumber = Integer.valueOf(data[1]);
						String imageName = data[33];

						Image[] actionImage = new Image[frameNumber];
						String dirPath = path + LaunchSetting.characterNames[i] + "/graphics/" + imageName;

						// Get the directory containing the specified character's graphics.
						File[] files = new File(dirPath).listFiles();
						sortByFileName(files);

						int num = 0;
						for (int j = 0; j < files.length; j++) {
							if (j >= frameNumber) {
								break;
							}

							actionImage[j] = loadImage(files[j].getPath());
							num++;
						}

						// If there are fewer images than the frame count specified in Motion.csv, fill in the missing images.
						if (num < frameNumber) {
							for (int j = num; j < frameNumber; j++) {
								actionImage[j] = actionImage[0];
							}
						}
						CharacterActionImage temp = new CharacterActionImage(LaunchSetting.characterNames[i],
								actionName, frameNumber, actionImage);
						GraphicManager.getInstance().getCharacterImageContainer().add(temp);
					}

					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			addLoadedResource(LaunchSetting.characterNames[i] + "_Graphic");
		}
	}

	/**
	 * Load the background images.
	 *
	 * @param container
	 *            List in which to store the images.
	 * @param path
	 *            Path to the background image directory.
	 */
	public void loadBackgroundImage(ArrayList<Image> container, String path) {
		switch (LaunchSetting.backgroundType) {
		case BLACK:
			container.add(loadTextureFromBufferedImage(createBackgroundImage(0, 0, 0)));
			break;
			
		case GREY:
			container.add(loadTextureFromBufferedImage(createBackgroundImage(128, 128, 128)));
			break;
			
		default:
			loadImages(container, path);
			break;
		}
	}
	
	private BufferedImage createBackgroundImage(int r, int g, int b) {
		Color color = new Color(r, g, b);
		BufferedImage bg = new BufferedImage(GameSetting.STAGE_WIDTH, GameSetting.STAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < bg.getWidth(); x++) {
			for (int y = 0; y < bg.getHeight(); y++) {
				bg.setRGB(x, y, color.getRGB());
			}
		}
		return bg;
	}

	/**
	 * Load an image and return its data.
	 *
	 * @param filePath
	 *            Path to the image to load.
	 *
	 * @return data for the loaded image<br>
	 *         Returns null if the image could not be loaded.
	 */
	public Image loadImage(String filePath) {
		BufferedImage bimg = null;

		try {
			bimg = ImageIO.read(new FileInputStream(new File(filePath)));
			return loadTextureFromBufferedImage(bimg);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Load a texture from a BufferedImage.<br>
	 * Upload the result to OpenGL and return a new image instance containing the assigned texture ID and BufferedImage.
	 *
	 * @param bimg
	 *            BufferedImage from which to load the texture.
	 *
	 * @return the new image instance
	 */
	public Image loadTextureFromBufferedImage(BufferedImage bimg) {
		int textureId = -1;
		
		// Gather all the pixels
		int[] pixels = new int[bimg.getWidth() * bimg.getHeight()];
		bimg.getRGB(0, 0, bimg.getWidth(), bimg.getHeight(), pixels, 0, bimg.getWidth());
		
		if (LaunchSetting.isExpectedProcessingMode(LaunchSetting.STANDARD_MODE)) {
			// Create a ByteBuffer
			ByteBuffer buffer = ByteBuffer.allocateDirect(bimg.getWidth() * bimg.getHeight() * 4)
					.order(ByteOrder.nativeOrder());

			// Iterate through all the pixels and add them to the ByteBuffer
			for (int y = 0; y < bimg.getHeight(); y++) {
				for (int x = 0; x < bimg.getWidth(); x++) {
					// Select the pixel
					int pixel = pixels[y * bimg.getWidth() + x];

					// Add the RED component
					buffer.put(((byte) ((pixel >> 16) & 0xFF)));
					// Add the GREEN component
					buffer.put(((byte) ((pixel >> 8) & 0xFF)));
					// Add the BLUE component
					buffer.put((byte) (pixel & 0xFF));
					// Add the ALPHA component
					buffer.put(((byte) ((pixel >> 24) & 0xFF)));

				}
			}
			buffer.flip();
			
			// Generate a texture ID
			textureId = glGenTextures();

			// Bind the ID to the context
			glBindTexture(GL_TEXTURE_2D, textureId);

			// Setup texture scaling filtering
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

			// Send texture data to OpenGL
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, bimg.getWidth(), bimg.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE,
					buffer);
			buffer = null;
		}

		return new Image(textureId, bimg);
	}

	/**
	 * Load images from the specified directory and add them to the list.
	 *
	 * @param container
	 *            List in which to store the images.
	 * @param path
	 *            Path to the directory to load.
	 */
	private void loadImages(ArrayList<Image> container, String path) {
		File[] files = new File(path).listFiles();
		sortByFileName(files);

		for (File file : files) {
			container.add(loadImage(file.getPath()));
		}
	}

	/**
	 * Load uppercut images and store them in a two-dimensional array.
	 *
	 * @param path
	 *            Path to the image to load.
	 */
	private void loadUpperImages(String path) {
		for (int i = 0; i < 2; i++) {
			String tempPath = path;

			switch (LaunchSetting.characterNames[i]) {
			case "ZEN":
				tempPath += "ZEN/";
				break;
			case "GARNET":
				tempPath += "GARNET/";
				break;
			case "NEZ":
				tempPath += "NEZ/";
				break;
			default:
				tempPath += "LUD/";
			}

			File[] files = new File(tempPath).listFiles();
			sortByFileName(files);

			for (int j = 0; j < files.length; j++) {
				GraphicManager.getInstance().getUpperImageContainer()[i][j] = loadImage(files[j].getPath());
			}
		}
	}

	/**
	 * Load images for effects displayed when an attack hits and store them in a two-dimensional array.
	 *
	 * @param path
	 *            Path to the image to load.
	 */
	private void loadHitEffectImage(String path) {
		File[] dir = new File(path).listFiles();
		sortByFileName(dir);

		for (int i = 0; i < dir.length; i++) {
			File[] files = new File(dir[i].getPath()).listFiles();
			sortByFileName(files);

			for (int j = 0; j < files.length; j++) {
				GraphicManager.getInstance().getHitEffectImageContaier()[i][j] = loadImage(files[j].getPath());
			}
		}
	}
	
	/**
	 * Load sound effects and add them to the map.
	 */
	private void loadSoundEffect() {
		File[] files = new File(ResourceSetting.SOUND_DIRECTORY).listFiles();
		sortByFileName(files);

        for (File file : files) {
            if (!file.getName().equals(ResourceSetting.BGM_FILE) && !file.isDirectory()) {
                SoundManager.getInstance().getSoundBuffers().put(file.getName(),
                        SoundManager.getInstance().createAudioBuffer(file.getPath()));
            }
        }
    }

    /**
     * Load the background music.
     */
    private void loadBackGroundMusic() {
        SoundManager.getInstance().setBackGroundMusicBuffer(SoundManager.getInstance()
                .createAudioBuffer(ResourceSetting.SOUND_DIRECTORY + ResourceSetting.BGM_FILE));
    }

	/**
	 * Check whether a resource has been loaded.
	 *
	 * @param resourceName
	 *            Resource name.
	 *
	 * @return {@code true} if the resource has been loaded; {@code false} otherwise
	 */
	public boolean isLoaded(String resourceName) {
		return this.loadedResources.contains(resourceName);
	}

	/**
	 * Add the loaded resource name to the list of loaded resources.
	 *
	 * @param resourceName
	 *            Name of the loaded resource.
	 */
	public void addLoadedResource(String resourceName) {
		this.loadedResources.add(resourceName);
	}

	/**
	 * Sort multiple files in lexicographic order.
	 *
	 * @param files
	 *            Array containing the files to sort.
	 */
	public void sortByFileName(File[] files) {
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File file1, File file2) {
				return file1.getName().compareTo(file2.getName());
			}
		});
	}

}
