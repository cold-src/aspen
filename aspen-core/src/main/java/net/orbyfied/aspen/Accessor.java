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

    static <T> Accessor<T> special(T value) {
        return new Accessor<>() {
            @Override
            public T get(Schema schema, Property<T, ?> property) {
                return value;
            }

            @Override
            public void register(Schema schema, Property<T, ?> property, T value) {

            }
        };
    }

    static <T> Accessor<T> memoryLocal() {
        return new Accessor<>() {
            // the value
            T val;

            @Override
            public T get(Schema schema, Property<T, ?> property) {
                return val;
            }

            @Override
            public void register(Schema schema, Property<T, ?> property, T value) {
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
            public void register(Schema schema, Property<T, ?> property, T value) {
                unsafe.putObjectVolatile(schema.instance, offset, value);
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <T> Accessor<T> foreignField(Schema source,
                                        Field field) {
        final Unsafe unsafe = UnsafeUtil.getUnsafe();
        final long offset = unsafe.objectFieldOffset(field);
        return new Accessor<>() {
            @Override
            public T get(Schema schema, Property<T, ?> property) {
                return (T) unsafe.getObjectVolatile(source.instance, offset);
            }

            @Override
            public void register(Schema schema, Property<T, ?> property, T value) {
                unsafe.putObjectVolatile(source.instance, offset, value);
            }
        };
    }

    ///////////////////////////////

    /**
     * Get the value of the property if
     * present in the given context.
     *
     * @param schema The schema.
     * @param property The property.
     * @return The value if present.
     */
    T get(Schema schema, Property<T, ?> property);

    /**
     * Register a value.
     *
     * This may mean setting the value,
     * appending for embedded properties,
     * or anything else.
     *
     * @param schema The schema.
     * @param property The property.
     * @param value The value to register.
     */
    void register(Schema schema, Property<T, ?> property, T value);

}
