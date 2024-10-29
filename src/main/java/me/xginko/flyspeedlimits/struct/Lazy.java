package me.xginko.flyspeedlimits.struct;

import java.util.Objects;
import java.util.function.Supplier;

public class Lazy<E> implements Supplier<E> {

    private final Supplier<E> supplier;
    private E value;

    private Lazy(Supplier<E> supplier) {
        this.supplier = supplier;
    }

    public static <E> Lazy<E> of(Supplier<E> supplier) {
        Objects.requireNonNull(supplier, "Can't create lazy if supplier is null!");
        return new Lazy<>(supplier);
    }

    public boolean isEmpty() {
        return this.value == null;
    }

    public void reset() {
        this.value = null;
    }

    @Override
    public E get() {
        if (this.isEmpty()) {
            this.value = this.supplier.get();
        }
        return value;
    }
}
