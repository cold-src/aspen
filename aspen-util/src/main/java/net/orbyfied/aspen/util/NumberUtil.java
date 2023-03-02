package net.orbyfied.aspen.util;

public class NumberUtil {

    @SuppressWarnings("unchecked")
    public static <T extends Number> T castBoxed(Number number,
                                                 Class<T> tClass) {
        if (number == null) return null;
        if (tClass == Integer.class) return (T) Integer.valueOf(number.intValue());
        if (tClass == Double.class) return (T) Double.valueOf(number.doubleValue());
        if (tClass == Long.class) return (T) Long.valueOf(number.longValue());
        if (tClass == Float.class) return (T) Float.valueOf(number.floatValue());
        if (tClass == Short.class) return (T) Short.valueOf(number.shortValue());
        if (tClass == Byte.class) return (T) Byte.valueOf(number.byteValue());

        throw new IllegalArgumentException("Goofy number type " + tClass);
    }

}
