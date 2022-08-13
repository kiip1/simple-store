package nl.kiipdevelopment.simplestore.utils.type;

import nl.kiipdevelopment.simplestore.binary.BinaryReader;
import nl.kiipdevelopment.simplestore.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DoubleArrayData implements DataType<double[]> {
    @Override
    public void encode(@NotNull BinaryWriter writer, double @NotNull [] value) {
        writer.writeVarInt(value.length);

        for (double val : value) {
            writer.writeDouble(val);
        }
    }

    @Override
    public double @NotNull [] decode(@NotNull BinaryReader reader) {
        double[] array = new double[reader.readVarInt()];

        for (int i = 0; i < array.length; i++) {
            array[i] = reader.readDouble();
        }

        return array;
    }
}
