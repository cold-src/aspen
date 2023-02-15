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

    @SuppressWarnings("unchecked")
    static <T> Accessor<T> forField(Field field) {
        final Unsafe unsafe = UnsafeUtil.getUnsafe();
        final long offset = unsafe.objectFieldOffset(field);
        return new Accessor<>() {
            @Override
            public T get(Schema schema) {
                return (T) unsafe.getObjectVolatile(schema.instance, offset);
            }

            @Override
            public void set(Schema schema, T value) {
                unsafe.putObjectVolatile(schema.instance, offset, value);
            }
        };
    }

    ///////////////////////////////

    T get(Schema schema);

    void set(Schema schema, T value);

}
