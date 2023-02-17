package net.orbyfied.aspen.util;

public interface ThrowingConsumer<T> {

    void accept(T value) throws Throwable;

}
