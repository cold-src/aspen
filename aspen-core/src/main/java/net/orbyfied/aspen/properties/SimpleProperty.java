package net.orbyfied.aspen.properties;

import net.orbyfied.aspen.Property;
import net.orbyfied.aspen.context.PropertyContext;
import net.orbyfied.aspen.raw.nodes.RawListNode;
import net.orbyfied.aspen.raw.nodes.RawObjectNode;
import net.orbyfied.aspen.raw.nodes.RawNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class SimpleProperty<T> extends Property<T, T> {

    public static <T> Builder<T, T, SimpleProperty<T>> builder(String name, Class<T> tClass) {
        return new Builder<>(name, tClass, tClass, SimpleProperty::new);
    }

    public static <T> Builder<T, T, SimpleProperty<T>> anonymous(Class<T> tClass) {
        return Builder.newAnonymous(tClass, tClass, SimpleProperty::new);
    }

    @Override
    public T valueFromPrimitive(T primitive) {
        return primitive;
    }

    @Override
    public T valueToPrimitive(T value) {
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected RawNode emitValue0(PropertyContext context, T value) {
        if (value instanceof Map map) {
            RawObjectNode node = new RawObjectNode();
            map.forEach((k, v) -> {
                node.putEntry(k, emitValue0(context, (T) v));
            });

            return node;
        }

        if (value instanceof Collection collection) {
            RawListNode node = new RawListNode();
            collection.forEach(v -> {
                node.addElement(emitValue0(context, (T)v));
            });

            return node;
        }

        return super.emitValue0(context, value);
    }

    @Override
    protected T loadValue0(PropertyContext context, RawNode node) {
        if (node instanceof RawObjectNode mapNode) {
            Map<String, Object> map = new HashMap<>();
        }

        return super.loadValue0(context, node);
    }

}
