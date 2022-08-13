package nl.kiipdevelopment.simplestore.utils.type;

import nl.kiipdevelopment.simplestore.binary.BinaryReader;
import nl.kiipdevelopment.simplestore.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class GenericArrayData<T> implements DataType<T[]> {
    private final Function<Integer, T[]> create;
    private final BiConsumer<BinaryWriter, T> write;
    private final Function<BinaryReader, T> read;

    public GenericArrayData(Function<Integer, T[]> create, BiConsumer<BinaryWriter, T> write, Function<BinaryReader, T> read) {
        this.create = create;
        this.write = write;
        this.read = read;
    }

    @Override
    public void encode(@NotNull BinaryWriter writer, T @NotNull [] value) {
        writer.writeVarInt(value.length);

        for (T val : value) {
            write.accept(writer, val);
        }
    }

    @Override
    public T @NotNull [] decode(@NotNull BinaryReader reader) {
        T[] array = create.apply(reader.readVarInt());

        for (int i = 0; i < array.length; i++) {
            array[i] = read.apply(reader);
        }

        return array;
    }
}
