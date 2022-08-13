package nl.kiipdevelopment.simplestore.utils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public final class CompressionUtils {
	private CompressionUtils() {}

	/**
	 * Compresses a byte array.
	 *
	 * @param bytes The bytes to compress
	 * @return The compressed byte array
	 */
	public static byte @NotNull [] compress(byte @NotNull [] bytes) {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, new Deflater(Deflater.BEST_COMPRESSION));
			deflaterOutputStream.write(bytes);
			deflaterOutputStream.flush();
			deflaterOutputStream.close();

			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Decompresses a byte array.
	 *
	 * @param bytes The bytes to decompress
	 * @return The decompressed byte array
	 */
	public static byte @NotNull [] decompress(byte @NotNull [] bytes) {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(byteArrayOutputStream);
			inflaterOutputStream.write(bytes);
			inflaterOutputStream.flush();
			inflaterOutputStream.close();

			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
