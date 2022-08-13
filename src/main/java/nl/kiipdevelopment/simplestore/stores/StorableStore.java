package nl.kiipdevelopment.simplestore.stores;

import nl.kiipdevelopment.simplestore.Storable;
import nl.kiipdevelopment.simplestore.Store;
import nl.kiipdevelopment.simplestore.binary.BinaryReader;
import nl.kiipdevelopment.simplestore.binary.BinaryWriter;
import nl.kiipdevelopment.simplestore.utils.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class StorableStore implements Store<Storable> {
	public static final Function<byte[], Storable> BYTEARRAY2STORABLE = value -> {
		BinaryReader reader = new BinaryReader(value);

		try {
			Class<?> clazz = ReflectionUtils.clazz(reader.readSizedString());
			Storable storable = (Storable) ReflectionUtils.create(clazz);
			storable.read(reader);

			return storable;
		} catch (ClassNotFoundException | InstantiationException e) {
			e.printStackTrace();
		}

		return null;
	};
	public static final Function<Storable, byte[]> STORABLE2BYTEARRAY = value -> {
		BinaryWriter writer = new BinaryWriter();

		writer.writeSizedString(value.getClass().getName());
		value.write(writer);

		return writer.toByteArray();
	};

	private final ConcurrentMap<String, Storable> data = new ConcurrentHashMap<>();

	@Override
	public @NotNull Map<String, Storable> data() {
		return Collections.unmodifiableMap(data);
	}
	
	@Override
	public @Nullable Storable get(@NotNull String key) {
		return data.get(key);
	}
	
	@Override
	public void set(@NotNull String key, @NotNull Storable value) {
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
	public Function<byte[], Storable> fromByteArray() {
		return BYTEARRAY2STORABLE;
	}

	@Override
	public Function<Storable, byte[]> toByteArray() {
		return STORABLE2BYTEARRAY;
	}
}
