package nl.kiipdevelopment.simplestore;

import nl.kiipdevelopment.simplestore.persistence.FileStrategy;
import nl.kiipdevelopment.simplestore.persistence.StorageMethod;
import nl.kiipdevelopment.simplestore.persistence.storagemethods.DefaultStorageMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

public class SimpleStore<T> implements Store<T> {
    private final Store<T> store;
    private final StorageMethod<T> storageMethod;

    private SimpleStore(Store<T> store, Path location, FileStrategy fileStrategy) {
        this.store = store;
        this.storageMethod = new DefaultStorageMethod<>(store, location, fileStrategy);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public void load() {
        storageMethod.load();
    }

    public void save() {
        storageMethod.save();
    }

    @Override
    public @NotNull Map<String, T> data() {
        return store.data();
    }

    @Override
    public @Nullable T get(@NotNull String key) {
        return store.get(key);
    }

    @Override
    public void set(@NotNull String key, @NotNull T value) {
        store.set(key, value);
    }

    @Override
    public void delete(@NotNull String key) {
        store.delete(key);
    }

    @Override
    public boolean exists(@NotNull String key) {
        return store.exists(key);
    }

    @Override
    public Function<byte[], T> fromByteArray() {
        return store.fromByteArray();
    }

    @Override
    public Function<T, byte[]> toByteArray() {
        return store.toByteArray();
    }

    public static class Builder<T> {
        private Store<T> store;
        private Path location;
        private FileStrategy fileStrategy;

        public Builder<T> store(Store<T> store) {
            this.store = store;

            return this;
        }

        public Builder<T> location(Path location) {
            this.location = location;

            return this;
        }

        public Builder<T> fileStrategy(FileStrategy fileStrategy) {
            this.fileStrategy = fileStrategy;

            return this;
        }

        public SimpleStore<T> build() {
            return new SimpleStore<>(
                store,
                location == null ? Path.of("data") : location,
                fileStrategy == null ? FileStrategy.NORMAL : fileStrategy
            );
        }
    }
}
