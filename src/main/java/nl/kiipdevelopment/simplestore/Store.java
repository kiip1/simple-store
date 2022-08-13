package nl.kiipdevelopment.simplestore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Store<T> {
	@NotNull Map<String, T> data();

	@Nullable T get(@NotNull String key);

	default @NotNull CouldBeNull<T> getCouldBeNull(@NotNull String key) {
		return new CouldBeNull<>(get(key));
	}

	default @NotNull T get(@NotNull String key, @NotNull T fallback) {
		return getCouldBeNull(key)
			.getOrDefault(fallback);
	}

	default @NotNull T get(@NotNull String key, @NotNull Supplier<T> fallback) {
		return getCouldBeNull(key)
			.getOrDefault(fallback);
	}
	
	void set(@NotNull String key, @NotNull T value);

	void delete(@NotNull String key);

	boolean exists(@NotNull String key);

	Function<byte[], T> fromByteArray();

	Function<T, byte[]> toByteArray();
}
