package net.orbyfied.aspen.properties;

import net.orbyfied.aspen.Accessor;
import net.orbyfied.aspen.Property;
import net.orbyfied.aspen.raw.ListNode;
import net.orbyfied.aspen.raw.ValueNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A property which embeds a list and
 * does processing on it's values.
 *
 * @param <E> The elements complex type.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class CollectionProperty<E> extends Property<Collection<E>, List> {

    // the embedded property
    Property embedded;

    protected CollectionProperty(String name, String comment, Accessor<Collection<E>> accessor,
                                 Property embedded) {
        super(name, List.class, List.class, comment, accessor);
        this.embedded = embedded;
    }

    /*
        todo: better way to handle embedded properties
         when serialization gets node-structured
     */

    @Override
    public Collection<E> valueFromPrimitive(List primitive) {
        Collection<E> collection = newCollection();
        for (Object elem : primitive) {
            collection.add((E) embedded.valueFromPrimitive(elem));
        }

        return collection;
    }

    @Override
    public List valueToPrimitive(Collection<E> value) {
        // convert through embedded property
        List out = new ArrayList(value.size());
        for (E elem : value) {
            out.add(embedded.valueToPrimitive(out));
        }

        return out;
    }

    @Override
    public ValueNode emitValue(Collection<E> value) {
        ListNode node = new ListNode();
        for (E elem : value) {
            node.addElement(embedded.emitValue(elem));
        }

        return node;
    }

    @Override
    public Collection<E> loadValue(ValueNode node) {
        if (!(node instanceof ListNode listNode))
            throw new IllegalStateException("Not a list node");
        Collection<E> collection = newCollection();
        for (ValueNode elem : listNode.getValue()) {
            collection.add((E) embedded.loadValue(elem));
        }

        return collection;
    }

    /**
     * Creates a new collection of the
     * appropriate type.
     *
     * @return The collection.
     */
    protected abstract Collection<E> newCollection();

}
