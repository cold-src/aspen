package net.orbyfied.aspen.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtil {

    static final Unsafe UNSAFE;

    static {
        {
            Unsafe tmp;
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                tmp = (Unsafe) f.get(null);
            } catch (Exception e) {
                e.printStackTrace();
                tmp = null;
            }
            UNSAFE = tmp;
        }
    }

    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

}
