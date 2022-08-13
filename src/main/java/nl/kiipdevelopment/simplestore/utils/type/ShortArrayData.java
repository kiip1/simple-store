package nl.kiipdevelopment.simplestore.utils.type;

import nl.kiipdevelopment.simplestore.binary.BinaryReader;
import nl.kiipdevelopment.simplestore.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ShortArrayData implements DataType<short[]> {
    @Override
    public void encode(@NotNull BinaryWriter writer, short @NotNull [] value) {
        writer.writeVarInt(value.length);

        for (short val : value) {
            writer.writeShort(val);
        }
    }

    @Override
    public short @NotNull [] decode(@NotNull BinaryReader reader) {
        short[] array = new short[reader.readVarInt()];

        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readShort();
        }

        return array;
    }
}
