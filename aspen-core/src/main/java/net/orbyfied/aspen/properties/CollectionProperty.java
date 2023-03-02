package net.orbyfied.aspen.properties;

import net.orbyfied.aspen.Property;
import net.orbyfied.aspen.context.PropertyContext;
import net.orbyfied.aspen.raw.nodes.RawListNode;
import net.orbyfied.aspen.raw.nodes.RawNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * A property which embeds a list and
 * does processing on it's values.
 *
 * @param <E> The elements complex type.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class CollectionProperty<E> extends Property<Collection<E>, List> {

    public static <E> Builder<Collection<E>, List, CollectionProperty<E>> builder(String name, Property<E, ?> embedded,
                                                                                  Supplier<Collection<E>> supplier) {
        return new Builder<>(name, Collection.class, List.class, () -> new CollectionProperty<E>(embedded) {
            @Override
            protected Collection<E> newCollection() {
                return supplier.get();
            }
        });
    }

    public static <E> Builder<Collection<E>, List, CollectionProperty<E>> arrayList(String name, Property<E, ?> embedded) {
        return builder(name, embedded, ArrayList::new);
    }

    //////////////////////////////////////////////////

    // the embedded property
    Property embedded;

    protected CollectionProperty(Property embedded) {
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
    protected RawNode emitValue0(PropertyContext context, Collection<E> value) {
        RawListNode node = new RawListNode();
        for (E elem : value) {
            node.addElement(embedded.emitValue(context, elem));
        }

        return node;
    }

    @Override
    protected Collection<E> loadValue0(PropertyContext context, RawNode node) {
        if (!(node instanceof RawListNode listNode))
            throw new IllegalStateException("Not a list node");
        Collection<E> collection = newCollection();
        for (RawNode elem : listNode.getNodes()) {
            collection.add((E) embedded.loadValue(context, elem));
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
