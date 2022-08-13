package nl.kiipdevelopment.simplestore.utils.type;

import nl.kiipdevelopment.simplestore.binary.BinaryReader;
import nl.kiipdevelopment.simplestore.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class CharacterArrayData implements DataType<char[]> {
    @Override
    public void encode(@NotNull BinaryWriter writer, char @NotNull [] value) {
        writer.writeVarInt(value.length);

        for (char val : value) {
            writer.writeChar(val);
        }
    }

    @Override
    public char @NotNull [] decode(@NotNull BinaryReader reader) {
        char[] array = new char[reader.readVarInt()];

        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readChar();
        }

        return array;
    }
}
