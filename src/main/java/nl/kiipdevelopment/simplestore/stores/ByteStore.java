package nl.kiipdevelopment.simplestore.stores;

import nl.kiipdevelopment.simplestore.Store;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class ByteStore implements Store<byte[]>, Function<String, byte[]> {
	private final ConcurrentMap<String, byte[]> data = new ConcurrentHashMap<>();

	@Override
	public @NotNull Map<String, byte[]> data() {
		return Collections.unmodifiableMap(data);
	}

	@Override
	public byte @Nullable [] get(@NotNull String key) {
		return data.get(key);
	}
	
	@Override
	public void set(@NotNull String key, byte @NotNull [] value) {
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
	public byte[] apply(String key) {
		return get(key);
	}

	@Override
	public Function<byte[], byte[]> fromByteArray() {
		return Function.identity();
	}

	@Override
	public Function<byte[], byte[]> toByteArray() {
		return Function.identity();
	}
}
