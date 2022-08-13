package nl.kiipdevelopment.simplestore.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import nl.kiipdevelopment.simplestore.binary.BinaryReader;
import nl.kiipdevelopment.simplestore.binary.BinaryWriter;
import nl.kiipdevelopment.simplestore.utils.type.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class DataUtils {
    public static final Object2ObjectMap<Class, DataType> DATA_TYPE_MAP = new Object2ObjectOpenHashMap<>();

    static {
        registerType(Byte.class, BinaryWriter::writeByte, BinaryReader::readByte);
        registerType(byte[].class, new ByteArrayData());

        registerType(Boolean.class, BinaryWriter::writeBoolean, BinaryReader::readBoolean);
        registerType(boolean[].class, new BooleanArrayData());

        registerType(Character.class, BinaryWriter::writeChar, BinaryReader::readChar);
        registerType(char[].class, new CharacterArrayData());

        registerType(Short.class, BinaryWriter::writeShort, BinaryReader::readShort);
        registerType(short[].class, new ShortArrayData());

        registerType(Integer.class, BinaryWriter::writeVarInt, BinaryReader::readVarInt);
        registerType(int[].class, BinaryWriter::writeVarIntArray, BinaryReader::readVarIntArray);

        registerType(Long.class, BinaryWriter::writeVarLong, BinaryReader::readVarLong);
        registerType(long[].class, BinaryWriter::writeVarLongArray, BinaryReader::readVarLongArray);

        registerType(Float.class, BinaryWriter::writeFloat, BinaryReader::readFloat);
        registerType(float[].class, new FloatArrayData());

        registerType(Double.class, BinaryWriter::writeDouble, BinaryReader::readDouble);
        registerType(double[].class, new DoubleArrayData());

        registerType(String.class, BinaryWriter::writeSizedString, BinaryReader::readSizedString);
        registerType(String[].class, BinaryWriter::writeStringArray, BinaryReader::readSizedStringArray);

        registerType(UUID.class, BinaryWriter::writeUuid, BinaryReader::readUuid);
    }

    private DataUtils() {}

    public static <T> void registerType(@NotNull Class<T> clazz, @NotNull DataType<T> dataType) {
        DATA_TYPE_MAP.put(clazz, dataType);
    }

    public static <T> void registerType(
        @NotNull Class<T> clazz,
        @NotNull BiConsumer<BinaryWriter, T> encode,
        @NotNull Function<BinaryReader, T> decode
    ) {
        DATA_TYPE_MAP.put(clazz, new DataType<T>() {
            @Override
            public void encode(@NotNull BinaryWriter writer, @NotNull T value) {
                encode.accept(writer, value);
            }

            @NotNull
            @Override
            public T decode(@NotNull BinaryReader reader) {
                return decode.apply(reader);
            }
        });
    }

    @Nullable
    public static <T> DataType<T> type(@NotNull Class<T> clazz) {
        return DATA_TYPE_MAP.get(clazz);
    }
}
