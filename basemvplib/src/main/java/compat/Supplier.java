package compat;

public interface Supplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get();
}