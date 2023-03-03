package net.orbyfied.aspen.util;

public class ArrayUtil {

    /**
     * Index the array relatively using the offset.
     *
     * A negative offset will decrement from the end
     * of the array making -1 the last element, while
     * a positive index is just an absolute index.
     *
     * @param arr The array.
     * @param offset The offset.
     * @param <T> The arrays element type.
     * @return The element selected.
     */
    public static <T> T indexRelative(T[] arr, int offset) {
        return offset < 0 ?
                arr[arr.length + offset] :
                arr[offset];
    }

    public static <T> T lastOf(T[] arr) {
        return arr[arr.length - 1];
    }

}
