package net.orbyfied.aspen.properties;

import net.orbyfied.aspen.Property;

public class NumberProperty<T extends Number> extends Property<T, Object> {

    public static <T extends Number> Builder<T, Object, NumberProperty<T>> builder(String name, Class<T> tClass) {
        return new Builder<>(name, tClass, getPrimitiveNumberType(tClass), NumberProperty::new);
    }

    /**
     * Get the primitive number class.
     *
     * @param klass The complex class.
     * @return The primitive type.
     */
    public static Class<?> getPrimitiveNumberType(Class<? extends Number> klass) {
        if (
                klass == Double.class ||
                klass == Float.class
        ) return Double.class;

        if (
                klass == Long.class ||
                klass == Integer.class ||
                klass == Short.class ||
                klass == Byte.class
        ) return Long.class;

        throw new IllegalArgumentException();
    }

    @Override
    public T valueFromPrimitive(Object primitive) {
        return super.valueFromPrimitive(primitive);
    }

}
