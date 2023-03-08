package net.orbyfied.aspen;

import net.orbyfied.aspen.context.PropertyContext;
import net.orbyfied.aspen.util.UnsafeUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
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
            public T get(PropertyContext context) {
                return value;
            }

            @Override
            public void register(PropertyContext context, T value) {

            }

            @Override
            public boolean has(PropertyContext context) {
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
            public T get(PropertyContext context) {
                return val;
            }

            @Override
            public void register(PropertyContext context, T value) {
                val = value;
                set = true;
            }

            @Override
            public boolean has(PropertyContext context) {
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
//            // if a value has been set
//            boolean set = false;

            @Override
            public T get(PropertyContext context) {
                return (T) unsafe.getObjectVolatile(source.instance, offset);
            }

            @Override
            public void register(PropertyContext context, T value) {
                unsafe.putObjectVolatile(source.instance, offset, value);
//                set = true;
            }

            @Override
            public boolean has(PropertyContext context) {
                return true;
            }
        };
    }

    static <T> Accessor<T> defaulted(Accessor<T> accessor,
                                     Supplier<T> defSupplier) {
        if (defSupplier == null)
            return accessor;
        return new Accessor<>() {
            @Override
            public T get(PropertyContext context) {
                if (!accessor.has(context)) {
                    T t = defSupplier.get();
                    register(context, t);
                    return t;
                }

                return accessor.get(context);
            }

            @Override
            public void register(PropertyContext context, T value) {
                accessor.register(context, value);
            }

            @Override
            public boolean has(PropertyContext context) {
                return accessor.has(context);
            }
        };
    }

    static <T> Accessor<T> dynamic(Supplier<Accessor<T>> supplier) {
        return new Accessor<>() {
            @Override
            public T get(PropertyContext context) {
                return supplier.get().get(context);
            }

            @Override
            public void register(PropertyContext context, T value) {
                supplier.get().register(context, value);
            }

            @Override
            public boolean has(PropertyContext context) {
                return supplier.get().has(context);
            }
        };
    }

    /**
     * Creates a new shared accessor which stored
     * each value by schema separately.
     *
     * @return The accessor.
     */
    static <T> Accessor<T> sharedMutable() {
        return new Accessor<>() {
            // the value cache
            final Map<Schema, T> valueMap = new HashMap<>();

            @Override
            public T get(PropertyContext context) {
                return valueMap.get(context.schema());
            }

            @Override
            public void register(PropertyContext context, T value) {
                valueMap.put(context.schema(), value);
            }

            @Override
            public boolean has(PropertyContext context) {
                return valueMap.containsKey(context.schema());
            }
        };
    }

    ///////////////////////////////

    /**
     * Get the value of the property if
     * present in the given context.
     *
     * @param context The context.
     * @return The value if present.
     */
    T get(PropertyContext context);

    /**
     * Register a value.
     *
     * This may mean setting the value,
     * appending for embedded properties,
     * or anything else.
     *
     * @param context The context.
     * @param value The value to register.
     */
    void register(PropertyContext context, T value);

    /**
     * If this accessor can access a value
     * in the given context.
     *
     * @param context The context.
     * @return True/false.
     */
    boolean has(PropertyContext context);

}
