package nl.kiipdevelopment.simplestore.utils.type;

import nl.kiipdevelopment.simplestore.binary.BinaryReader;
import nl.kiipdevelopment.simplestore.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public interface DataType<T> {
    void encode(@NotNull BinaryWriter writer, @NotNull T value);

    @NotNull T decode(@NotNull BinaryReader reader);
}
