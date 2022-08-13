package nl.kiipdevelopment.pencil.utils;

public final class MathUtils {
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		
		final long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public static boolean isBetween(long number, long min, long max) {
		return number >= min && number <= max;
	}
	
	public static boolean isBetween(double number, double min, double max) {
		return number >= min && number <= max;
	}
	
	public static boolean isBetweenUnordered(long number, long compare1, long compare2) {
		if (compare1 > compare2) {
			return isBetween(number, compare2, compare1);
		} else {
			return isBetween(number, compare1, compare2);
		}
	}
	
	public static boolean isBetweenUnordered(double number, double compare1, double compare2) {
		if (compare1 > compare2) {
			return isBetween(number, compare2, compare1);
		} else {
			return isBetween(number, compare1, compare2);
		}
	}

	public static int clamp(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}

	public static long clamp(long value, long min, long max) {
		return Math.min(Math.max(value, min), max);
	}
	
	public static double clamp(double value, double min, double max) {
		return Math.min(Math.max(value, min), max);
	}
}
