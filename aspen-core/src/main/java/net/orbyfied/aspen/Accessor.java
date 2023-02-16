package net.orbyfied.aspen;

import net.orbyfied.aspen.util.UnsafeUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.function.Supplier;

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

            @Override
            public boolean has(Schema schema, Property<T, ?> property) {
                return true;
            }
        };
    }

    static <T> Accessor<T> memoryLocal() {
        return new Accessor<>() {
            // the value
            T val;
            // if it is set
            boolean set = false;

            @Override
            public T get(Schema schema, Property<T, ?> property) {
                return val;
            }

            @Override
            public void register(Schema schema, Property<T, ?> property, T value) {
                val = value;
                set = true;
            }

            @Override
            public boolean has(Schema schema, Property<T, ?> property) {
                return set;
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <T> Accessor<T> forField(Schema source,
                                    Field field) {
        final Unsafe unsafe = UnsafeUtil.getUnsafe();
        final long offset = unsafe.objectFieldOffset(field);
        return new Accessor<>() {
            // if a value has been set
            boolean set = false;

            @Override
            public T get(Schema schema, Property<T, ?> property) {
                return (T) unsafe.getObjectVolatile(source.instance, offset);
            }

            @Override
            public void register(Schema schema, Property<T, ?> property, T value) {
                unsafe.putObjectVolatile(source.instance, offset, value);
                set = true;
            }

            @Override
            public boolean has(Schema schema, Property<T, ?> property) {
                return set;
            }
        };
    }

    static <T> Accessor<T> defaulted(Accessor<T> accessor,
                                     Supplier<T> defSupplier) {
        if (defSupplier == null)
            return accessor;
        return new Accessor<>() {
            @Override
            public T get(Schema schema, Property<T, ?> property) {
                if (!accessor.has(schema, property)) {
                    T t = defSupplier.get();
                    register(schema, property, t);
                    return t;
                }

                return accessor.get(schema, property);
            }

            @Override
            public void register(Schema schema, Property<T, ?> property, T value) {
                accessor.register(schema, property, value);
            }

            @Override
            public boolean has(Schema schema, Property<T, ?> property) {
                return accessor.has(schema, property);
            }
        };
    }

    static <T> Accessor<T> dynamic(Supplier<Accessor<T>> supplier) {
        return new Accessor<>() {
            @Override
            public T get(Schema schema, Property<T, ?> property) {
                return supplier.get().get(schema, property);
            }

            @Override
            public void register(Schema schema, Property<T, ?> property, T value) {
                supplier.get().register(schema, property, value);
            }

            @Override
            public boolean has(Schema schema, Property<T, ?> property) {
                return supplier.get().has(schema, property);
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

    /**
     * If this accessor can access a value
     * in the given context.
     *
     * @param schema The schema.
     * @param property The property.
     * @return True/false.
     */
    boolean has(Schema schema, Property<T, ?> property);

}
