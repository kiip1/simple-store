package nl.kiipdevelopment.simplestore.utils.type;

import nl.kiipdevelopment.simplestore.binary.BinaryReader;
import nl.kiipdevelopment.simplestore.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class BooleanArrayData implements DataType<boolean[]> {
    @Override
    public void encode(@NotNull BinaryWriter writer, boolean @NotNull [] value) {
        writer.writeVarInt(value.length);

        for (boolean val : value) {
            writer.writeBoolean(val);
        }
    }

    @Override
    public boolean @NotNull [] decode(@NotNull BinaryReader reader) {
        boolean[] array = new boolean[reader.readVarInt()];

        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readBoolean();
        }

        return array;
    }
}
