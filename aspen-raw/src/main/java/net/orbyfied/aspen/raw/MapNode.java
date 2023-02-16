package net.orbyfied.aspen.raw;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A node which holds a section, object or just map.
 */
@SuppressWarnings("rawtypes")
public class MapNode extends ValueNode<LinkedHashMap<String, ValueNode>> {

    public MapNode() {
        setValue(new LinkedHashMap<>());
    }

    public MapNode(LinkedHashMap<String, ValueNode> map) {
        super(map);
    }

    public MapNode(Map<String, ValueNode> map) {
        super(new LinkedHashMap<>(map));
    }

    public MapNode putEntry(String key, ValueNode val) {
        value.put(key, val);
        return this;
    }

    /**
     * Merge this node with the given node.
     *
     * @return The new merged node.
     */
    public MapNode merge(MapNode node) {
        LinkedHashMap<String, ValueNode> map = new LinkedHashMap<>(getValue());
        map.putAll(node.value);
        return new MapNode(map);
    }

    public MapNode addAll(MapNode mapNode) {
        this.value.putAll(mapNode.value);
        return this;
    }

}
