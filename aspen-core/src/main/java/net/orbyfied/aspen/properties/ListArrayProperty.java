package net.orbyfied.aspen.properties;

import net.orbyfied.aspen.Accessor;
import net.orbyfied.aspen.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores an array as a list.
 *
 * @param <E> The element type.
 *
 * @author orbyfied
 */
public class ListArrayProperty<E> extends Property<E[], List<E>> {

    public ListArrayProperty(String name, Class<E[]> type, String comment, Accessor<E[]> accessor) {
        super(name, type, List.class, comment, accessor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E[] valueFromPrimitive(List<E> primitive) {
        if (primitive == null)
            return null;
        return (E[]) primitive.toArray();
    }

    @Override
    public List<E> valueToPrimitive(E[] value) {
        if (value == null)
            return null;
        return new ArrayList<>(Arrays.asList(value));
    }

}
