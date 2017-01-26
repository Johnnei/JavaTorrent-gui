package org.johnnei.javatorrent.utils;

public class StringFormatUtils {

	/**
	 * Compacts a size of a file/speed in bytes to their smaller notations (kb, mb, etc) upto (including) TB
	 *
	 * @param size The size in bytes
	 * @return The string which equals the size in the smallest notation
	 */
	public static String compactByteSize(double size) {
		String[] names = { "B", "KB", "MB", "GB", "TB" };
		int pointer = 0;
		while (pointer < names.length && size >= 1000) {
			size /= 1000;
			++pointer;
		}
		String stringSpeed = Double.toString(size);
		if (stringSpeed.contains(".")) {
			String[] parts = stringSpeed.split("\\.");
			if (parts[1].length() == 1 && parts[1].equals("0")) {
				stringSpeed = parts[0];
			} else {
				stringSpeed = parts[0] + "." + parts[1].substring(0, 1);
			}
		}
		return stringSpeed + " " + names[pointer];
	}

	/**
	 * Converts seconds into the HH:MM:SS string
	 * @param seconds The amount of seconds
	 * @return The string which formats the time
	 */
	public static String timeToString(long seconds) {
		if (seconds < 10) {
			return "0:0" + seconds;
		} else if (seconds < 60) {
			return "0:" + seconds;
		} else {
			int hours = 0;
			int minutes = 0;
			while (seconds >= 3600) {
				++hours;
				seconds -= 3600;
			}
			while (seconds >= 60) {
				++minutes;
				seconds -= 60;
			}
			String sHours = Integer.toString(hours);
			String sMinutes = Integer.toString(minutes);
			String sSeconds = Long.toString(seconds);
			if (sSeconds.length() < 2)
				sSeconds = "0" + sSeconds;
			if (hours > 0) {
				if (sMinutes.length() < 2)
					sMinutes = "0" + sMinutes;
				return sHours + ":" + sMinutes + ":" + sSeconds;
			} else {
				return sMinutes + ":" + sSeconds;
			}
		}
	}

	public static String progressToString(double d) {
		String progressString = Double.toString(d);
		if (progressString.equals("NaN"))
			progressString = "0.0";
		if (progressString.contains(".")) {
			int pointIndex = progressString.indexOf(".");
			progressString = progressString.substring(0, Math.min(pointIndex + 3, progressString.length()));
		}
		return progressString;
	}

}
