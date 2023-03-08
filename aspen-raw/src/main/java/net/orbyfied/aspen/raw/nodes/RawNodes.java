package net.orbyfied.aspen.raw.nodes;

import net.orbyfied.aspen.raw.Section;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility methods for handling {@link RawNode}s and
 * node-based implementations of {@link Section}.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class RawNodes {

    public static RawNode toNode(Object value) {
        if (value == null) {
            return RawScalarNode.nullNode();
        }

        if (value instanceof RawNode rawNode) {
            return rawNode;
        }

        if (value instanceof Collection collection) {
            List<RawNode> list = new ArrayList<>();
            for (Object obj : collection) {
                list.add(toNode(obj));
            }

            return new RawListNode(list);
        }

        if (value instanceof Map map) {
            List<RawNode> list = new ArrayList<>();
            map.forEach((k, v) -> {
                RawPairNode pairNode = new RawPairNode(
                        toNode(k),
                        toNode(v)
                );

                list.add(pairNode);
            });

            return new RawObjectNode(list);
        }

        return new RawScalarNode<>(value);
    }

    public static Section<String> nodeToObjectSection(RawObjectNode objectNode) {
        return new RawNodeObjectSection(objectNode);
    }

    public static Section<Integer> nodeToListSection(RawListNode listNode) {
        return new RawNodeListSection(listNode);
    }

    /**
     * An object section which utilizes a raw
     * mapping node.
     */
    static class RawNodeObjectSection implements Section<String> {
        // the raw mapping node
        final RawObjectNode node;

        RawNodeObjectSection(RawObjectNode node) {
            this.node = node;
        }

        @Override
        public int size() {
            return node.getSize();
        }

        @Override
        public <T> T get(String key) {
            return (T) node.toValue();
        }

        @Override
        public Section<String> set(String key, Object val) {
            node.putEntry(key, toNode(val));
            return this;
        }

        @Override
        public <T> T getOr(String key, T def) {
            return (T) node.toValue().getOrDefault(key, def);
        }

        @Override
        public <T> T getOrCompute(String key, Function<String, T> supplier) {
            return (T) node.toValue().computeIfAbsent(key, supplier);
        }

        @Override
        public boolean has(String key) {
            return node.toValue().containsKey(key);
        }

        @Override
        public Collection<String> keys() {
            return node.toValue().keySet();
        }

        @Override
        public Collection<Object> values() {
            return node.toValue().values();
        }

        @Override
        public Section<String> object(String key) {
            return nodeToObjectSection(node.toMap().get(key).expect(RawObjectNode.class));
        }

        @Override
        public Section<Integer> list(String key) {
            return nodeToListSection(node.toMap().get(key).expect(RawListNode.class));
        }
    }

    /**
     * An list section which utilizes a raw
     * list node.
     */
    static class RawNodeListSection implements Section<Integer> {
        // the raw mapping node
        final RawListNode node;

        RawNodeListSection(RawListNode node) {
            this.node = node;
        }

        @Override
        public int size() {
            return node.getSize();
        }

        @Override
        public <T> T get(Integer key) {
            if (key < 0 || key >= node.getSize())
                return null;
            return (T) node.toValue().get(key);
        }

        @Override
        public Section<Integer> set(Integer key, Object val) {
            node.setElement(key, toNode(val));
            return this;
        }

        @Override
        public boolean has(Integer key) {
            return !(key < 0 || key >= node.getSize());
        }

        @Override
        public Collection<Integer> keys() {
            return /* todo: int range */ null;
        }

        @Override
        public Collection<Object> values() {
            return node.toValue();
        }

        @Override
        public Section<String> object(Integer key) {
            return nodeToObjectSection(node.getElement(key).expect(RawObjectNode.class));
        }

        @Override
        public Section<Integer> list(Integer key) {
            return nodeToListSection(node.getElement(key).expect(RawListNode.class));
        }
    }

}
