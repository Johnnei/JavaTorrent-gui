package org.johnnei.javatorrent.utils.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Properties;

public class Config {

	private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

	/**
	 * The singleton config instance
	 */
	private static Config instance;

	/**
	 * Gets the singleton config file for JavaTorrent
	 *
	 * @return The configuration
	 */
	public static Config getConfig() {
		if (instance == null) {
			instance = new Config();
			instance.load();
		}
		return instance;
	}

	/**
	 * The properties of the client
	 */
	private Properties properties;

	/**
	 * The file which the config is being stored
	 */
	private Path configFile;

	private Path metadataDirectory;

	private Path tempDirectory;

	private Config() {
		Properties defaultProperties = new Properties();
		defaultProperties.put("peer-max", "500");
		defaultProperties.put("peer-max_burst_ratio", "1.5");
		defaultProperties.put("peer-max_concurrent_connecting", "2");
		defaultProperties.put("peer-max_connecting", "50");
		defaultProperties.put("download-output_folder", ".");
		defaultProperties.put("download-port", "6881");
		defaultProperties.put("general-show_all_peers", "false");

		properties = new Properties(defaultProperties);

		getFile("JavaTorrent.cfg");
	}

	private void getFile(String filename) {
		Path userHome = Paths.get(System.getProperty("user.home"));
		String os = System.getProperty("os.name").toLowerCase();

		Path configDirectory;
		if (os.contains("win")) {
			configDirectory = userHome.resolve("Appdata").resolve("Roaming");
		} else  {
			configDirectory = userHome.resolve(".local").resolve("share");
		}

		configDirectory = configDirectory.resolve("javatorrent").toAbsolutePath();
		metadataDirectory = configDirectory.resolve("metadata");
		tempDirectory = configDirectory.resolve("temp");

		Collection<Path> directoriesToCheck = List.of(configDirectory, metadataDirectory, tempDirectory);
		for (Path directory : directoriesToCheck) {
			if (Files.notExists(directory)) {
				try {
					Files.createDirectories(directory);
				} catch (IOException e) {
					throw new IllegalStateException("Failed to create configuration directory at " + directory.toAbsolutePath(), e);
				}
			}
		}

		configFile = configDirectory.resolve(filename);
		LOGGER.info("Loading configuration in {}", configFile.toAbsolutePath());
		if (Files.notExists(configFile)) {
			try {
				Files.createFile(configFile);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to create empty config file", e);
			}
		}
	}

	/**
	 * Loads all settings from the config file
	 */
	void load() {
		try (BufferedReader inputStream = Files.newBufferedReader(configFile)) {
			properties.load(inputStream);
		} catch (IOException e) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			load();
		}
	}

	/**
	 * Saves the config file to the hdd
	 */
	public void save() {
		try (BufferedWriter outStream = Files.newBufferedWriter(configFile)) {
			properties.store(outStream, "JavaTorrent GUI Configuration");
		} catch (IOException e) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			save();
		}
	}

	/**
	 * Gets a value from the config
	 *
	 * @param key The key of the pair
	 * @return The value by the given key
	 */
	private String get(String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException(getMissingConfigError(key));
		} else {
			return value;
		}
	}

	/**
	 * Overrides a pair of values
	 *
	 * @param key The key of the pair
	 * @param value The value of the pair
	 */
	public void set(String key, Object value) {
		properties.put(key, value.toString());
		save();
	}

	/**
	 * Gets an integer config value
	 *
	 * @param key The key of the pair
	 * @return The value by the given key
	 */
	public int getInt(String key) {
		String val = get(key);
		if (isInt(val)) {
			return Integer.parseInt(val);
		} else {
			throw new IllegalArgumentException(getMissingConfigError(key));
		}
	}

	public boolean getBoolean(String key) {
		String val = get(key);
		if (isBoolean(val)) {
			return parseBoolean(val);
		} else {
			throw new IllegalArgumentException(getMissingConfigError(key));
		}
	}

	/**
	 * Gets an float config value
	 *
	 * @param key The key of the pair
	 * @return The value by the given key
	 */
	public float getFloat(String key) {
		String val = get(key);
		if (isFloat(val)) {
			return Float.parseFloat(val);
		} else {
			throw new IllegalArgumentException(getMissingConfigError(key));
		}
	}

	/**
	 * Gets an String config value
	 *
	 * @param key The key of the pair
	 * @return The value by the given key
	 */
	public String getString(String key) {
		return get(key);
	}

	public Path getMetadataDirectory() {
		return metadataDirectory;
	}

	public Path getTempFolder() {
		return tempDirectory;
	}

	private boolean isBoolean(String s) {
		try {
			parseBoolean(s);
			return true;
		} catch (IllegalFormatException e) {
			return false;
		}
	}

	private boolean parseBoolean(String s) {
		s = s.toLowerCase();
		String[] trueList = new String[] { "yes", "1", "true" };
		String[] falseList = new String[] { "no", "0", "false" };
		for(int i = 0; i < trueList.length; i++) {
			if(trueList[i].equals(s)) {
				return true;
			} else if(falseList[i].equals(s)) {
				return false;
			}
		}
		throw new NumberFormatException(String.format("Invalid boolean string: %s", s));
	}

	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isFloat(String s) {
		try {
			Float.parseFloat(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private String getMissingConfigError(String key) {
		return String.format("Configuration Setting \"%s\" has not been registered", key);
	}

}
