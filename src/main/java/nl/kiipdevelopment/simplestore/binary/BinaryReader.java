package nl.kiipdevelopment.simplestore.binary;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BinaryReader extends InputStream {
	private final ByteBuffer buffer;

	public BinaryReader(@NotNull ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public BinaryReader(byte[] bytes) {
		this(ByteBuffer.wrap(bytes));
	}

	public BinaryReader slice(int length) {
		return new BinaryReader(buffer.slice(buffer.position(), length));
	}

	public void skip(int length) {
		buffer.position(buffer.position() + length);
	}

	public int readVarInt() {
		int result = 0;
		for (int shift = 0; ; shift += 7) {
			byte b = buffer.get();
			result |= (b & 0x7f) << shift;
			if (b >= 0) {
				return result;
			}
		}
	}

	public long readVarLong() {
		long result = 0;
		for (int shift = 0; shift < 56; shift += 7) {
			byte b = buffer.get();
			result |= (b & 0x7fL) << shift;
			if (b >= 0) {
				return result;
			}
		}
		return result | (buffer.get() & 0xffL) << 56;
	}

	public boolean readBoolean() {
		return buffer.get() == 1;
	}

	public byte readByte() {
		return buffer.get();
	}

	public short readShort() {
		return buffer.getShort();
	}

	public char readChar() {
		return buffer.getChar();
	}

	public int readUnsignedShort() {
		return buffer.getShort() & 0xFFFF;
	}

	public int readInt() {
		return buffer.getInt();
	}

	public long readLong() {
		return buffer.getLong();
	}

	public float readFloat() {
		return buffer.getFloat();
	}

	public double readDouble() {
		return buffer.getDouble();
	}

	public String readSizedString() {
		final int length = readVarInt();
		byte[] bytes = new byte[length];
		try {
			this.buffer.get(bytes);
		} catch (BufferUnderflowException e) {
			throw new RuntimeException("Could not read " + length + ", " + buffer.remaining() + " remaining.");
		}
		final String str = new String(bytes, StandardCharsets.UTF_8);
		return str;
	}

	public byte[] readBytes(int length) {
		byte[] bytes = new byte[length];
		buffer.get(bytes);
		return bytes;
	}

	public String[] readSizedStringArray() {
		final int size = readVarInt();
		final String[] strings = new String[size];

		for (int i = 0; i < size; i++) {
			strings[i] = readSizedString();
		}

		return strings;
	}

	public int[] readVarIntArray() {
		final int size = readVarInt();
		final int[] array = new int[size];

		for (int i = 0; i < size; i++) {
			array[i] = readVarInt();
		}

		return array;
	}

	public long[] readVarLongArray() {
		final int size = readVarInt();
		final long[] array = new long[size];

		for (int i = 0; i < size; i++) {
			array[i] = readVarLong();
		}

		return array;
	}

	public byte[] readRemainingBytes() {
		return readBytes(available());
	}

	public UUID readUuid() {
		return new UUID(readLong(), readLong());
	}

	public ByteBuffer buffer() {
		return buffer;
	}

	public byte[] extractBytes(Runnable extractor) {
		int startingPosition = buffer.position();
		extractor.run();
		int endingPosition = buffer.position();
		byte[] output = new byte[endingPosition - startingPosition];
		buffer.get(output, 0, output.length);
		//buffer.fromInt(startingPosition, output);
		return output;
	}

	public boolean hasRemaining() {
		return buffer.hasRemaining();
	}

	@Override
	public int read() {
		return readByte() & 0xFF;
	}

	@Override
	public int available() {
		return buffer.remaining();
	}
}
