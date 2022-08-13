package nl.kiipdevelopment.simplestore.utils.type;

import nl.kiipdevelopment.simplestore.binary.BinaryReader;
import nl.kiipdevelopment.simplestore.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ByteArrayData implements DataType<byte[]> {
    @Override
    public void encode(@NotNull BinaryWriter writer, byte @NotNull [] value) {
        writer.writeVarInt(value.length);
        writer.writeBytes(value);
    }

    @Override
    public byte @NotNull [] decode(@NotNull BinaryReader reader) {
        int length = reader.readVarInt();

        return reader.readBytes(length);
    }
}
