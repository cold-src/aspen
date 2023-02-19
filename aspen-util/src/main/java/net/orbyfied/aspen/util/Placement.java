package net.orbyfied.aspen.util;

import java.util.List;
import java.util.function.Predicate;

/**
 * Finds an index to place an element
 * of type T at inside an ordered collection
 * of type T.
 *
 * @param <T> The element type.
 */
public interface Placement<T> {

    static <T> Placement<T> at(int offset) {
        return (list, value) -> {
            if (offset < 0) {
                return list.size() - 1 + offset;
            } else {
                return offset;
            }
        };
    }

    static <T> Placement<T> last() {
        return (list, value) -> list.size();
    }

    static <T> Placement<T> last(int offset) {
        return (list, value) -> list.size() + offset;
    }

    static <T> Placement<T> before(Predicate<T> predicate) {
        return (list, value) -> {
            final int l = list.size();
            for (int i = 0; i < l; i++) {
                T t = list.get(i);

                if (predicate.test(t)) {
                    return i;
                }
            }

            return -1;
        };
    }

    ////////////////////////////////

    int find(List<T> list, T value);

    default int findChecked(List<T> list, T value) {
        int index = find(list, value);
        if (index == -1)
            throw new IllegalArgumentException("Could not find an index for the given element");
        return index;
    }

    default void add(List<T> list, T value) {
        list.add(findChecked(list, value), value);
    }

}
