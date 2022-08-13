package nl.kiipdevelopment.simplestore.utils;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public final class ReflectionUtils {
	public static final Unsafe UNSAFE;

	private static final int nrBits = Integer.parseInt(System.getProperty("sun.arch.data.model"));
	private static final int word = nrBits / Byte.SIZE;
	private static final int minSize = 16;
	
	static {
		Unsafe unsafe;
		
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new Error(e);
		}
		
		UNSAFE = unsafe;
	}
	
	private ReflectionUtils() {}
	
	/**
	 * Creates an instance of a class.
	 *
	 * @param clazz The class to instantiate
	 * @return An instance of the clazz parameter
	 * @throws InstantiationException When the class couldn't be instantiated
	 */
	public static <T> T create(@NotNull Class<T> clazz) throws InstantiationException {
		return clazz.cast(ReflectionUtils.UNSAFE.allocateInstance(clazz));
	}
	
	/**
	 * Gets a class reference from a binary class name.
	 *
	 * @param clazz The binary class name to fromInt a class reference of
	 * @return A class reference of the clazz parameter
	 * @throws ClassNotFoundException When the class couldn't be found
	 */
	public static Class<?> clazz(@NotNull String clazz) throws ClassNotFoundException {
		return Class.forName(clazz);
	}

	public static int sizeOf(Class<?> src) {
		List<Field> instanceFields = new LinkedList<>();

		do {
			if (src == Object.class) {
				return minSize;
			}

			for (Field f : src.getDeclaredFields()) {
				if ((f.getModifiers() & Modifier.STATIC) == 0){
					instanceFields.add(f);
				}
			}

			src = src.getSuperclass();
		} while(instanceFields.isEmpty());

		long maxOffset = 0;
		for (Field f : instanceFields) {
			long offset = UNSAFE.objectFieldOffset(f);

			if (offset > maxOffset) {
				maxOffset = offset;
			}
		}

		return (((int) maxOffset / word) + 1) * word;
	}
}
