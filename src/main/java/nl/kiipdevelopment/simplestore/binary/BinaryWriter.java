package nl.kiipdevelopment.simplestore.binary;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class BinaryWriter extends OutputStream {
	private ByteBuffer buffer;

	private final boolean resizable;

	private BinaryWriter(ByteBuffer buffer, boolean resizable) {
		this.buffer = buffer;
		this.resizable = resizable;
	}

	public BinaryWriter(@NotNull ByteBuffer buffer) {
		this.buffer = buffer;
		this.resizable = true;
	}

	public BinaryWriter(int initialCapacity) {
		this(ByteBuffer.allocate(initialCapacity));
	}

	public BinaryWriter() {
		this(255);
	}

	public static BinaryWriter view(ByteBuffer buffer) {
		return new BinaryWriter(buffer, false);
	}

	protected void ensureSize(int length) {
		if (!resizable) return;
		final int position = buffer.position();
		if (position + length >= buffer.limit()) {
			final int newLength = (position + length) * 4;
			var copy = buffer.isDirect() ?
				ByteBuffer.allocateDirect(newLength) : ByteBuffer.allocate(newLength);
			copy.put(buffer.flip());
			this.buffer = copy;
		}
	}

	public void writeByte(byte b) {
		ensureSize(Byte.BYTES);
		buffer.put(b);
	}

	public void writeBoolean(boolean b) {
		writeByte((byte) (b ? 1 : 0));
	}

	public void writeChar(char c) {
		ensureSize(Character.BYTES);
		buffer.putChar(c);
	}

	public void writeShort(short s) {
		ensureSize(Short.BYTES);
		buffer.putShort(s);
	}

	public void writeInt(int i) {
		ensureSize(Integer.BYTES);
		buffer.putInt(i);
	}

	public void writeLong(long l) {
		ensureSize(Long.BYTES);
		buffer.putLong(l);
	}

	public void writeFloat(float f) {
		ensureSize(Float.BYTES);
		buffer.putFloat(f);
	}

	public void writeDouble(double d) {
		ensureSize(Double.BYTES);
		buffer.putDouble(d);
	}

	public void writeVarInt(int i) {
		ensureSize(5);
		if ((i & (0xFFFFFFFF << 7)) == 0) {
			buffer.put((byte) i);
		} else if ((i & (0xFFFFFFFF << 14)) == 0) {
			buffer.putShort((short) ((i & 0x7F | 0x80) << 8 | (i >>> 7)));
		} else if ((i & (0xFFFFFFFF << 21)) == 0) {
			buffer.put((byte) (i & 0x7F | 0x80));
			buffer.put((byte) ((i >>> 7) & 0x7F | 0x80));
			buffer.put((byte) (i >>> 14));
		} else if ((i & (0xFFFFFFFF << 28)) == 0) {
			buffer.putInt((i & 0x7F | 0x80) << 24 | (((i >>> 7) & 0x7F | 0x80) << 16)
				| ((i >>> 14) & 0x7F | 0x80) << 8 | (i >>> 21));
		} else {
			buffer.putInt((i & 0x7F | 0x80) << 24 | ((i >>> 7) & 0x7F | 0x80) << 16
				| ((i >>> 14) & 0x7F | 0x80) << 8 | ((i >>> 21) & 0x7F | 0x80));
			buffer.put((byte) (i >>> 28));
		}
	}

	public void writeVarLong(long l) {
		ensureSize(10);
		do {
			byte temp = (byte) (l & 0b01111111);
			l >>>= 7;
			if (l != 0) {
				temp |= 0b10000000;
			}
			buffer.put(temp);
		} while (l != 0);
	}

	public void writeSizedString(@NotNull String string) {
		final var bytes = string.getBytes(StandardCharsets.UTF_8);
		writeVarInt(bytes.length);
		writeBytes(bytes);
	}

	public void writeVarIntArray(int[] array) {
		if (array == null) {
			writeVarInt(0);
			return;
		}
		writeVarInt(array.length);
		for (int element : array) {
			writeVarInt(element);
		}
	}

	public void writeVarLongArray(long[] array) {
		if (array == null) {
			writeVarInt(0);
			return;
		}
		writeVarInt(array.length);
		for (long element : array) {
			writeVarLong(element);
		}
	}

	public void writeBytes(byte @NotNull [] bytes) {
		if (bytes.length == 0) return;
		ensureSize(bytes.length);
		buffer.put(bytes);
	}

	public void writeStringArray(@NotNull String[] array) {
		if (array == null) {
			writeVarInt(0);
			return;
		}
		writeVarInt(array.length);
		for (String element : array) {
			writeSizedString(element);
		}
	}

	public void writeUuid(@NotNull UUID uuid) {
		writeLong(uuid.getMostSignificantBits());
		writeLong(uuid.getLeastSignificantBits());
	}

	public byte[] toByteArray() {
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return bytes;
	}

	public void writeAtStart(@NotNull BinaryWriter headerWriter) {
		final var headerBuf = headerWriter.buffer;
		final var finalBuffer = concat(headerBuf, buffer);
		updateBuffer(finalBuffer);
	}

	public void writeAtEnd(@NotNull BinaryWriter footerWriter) {
		final var footerBuf = footerWriter.buffer;
		final var finalBuffer = concat(buffer, footerBuf);
		updateBuffer(finalBuffer);
	}

	public static ByteBuffer concat(final ByteBuffer... buffers) {
		final ByteBuffer combined = ByteBuffer.allocate(Arrays.stream(buffers).mapToInt(Buffer::remaining).sum());
		Arrays.stream(buffers).forEach(b -> combined.put(b.duplicate()));
		return combined;
	}

	public @NotNull ByteBuffer buffer() {
		return buffer;
	}

	public void updateBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public void write(int b) {
		writeByte((byte) b);
	}

	public void writeUnsignedShort(int yourShort) {
		ensureSize(Short.BYTES);
		buffer.putShort((short) (yourShort & 0xFFFF));
	}
}
