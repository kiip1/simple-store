package nl.kiipdevelopment.simplestore.stores;

import nl.kiipdevelopment.simplestore.Storable;
import nl.kiipdevelopment.simplestore.Store;
import nl.kiipdevelopment.simplestore.binary.BinaryReader;
import nl.kiipdevelopment.simplestore.binary.BinaryWriter;
import nl.kiipdevelopment.simplestore.utils.DataUtils;
import nl.kiipdevelopment.simplestore.utils.ReflectionUtils;
import nl.kiipdevelopment.simplestore.utils.type.DataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ObjectStore implements Store<Object> {
	public static final Function<byte[], Object> BYTEARRAY2OBJECT = value -> {
		BinaryReader reader = new BinaryReader(value);

		try {
			boolean isStorable = reader.readBoolean();

			if (isStorable) {
				return StorableStore.BYTEARRAY2STORABLE.apply(reader.readRemainingBytes());
			} else {
				Class<?> clazz = ReflectionUtils.clazz(reader.readSizedString());
				DataType dataType = DataUtils.type(clazz);

				assert dataType != null;
				return dataType.decode(reader);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	};
	public static final Function<Object, byte[]> OBJECT2BYTEARRAY = value -> {
		BinaryWriter writer = new BinaryWriter();

		writer.writeBoolean(value instanceof Storable);
		writer.writeSizedString(value.getClass().getName());

		if (value instanceof Storable storable) {
			storable.write(writer);
		} else {
			DataType dataType = DataUtils.type(value.getClass());

			assert dataType != null;
			dataType.encode(writer, value);
		}

		return writer.toByteArray();
	};

	private final ConcurrentMap<String, Object> data = new ConcurrentHashMap<>();

	@Override
	public @NotNull Map<String, Object> data() {
		return Collections.unmodifiableMap(data);
	}
	
	@Override
	public @Nullable Object get(@NotNull String key) {
		return data.get(key);
	}
	
	@Override
	public void set(@NotNull String key, @NotNull Object value) {
		data.put(key, value);
	}

	@Override
	public void delete(@NotNull String key) {
		data.remove(key);
	}

	@Override
	public boolean exists(@NotNull String key) {
		return data.containsKey(key);
	}

	@Override
	public Function<byte[], Object> fromByteArray() {
		return BYTEARRAY2OBJECT;
	}

	@Override
	public Function<Object, byte[]> toByteArray() {
		return OBJECT2BYTEARRAY;
	}
}
