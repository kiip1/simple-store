package nl.kiipdevelopment.simplestore.utils.type;

import nl.kiipdevelopment.simplestore.binary.BinaryReader;
import nl.kiipdevelopment.simplestore.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class FloatArrayData implements DataType<float[]> {
    @Override
    public void encode(@NotNull BinaryWriter writer, float @NotNull [] value) {
        writer.writeVarInt(value.length);

        for (float val : value) {
            writer.writeFloat(val);
        }
    }

    @Override
    public float @NotNull [] decode(@NotNull BinaryReader reader) {
        float[] array = new float[reader.readVarInt()];

        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readFloat();
        }

        return array;
    }
}
