package learning_management_system.backend.utility;

import java.util.function.Consumer;

/** PATCH helper: only values that were sent (non-null) are applied. */
public final class Patch {
    private Patch() {}
    public static <T> void set(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }
}
