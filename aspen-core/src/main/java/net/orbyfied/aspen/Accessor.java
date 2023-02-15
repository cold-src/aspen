package net.orbyfied.aspen;

import net.orbyfied.aspen.util.UnsafeUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Provides an accessor for properties
 * of the given type.
 *
 * @param <T> The value type.
 *
 *
 * @author orbyfied
 */
public interface Accessor<T> {

    static <T> Accessor<T> memoryLocal() {
        return new Accessor<>() {
            // the value
            T val;

            @Override
            public T get(Schema schema, Property<T, ?> property) {
                return val;
            }

            @Override
            public void set(Schema schema, Property<T, ?> property, T value) {
                val = value;
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <T> Accessor<T> forField(Field field) {
        final Unsafe unsafe = UnsafeUtil.getUnsafe();
        final long offset = unsafe.objectFieldOffset(field);
        return new Accessor<>() {
            @Override
            public T get(Schema schema, Property<T, ?> property) {
                return (T) unsafe.getObjectVolatile(schema.instance, offset);
            }

            @Override
            public void set(Schema schema, Property<T, ?> property, T value) {
                unsafe.putObjectVolatile(schema.instance, offset, value);
            }
        };
    }

    ///////////////////////////////

    T get(Schema schema, Property<T, ?> property);

    void set(Schema schema, Property<T, ?> property, T value);

}
