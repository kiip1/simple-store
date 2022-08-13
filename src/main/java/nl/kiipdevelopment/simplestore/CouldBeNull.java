package nl.kiipdevelopment.simplestore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CouldBeNull<T> {
    private T value = null;

    public CouldBeNull() {}

    public CouldBeNull(@Nullable T value) {
        this.value = value;
    }

    public CouldBeNull(@NotNull Supplier<T> value) {
        this.value = value.get();
    }

    public CouldBeNull(@NotNull Callable<T> value, Consumer<Exception> exceptionConsumer) {
        try {
            this.value = value.call();
        } catch (Exception e) {
            exceptionConsumer.accept(e);
        }
    }

    public @Nullable T get() {
        return value;
    }

    public void update(T value) {
        this.value = value;
    }

    public @NotNull T getOrDefault(@NotNull T defaultValue) {
        return isNull()
            ? defaultValue
            : asNotNull();
    }

    public @NotNull T getOrDefault(@NotNull Supplier<T> defaultValue) {
        return isNull()
            ? defaultValue.get()
            : asNotNull();
    }

    public void ifNull(@NotNull Runnable runnable) {
        if (isNull()) {
            runnable.run();
        }
    }

    public void ifNullThenUpdate(@NotNull Supplier<T> supplier) {
        if (isNull()) {
            value = supplier.get();
        }
    }

    public void ifNotNull(@NotNull Consumer<T> consumer) {
        if (!isNull()) {
            consumer.accept(value);
        }
    }

    public void ifNotNullThenUpdate(@NotNull Function<T, T> function) {
        if (!isNull()) {
            value = function.apply(value);
        }
    }

    public boolean isNull() {
        return value == null;
    }

    public @NotNull T asNotNull() {
        return Objects.requireNonNull(value);
    }

    public @NotNull Optional<T> asOptional() {
        return Optional.ofNullable(value);
    }
}
